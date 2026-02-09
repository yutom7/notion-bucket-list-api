package com.yutom7.notionbucketlist

data class TodoItem(
    val id: String,
    val title: String,
    val deadline: String,
    val genre: String,
    val isCompleted: Boolean = false // 完了フラグを追加
)