package com.example.omaster.data.repository

import android.content.Context
import com.example.omaster.data.local.CustomPresetManager
import com.example.omaster.data.local.FavoriteManager
import com.example.omaster.model.MasterPreset
import com.example.omaster.util.JsonUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * 预设数据仓库
 * 统一管理默认预设、自定义预设和收藏数据
 */
class PresetRepository(
    context: Context
) {
    private val favoriteManager = FavoriteManager.getInstance(context)
    private val customPresetManager = CustomPresetManager.getInstance(context)
    private val appContext = context.applicationContext

    // 缓存默认预设
    private val _defaultPresets = MutableStateFlow<List<MasterPreset>>(emptyList())
    private val defaultPresetsLoaded = MutableStateFlow(false)

    init {
        // 初始化时加载默认预设
        loadDefaultPresets()
    }

    private fun loadDefaultPresets() {
        val presets = JsonUtil.loadPresets(appContext)
        _defaultPresets.value = presets
        defaultPresetsLoaded.value = true
        android.util.Log.d("PresetRepository", "Loaded ${presets.size} default presets")
    }

    /**
     * 获取所有预设（默认 + 自定义），并标记收藏状态
     */
    fun getAllPresets(): Flow<List<MasterPreset>> = combine(
        _defaultPresets,
        customPresetManager.customPresetsFlow,
        favoriteManager.favoritesFlow
    ) { defaultPresets, customPresets, favorites ->
        val allPresets = defaultPresets + customPresets
        allPresets.map { preset ->
            preset.copy(isFavorite = preset.id?.let { it in favorites } ?: false)
        }
    }

    /**
     * 获取默认预设（从缓存加载）
     */
    fun getDefaultPresets(): Flow<List<MasterPreset>> = _defaultPresets

    /**
     * 获取自定义预设
     */
    fun getCustomPresets(): Flow<List<MasterPreset>> = combine(
        customPresetManager.customPresetsFlow,
        favoriteManager.favoritesFlow
    ) { presets, favorites ->
        presets.map { preset ->
            preset.copy(
                isCustom = true,
                isFavorite = preset.id?.let { it in favorites } ?: false
            )
        }
    }

    /**
     * 获取收藏的预设
     */
    fun getFavoritePresets(): Flow<List<MasterPreset>> = combine(
        getAllPresets(),
        favoriteManager.favoritesFlow
    ) { allPresets, favorites ->
        allPresets.filter { it.id?.let { id -> id in favorites } ?: false }
    }

    /**
     * 根据 ID 获取预设
     */
    suspend fun getPresetById(presetId: String): MasterPreset? {
        // 先查找默认预设
        val defaultPreset = JsonUtil.loadPresets(appContext)
            .find { it.id == presetId }
        if (defaultPreset != null) {
            return defaultPreset.id?.let { id ->
                defaultPreset.copy(isFavorite = favoriteManager.isFavorite(id))
            }
        }

        // 再查找自定义预设
        val customPreset = customPresetManager.getPresetById(presetId)
        return customPreset?.copy(
            isFavorite = favoriteManager.isFavorite(presetId),
            isCustom = true
        )
    }

    /**
     * 根据名称获取预设
     */
    suspend fun getPresetByName(name: String): MasterPreset? {
        // 先查找默认预设
        val defaultPreset = JsonUtil.loadPresets(appContext)
            .find { it.name == name }
        if (defaultPreset != null) {
            return defaultPreset.id?.let { id ->
                defaultPreset.copy(isFavorite = favoriteManager.isFavorite(id))
            }
        }

        // 再查找自定义预设
        val customPreset = customPresetManager.getCustomPresets().find { it.name == name }
        return customPreset?.copy(
            isFavorite = customPreset.id?.let { favoriteManager.isFavorite(it) } ?: false
        )
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(presetId: String): Boolean {
        return favoriteManager.toggleFavorite(presetId)
    }

    /**
     * 检查是否已收藏
     */
    fun isFavorite(presetId: String): Boolean {
        return favoriteManager.isFavorite(presetId)
    }

    /**
     * 添加自定义预设
     */
    fun addCustomPreset(preset: MasterPreset) {
        customPresetManager.addCustomPreset(preset)
    }

    /**
     * 更新自定义预设
     */
    fun updateCustomPreset(preset: MasterPreset) {
        customPresetManager.updateCustomPreset(preset)
    }

    /**
     * 删除自定义预设
     */
    fun deleteCustomPreset(presetId: String) {
        customPresetManager.deleteCustomPreset(appContext, presetId)
        // 同时从收藏中移除
        favoriteManager.removeFavorite(presetId)
    }

    /**
     * 获取收藏数量
     */
    fun getFavoriteCount(): Int {
        return favoriteManager.getFavorites().size
    }

    /**
     * 获取自定义预设数量
     */
    fun getCustomPresetCount(): Int {
        return customPresetManager.getCustomPresets().size
    }

    companion object {
        @Volatile
        private var INSTANCE: PresetRepository? = null

        fun getInstance(context: Context): PresetRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PresetRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
