package com.yutom7.notionbucketlist

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

/**
 * API インターフェース
 */
interface NotionApi {
    @GET("api/todo")
    suspend fun getTodoList(): List<TodoItem>
}

/**
 * バックグラウンドで定期的にAPIからデータを取得し、キャッシュに保存する Worker
 */
class TodoSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME = "todo_sync_worker"

        /**
         * 15分ごとの定期実行をスケジュール
         */
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // ネットワーク接続時のみ
                .build()

            val request = PeriodicWorkRequestBuilder<TodoSyncWorker>(
                15, TimeUnit.MINUTES // 15分ごと
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // 既存のワークがあればそのまま維持
                request
            )
        }

        /**
         * 即時同期を実行（ウィジェット追加時など）
         * ネットワーク制約なしで即座に実行
         */
        fun enqueueOneTimeSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<TodoSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // OkHttpClient にタイムアウトを設定
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://notion-bucket-list-api.vercel.app/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(NotionApi::class.java)

            // APIからデータを取得
            val allTasks = api.getTodoList()

            // フィルタリング（ソートはAPI側で完了済み）
            val filteredTasks = allTasks
                .filter { it.deadline <= "2026-12-31" }

            // キャッシュに保存
            val repository = TodoRepository(applicationContext)
            repository.saveTasks(filteredTasks)

            // ウィジェットを更新
            BucketListWidget().updateAll(applicationContext)

            Result.success()
        } catch (e: Exception) {
            // リトライ可能なエラーの場合は再試行
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
