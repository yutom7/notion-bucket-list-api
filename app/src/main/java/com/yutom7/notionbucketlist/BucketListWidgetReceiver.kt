package com.yutom7.notionbucketlist

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class BucketListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BucketListWidget()
}