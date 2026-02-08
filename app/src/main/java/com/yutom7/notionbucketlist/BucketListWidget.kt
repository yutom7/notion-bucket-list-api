package com.yutom7.notionbucketlist

import android.content.Context
import android.content.Intent
import android.net.Uri
// ★ポイント：Colorのインポートをあえて外して、コードの中で直接指定することでエラーを防ぎます
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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

// ⚠️ ここに NotionのURL を入れてください
private const val NOTION_URL = "https://www.notion.so/1d4fc21646f680968935d09daa97b331?source=copy_link"

interface NotionApi {
    @GET("api/todo")
    suspend fun getTodoList(): List<TodoItem>
}

class BucketListWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://notion-bucket-list-api.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(NotionApi::class.java)

        val tasks = try {
            val allTasks = api.getTodoList()
            allTasks.filter { task ->
                task.deadline <= "2026-12-31"
            }.sortedBy { task ->
                task.deadline
            }
        } catch (e: Exception) {
            listOf(TodoItem("error", "通信エラー", "確認してね", "エラー"))
        }

        provideContent {
            // ★ここで色を定義（エラー回避のため、フルネームで指定）
            val white = androidx.compose.ui.graphics.Color.White
            val black = androidx.compose.ui.graphics.Color.Black
            val gray = androidx.compose.ui.graphics.Color.Gray
            val blue = androidx.compose.ui.graphics.Color(0xFF0277BD)

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(white) // ★白背景
                    .padding(12.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(NOTION_URL))
                        )
                    )
            ) {
                Text(
                    text = "やりたいことリスト ✨",
                    style = TextStyle(
                        color = ColorProvider(black), // ★黒文字
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(8.dp))

                LazyColumn {
                    items(tasks) { task ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "✅ ",
                                style = TextStyle(fontSize = 14.sp),
                                modifier = GlanceModifier.padding(top = 2.dp)
                            )

                            Column {
                                Text(
                                    text = task.title,
                                    style = TextStyle(
                                        color = ColorProvider(black), // ★タイトルも黒
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Row {
                                    Text(
                                        text = "📅 ${task.deadline}",
                                        style = TextStyle(
                                            color = ColorProvider(gray), // ★日付はグレー
                                            fontSize = 10.sp
                                        )
                                    )

                                    Spacer(GlanceModifier.width(8.dp))

                                    Text(
                                        text = "🏷 ${task.genre}",
                                        style = TextStyle(
                                            color = ColorProvider(blue), // ★ジャンルは濃い青
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