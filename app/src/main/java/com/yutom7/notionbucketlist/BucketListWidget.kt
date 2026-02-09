package com.yutom7.notionbucketlist

import android.content.Context
import android.content.Intent
import android.net.Uri
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

// ⚠️ ここに NotionのURL を入れてください
private const val NOTION_URL = "https://www.notion.so/1d4fc21646f680968935d09daa97b331?source=copy_link"

class BucketListWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // キャッシュからタスクを読み込み（API呼び出しなし）
        val repository = TodoRepository(context)
        val allTasks = repository.loadTasks()
        
        // 未完了と完了を分離
        val incompleteTasks = allTasks.filter { !it.isCompleted }
        val completedTasks = allTasks.filter { it.isCompleted }

        provideContent {
            // ★ここで色を定義（エラー回避のため、フルネームで指定）
            val white = androidx.compose.ui.graphics.Color.White
            val black = androidx.compose.ui.graphics.Color.Black
            val gray = androidx.compose.ui.graphics.Color.Gray
            val blue = androidx.compose.ui.graphics.Color(0xFF0277BD)
            val green = androidx.compose.ui.graphics.Color(0xFF4CAF50)

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

                if (allTasks.isEmpty()) {
                    // キャッシュが空の場合
                    Text(
                        text = "読み込み中...",
                        style = TextStyle(
                            color = ColorProvider(gray),
                            fontSize = 14.sp
                        )
                    )
                } else {
                    LazyColumn {
                        // 未完了タスク
                        items(incompleteTasks) { task ->
                            TaskRow(
                                task = task,
                                checkmark = "✅",
                                dateIcon = "📅",
                                titleColor = ColorProvider(black),
                                gray = gray,
                                blue = blue
                            )
                        }
                        
                        // 完了タスクがある場合、セパレーターを表示
                        if (completedTasks.isNotEmpty()) {
                            item {
                                Spacer(GlanceModifier.height(8.dp))
                                Text(
                                    text = "── 達成済み ──",
                                    style = TextStyle(
                                        color = ColorProvider(green),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(GlanceModifier.height(4.dp))
                            }
                        }
                        
                        // 完了タスク
                        items(completedTasks) { task ->
                            TaskRow(
                                task = task,
                                checkmark = "✅",
                                dateIcon = "🎉",
                                titleColor = ColorProvider(gray), // 完了は薄い色
                                gray = gray,
                                blue = blue
                            )
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TaskRow(
    task: TodoItem,
    checkmark: String,
    dateIcon: String,
    titleColor: ColorProvider,
    gray: androidx.compose.ui.graphics.Color,
    blue: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$checkmark ",
            style = TextStyle(fontSize = 14.sp),
            modifier = GlanceModifier.padding(top = 2.dp)
        )

        Column {
            Text(
                text = task.title,
                style = TextStyle(
                    color = titleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Row {
                if (task.deadline.isNotEmpty()) {
                    Text(
                        text = "$dateIcon ${task.deadline}",
                        style = TextStyle(
                            color = ColorProvider(gray),
                            fontSize = 10.sp
                        )
                    )
                    Spacer(GlanceModifier.width(8.dp))
                }

                Text(
                    text = "🏷 ${task.genre}",
                    style = TextStyle(
                        color = ColorProvider(blue),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}