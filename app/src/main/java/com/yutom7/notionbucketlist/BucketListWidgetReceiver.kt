package com.yutom7.notionbucketlist

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class BucketListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BucketListWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // ウィジェットが追加されたら、定期同期をスケジュール
        TodoSyncWorker.enqueuePeriodicSync(context)
        // 即時同期も実行して初回表示を高速化
        TodoSyncWorker.enqueueOneTimeSync(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // ウィジェットが更新されるたびに同期をスケジュール（アプリ更新後も確実に動作）
        TodoSyncWorker.enqueuePeriodicSync(context)
        
        // キャッシュが空なら即時同期
        val repository = TodoRepository(context)
        if (!repository.hasCachedData()) {
            TodoSyncWorker.enqueueOneTimeSync(context)
        }
    }
}