package com.silas.omaster.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 收藏管理器
 * 使用 SharedPreferences 存储收藏的预设 ID
 */
class FavoriteManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _favoritesFlow: MutableStateFlow<Set<String>>
    val favoritesFlow: StateFlow<Set<String>>

    init {
        // 必须使用 HashSet 创建副本，因为 getStringSet 返回的是原始引用
        val initialFavorites = HashSet(prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet())
        android.util.Log.d("FavoriteManager", "Init loaded ${initialFavorites.size} favorites: $initialFavorites")
        _favoritesFlow = MutableStateFlow(initialFavorites)
        favoritesFlow = _favoritesFlow.asStateFlow()
    }

    /**
     * 获取所有收藏的预设 ID
     */
    fun getFavorites(): Set<String> {
        return _favoritesFlow.value
    }

    /**
     * 检查预设是否已收藏
     */
    fun isFavorite(presetId: String): Boolean {
        return presetId in getFavorites()
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(presetId: String): Boolean {
        val favorites = getFavorites().toMutableSet()
        val isNowFavorite = if (presetId in favorites) {
            favorites.remove(presetId)
            false
        } else {
            favorites.add(presetId)
            true
        }
        android.util.Log.d("FavoriteManager", "Toggle $presetId -> $isNowFavorite, saving ${favorites.size} favorites")
        saveFavorites(favorites)
        return isNowFavorite
    }

    /**
     * 添加收藏
     */
    fun addFavorite(presetId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(presetId)
        saveFavorites(favorites)
    }

    /**
     * 移除收藏
     */
    fun removeFavorite(presetId: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(presetId)
        saveFavorites(favorites)
    }

    /**
     * 清空所有收藏
     */
    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
        _favoritesFlow.value = emptySet()
    }

    private fun saveFavorites(favorites: Set<String>) {
        // 创建新的 HashSet 保存，避免引用问题
        val newSet = HashSet(favorites)
        android.util.Log.d("FavoriteManager", "Saving to SharedPreferences: $newSet")
        // 使用 commit() 同步保存，确保数据立即写入
        val success = prefs.edit().putStringSet(KEY_FAVORITES, newSet).commit()
        android.util.Log.d("FavoriteManager", "Commit result: $success")
        _favoritesFlow.value = newSet
        
        // 验证保存结果
        val saved = prefs.getStringSet(KEY_FAVORITES, emptySet())
        android.util.Log.d("FavoriteManager", "Verified saved to SharedPreferences: $saved")
    }

    companion object {
        private const val PREFS_NAME = "omaster_prefs"
        private const val KEY_FAVORITES = "favorite_presets"

        @Volatile
        private var INSTANCE: FavoriteManager? = null

        fun getInstance(context: Context): FavoriteManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoriteManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
