package com.yutom7.notionbucketlist

data class TodoItem(
    val id: String,
    val title: String,
    val deadline: String,
    val genre: String // ★ 受け皿を追加
)