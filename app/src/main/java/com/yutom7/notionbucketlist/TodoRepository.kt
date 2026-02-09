package com.yutom7.notionbucketlist

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SharedPreferences を使ったタスクのキャッシュ管理
 */
class TodoRepository(context: Context) {

    private val prefs = context.getSharedPreferences("todo_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TASKS = "cached_tasks"
        private const val KEY_LAST_SYNC = "last_sync_time"
    }

    /**
     * タスクリストをキャッシュに保存
     */
    fun saveTasks(tasks: List<TodoItem>) {
        val json = gson.toJson(tasks)
        prefs.edit()
            .putString(KEY_TASKS, json)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }

    /**
     * キャッシュからタスクリストを読み込み
     */
    fun loadTasks(): List<TodoItem> {
        val json = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<TodoItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 最終同期時刻を取得（ミリ秒）
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0)
    }

    /**
     * キャッシュが存在するかどうか
     */
    fun hasCachedData(): Boolean {
        return prefs.contains(KEY_TASKS)
    }
}
