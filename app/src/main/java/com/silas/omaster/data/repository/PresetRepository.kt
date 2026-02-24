package com.silas.omaster.data.repository

import android.content.Context
import com.silas.omaster.data.local.CustomPresetManager
import com.silas.omaster.data.local.FavoriteManager
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.util.JsonUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 【数据仓库层 - 统一数据访问入口】
 * 预设数据仓库 - 统一管理默认预设、自定义预设和收藏数据
 * 
 * 【架构说明】
 * 此仓库作为数据访问的统一入口，协调三个数据源：
 * 1. 内置预设（assets/presets.json）- 随 App 更新
 * 2. 自定义预设（SharedPreferences）- 用户数据，App 更新保留
 * 3. 收藏数据（SharedPreferences）- 用户数据，App 更新保留
 * 
 * 【数据流向】
 * UI -> PresetRepository -> [CustomPresetManager/FavoriteManager/JsonUtil] -> 存储
 * 
 * 【重要】此文件本身不直接存储数据，只是协调者
 * 真正的数据持久化在 CustomPresetManager 和 FavoriteManager 中
 */
class PresetRepository(
    context: Context
) {
    /**
     * 【用户数据管理器 - App 更新时保留】
     * 收藏数据管理器
     * 位置：SharedPreferences (omaster_prefs.xml)
     */
    private val favoriteManager = FavoriteManager.getInstance(context)
    
    /**
     * 【用户数据管理器 - App 更新时保留】
     * 自定义预设管理器
     * 位置：SharedPreferences (omaster_custom_presets.xml)
     */
    private val customPresetManager = CustomPresetManager.getInstance(context)
    
    /**
     * 【应用上下文】
     * 使用 applicationContext 避免内存泄漏
     */
    private val appContext = context.applicationContext

    private val subscriptionManager = com.silas.omaster.data.local.SubscriptionManager.getInstance(appContext)

    // 缓存默认预设（内存缓存，App 重启后清空）
    private val _defaultPresets = MutableStateFlow<List<MasterPreset>>(emptyList())
    private val defaultPresetsLoaded = MutableStateFlow(false)

    init {
        // 初始化时加载默认预设
        loadDefaultPresets()
        
        // 监听订阅状态变化，自动重新加载预设
        // 注意：这里使用 Flow 的特性，只有当订阅列表内容真正发生变化时才触发重载
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            subscriptionManager.subscriptionsFlow.collect { subs ->
                android.util.Log.d("PresetRepository", "Subscription status changed, reloading...")
                reloadDefaultPresets()
            }
        }
    }

    /**
     * 【内置预设加载】
     * 从 assets 加载内置预设
     * 这些数据随 App 打包，更新时会被覆盖
     */
    private fun loadDefaultPresets() {
        val presets = JsonUtil.loadPresets(appContext)
        _defaultPresets.value = presets
        defaultPresetsLoaded.value = true
        android.util.Log.d("PresetRepository", "Loaded ${presets.size} default presets")
    }

    /**
     * 重新从 JsonUtil（会优先读取远程保存文件）加载内置预设
     */
    fun reloadDefaultPresets() {
        android.util.Log.d("PresetRepository", "Reloading default presets from JsonUtil")
        JsonUtil.invalidateCache() // 必须先清除 JsonUtil 的内存缓存
        val presets = JsonUtil.loadPresets(appContext)
        _defaultPresets.value = presets
        android.util.Log.d("PresetRepository", "Reloaded ${presets.size} default presets")
    }

    /**
     * 【数据合并方法】
     * 获取所有预设（默认 + 自定义），并标记收藏状态
     * 
     * 【合并逻辑】
     * 1. 内置预设 + 自定义预设 = 全部预设
     * 2. 根据收藏 ID 列表标记每个预设的 isFavorite 状态
     * 3. 【新增】新预设（isNew=true）置顶排序
     * 
     * 【使用场景】
     * 首页 "全部" Tab 使用此数据源
     */
    fun getAllPresets(): Flow<List<MasterPreset>> = combine(
        _defaultPresets,
        customPresetManager.customPresetsFlow,
        favoriteManager.favoritesFlow
    ) { defaultPresets, customPresets, favorites ->
        val allPresets = defaultPresets + customPresets
        allPresets
            .map { preset ->
                preset.copy(isFavorite = preset.id?.let { it in favorites } ?: false)
            }
            .sortedByDescending { it.isNew }  // 新预设置顶，收藏不影响排序
    }

    /**
     * 获取默认预设（从缓存加载）
     * 仅返回内置预设，不包含用户自定义预设
     */
    fun getDefaultPresets(): Flow<List<MasterPreset>> = _defaultPresets

    /**
     * 【用户数据查询】
     * 获取自定义预设
     * 
     * 【数据来源】
     * 从 CustomPresetManager 获取，数据存储在 SharedPreferences
     * App 更新时这些数据会保留
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
     * 从全部预设中筛选出 isFavorite = true 的预设
     */
    fun getFavoritePresets(): Flow<List<MasterPreset>> = combine(
        getAllPresets(),
        favoriteManager.favoritesFlow
    ) { allPresets, favorites ->
        allPresets.filter { it.id?.let { id -> id in favorites } ?: false }
    }

    /**
     * 【数据查询方法】
     * 根据 ID 获取预设
     * 
     * 【查询顺序】
     * 1. 先查找默认预设（内置）
     * 2. 再查找自定义预设（用户数据）
     * 
     * 【重要】
     * 自定义预设的 ID 是 UUID 格式（如 550e8400-e29b-41d4-a716-446655440000）
     * 内置预设的 ID 是基于名称生成的（如 fuji_film_0）
     * 两者不会冲突
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
     * 先查找默认预设，再查找自定义预设
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
     * 【用户数据写入】
     * 切换收藏状态
     * 数据会保存到 FavoriteManager（SharedPreferences）
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
     * 【用户数据写入】
     * 添加自定义预设
     * 数据会保存到 CustomPresetManager（SharedPreferences）
     * App 更新时这些数据会保留
     */
    fun addCustomPreset(preset: MasterPreset) {
        customPresetManager.addCustomPreset(preset)
    }

    /**
     * 【用户数据写入】
     * 更新自定义预设
     * 通过 preset.id 匹配现有数据并更新
     * App 更新时这些数据会保留
     */
    fun updateCustomPreset(preset: MasterPreset) {
        customPresetManager.updateCustomPreset(preset)
    }

    /**
     * 【用户数据删除】
     * 删除自定义预设
     * 
     * 【级联删除】
     * 1. 删除 CustomPresetManager 中的数据
     * 2. 同时从 FavoriteManager 中移除对应的收藏记录
     * 3. 删除内部存储中的图片文件
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

        /**
         * 单例获取方法
         * 使用 applicationContext 避免内存泄漏
         */
        fun getInstance(context: Context): PresetRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PresetRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
