package com.yutom7.notionbucketlist

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

// データクラス（タスク用）
data class TaskItem(
    val id: String,
    val title: String,
    val genre: String,
    val status: String
)

// APIインターフェース（タスク用）
interface TaskApi {
    @GET("api/tasks") // ★さっき作った新しい場所！
    suspend fun getTasks(): List<TaskItem>
}

// ★更新ボタンが押された時の動き
class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // ウィジェットを強制的に更新する
        TodayTaskWidget().update(context, glanceId)
    }
}

class TodayTaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 通信設定（タイムアウト対策）
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
            api.getTasks() // フィルターや並び替えはAPI側でやってあるので、呼ぶだけでOK
        } catch (e: Exception) {
            listOf(TaskItem("error", "通信エラー", "タップして更新", ""))
        }

        provideContent {
            // 色の設定
            val white = androidx.compose.ui.graphics.Color.White
            val black = androidx.compose.ui.graphics.Color.Black
            val gray = androidx.compose.ui.graphics.Color.Gray
            val red = androidx.compose.ui.graphics.Color(0xFFE53935) // 赤っぽい色（重要感）

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(white)
                    .padding(12.dp)
            ) {
                // ★ ヘッダー（タイトル ＋ 更新ボタン）
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日のミッション 🔥",
                        style = TextStyle(
                            color = ColorProvider(black),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(GlanceModifier.defaultWeight()) // 右端に寄せるためのスペース

                    // ★ 手動更新ボタン（ここ！）
                    Text(
                        text = "↻", // くるっと回る矢印
                        style = TextStyle(
                            color = ColorProvider(black),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier
                            .padding(horizontal = 8.dp)
                            .clickable(actionRunCallback<RefreshAction>()) // タップで更新！
                    )
                }

                Spacer(GlanceModifier.height(8.dp))

                if (tasks.isEmpty()) {
                    Text(
                        text = "今日のタスクはありません🎉",
                        style = TextStyle(color = ColorProvider(gray), fontSize = 12.sp)
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
                                // チェックボックス風の四角
                                Text(
                                    text = "⬜ ",
                                    style = TextStyle(fontSize = 16.sp)
                                )

                                Column {
                                    Text(
                                        text = task.title,
                                        style = TextStyle(
                                            color = ColorProvider(black),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    Text(
                                        text = "${task.genre} • ${task.status}",
                                        style = TextStyle(
                                            color = ColorProvider(gray),
                                            fontSize = 10.sp
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