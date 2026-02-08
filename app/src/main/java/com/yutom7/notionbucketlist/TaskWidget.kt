package com.yutom7.notionbucketlist

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// ★タスク管理ページのURL（タップした時用）
private const val TASK_PAGE_URL = "https://www.notion.so/2d7fc21646f680399a9bd53ba642bd95?source=copy_link"

// データの中身（JSONに合わせる）
data class TaskItem(
    val id: String,
    val title: String,
    val genre: String,
    val status: String
)

interface TaskApi {
    @GET("api/tasks") // ★さっき作ったAPIを指定
    suspend fun getTasks(): List<TaskItem>
}

// レシーバー（ウィジェットの窓口）
class TaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskWidget()
}

// ウィジェット本体
class TaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 通信設定（タイムアウト対策済み）
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://notion-bucket-list-api.vercel.app/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TaskApi::class.java)

        val tasks = try {
            api.getTasks()
        } catch (e: Exception) {
            listOf(TaskItem("error", "通信エラー", "リロードしてね", "error"))
        }

        provideContent {
            // 色の設定（ダークモード対応）
            val white = androidx.compose.ui.graphics.Color.White
            val black = androidx.compose.ui.graphics.Color.Black
            val red = androidx.compose.ui.graphics.Color(0xFFE53935) // 緊急度を感じる赤
            val gray = androidx.compose.ui.graphics.Color.Gray

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(white) // ★今回は白ベースで見やすく！
                    .padding(12.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(TASK_PAGE_URL))
                        )
                    )
            ) {
                // タイトル部分
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 今日のミッション",
                        style = TextStyle(
                            color = ColorProvider(red),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.height(8.dp))

                if (tasks.isEmpty()) {
                    Text(
                        text = "🎉 全部完了！お疲れ様！",
                        style = TextStyle(color = ColorProvider(black), fontSize = 14.sp)
                    )
                } else {
                    LazyColumn {
                        items(tasks) { task ->
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // チェックボックス風アイコン
                                Text(
                                    text = "⬜ ",
                                    style = TextStyle(fontSize = 18.sp)
                                )
                                Column {
                                    Text(
                                        text = task.title,
                                        style = TextStyle(
                                            color = ColorProvider(black),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "🏷 ${task.genre}",
                                        style = TextStyle(
                                            color = ColorProvider(gray),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}