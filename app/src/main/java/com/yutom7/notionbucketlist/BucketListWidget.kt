package com.yutom7.notionbucketlist

import android.content.Context
// ✅ 重要な修正：ここを Compose の Color に統一！
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Notionのデータを受け取るための設計図
interface NotionApi {
    @GET("api/todo")
    suspend fun getTodoList(): List<TodoItem>
}

class BucketListWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 1. 通信の準備
        val retrofit = Retrofit.Builder()
            .baseUrl("https://notion-bucket-list-api.vercel.app/") // あなたのURL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(NotionApi::class.java)

        // 2. データを取ってくる（フィルタリング付き）
        val tasks = try {
            // いったん全データを取得
            val allTasks = api.getTodoList()

            // ここで「選別」と「並び替え」をします
            allTasks.filter { task ->
                // "2026-12-31" 以前の日付だけを通す（今年まで！）
                task.deadline <= "2026-12-31"
            }.sortedBy { task ->
                // 日付が近い順に並べる
                task.deadline
            }

        } catch (e: Exception) {
            // エラー時は理由を表示（ここはそのまま）
            listOf(TodoItem("error", "通信エラー: ${e.message}", "確認してね"))
        }

        // 3. 画面を作る
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A)) // ✅ 新しい書き方
                    .padding(12.dp)
            ) {
                Text(
                    text = "やりたいことリスト ✨",
                    style = TextStyle(
                        color = ColorProvider(Color.White), // ✅ 新しい書き方
                        fontSize = 18.sp
                    )
                )
                Spacer(GlanceModifier.height(8.dp))

                // リスト表示
                LazyColumn {
                    items(tasks) { task ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔥 ",
                                style = TextStyle(fontSize = 14.sp)
                            )
                            Column {
                                Text(
                                    text = task.title,
                                    style = TextStyle(
                                        color = ColorProvider(Color.White), // ✅ 白文字
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = task.deadline,
                                    style = TextStyle(
                                        color = ColorProvider(Color.LightGray), // ✅ 薄いグレー
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