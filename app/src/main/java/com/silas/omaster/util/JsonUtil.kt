package com.silas.omaster.util

import android.content.Context
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.model.PresetList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.Locale

/**
 * 【内置预设加载工具类 - App 更新时会重新加载】
 * JSON 工具类 - 负责从 assets 目录加载和解析预设数据
 * 
 * 【重要区别】
 * 此文件管理的是内置预设（随 App 一起打包的数据）
 * 与 CustomPresetManager 管理的用户数据完全不同
 * 
 * 【App 更新行为】
 * - App 更新时，assets/presets.json 会被新版本覆盖
 * - 这是正常的，因为内置预设应该随 App 更新而更新
 * - 用户数据（SharedPreferences）完全不受影响
 * 
 * 【数据流向】
 * assets/presets.json -> JsonUtil.loadPresets() -> PresetRepository -> UI 展示
 */
object JsonUtil {

    private val gson = Gson()
    
    /**
     * 【内存缓存】
     * 缓存已加载的预设列表，避免重复解析 JSON
     * 注意：App 重启后缓存会清空，需要重新加载
     */
    private var cachedPresets: List<MasterPreset>? = null

    /**
     * 当前加载的预设版本
     * 默认为 2 (当前版本)
     */
    var currentPresetsVersion: Int = 2
        private set

    /**
     * 【内置预设加载方法】
     * 从 assets 目录加载 presets.json 文件
     * 
     * 【关键说明】
     * 1. 文件位置：app/src/main/assets/presets.json
     * 2. 此文件随 App 打包，用户无法修改
     * 3. App 更新时，此文件会被新版本覆盖
     * 4. 使用缓存避免重复解析
     * 
     * @param context 应用上下文
     * @param fileName JSON 文件名，默认为 "presets.json"
     * @return 解析后的预设列表，如果加载失败则返回空列表
     */
    fun loadPresets(context: Context, fileName: String = "presets.json"): List<MasterPreset> {
        // 如果已有缓存，直接返回缓存（性能优化）
        cachedPresets?.let {
            android.util.Log.d("JsonUtil", "Returning cached presets, count: ${it.size}")
            return it
        }

        // 特殊逻辑：检查是否存在旧版的远程更新文件（presets_remote.json）
        // 如果存在，说明用户是从旧版本升级上来的，需要提示迁移
        val oldRemoteFile = java.io.File(context.filesDir, "presets_remote.json")
        if (oldRemoteFile.exists()) {
            android.util.Log.d("JsonUtil", "Old remote presets file detected, triggering migration")
            currentPresetsVersion = 1
        } else {
            // 如果不存在旧文件，默认设为当前最新版本
            currentPresetsVersion = 2
        }

        val allPresets = mutableListOf<MasterPreset>()
        
        val subManager = com.silas.omaster.data.local.SubscriptionManager.getInstance(context)
        val subscriptions = subManager.subscriptionsFlow.value
        val defaultSub = subscriptions.find { it.url == UpdateConfigManager.DEFAULT_PRESET_URL }
        val isDefaultEnabled = defaultSub?.isEnabled ?: true // 如果找不到默认订阅（理论上不会），默认开启

        // 1. 加载内置资产预设 (Assets) - 仅当默认订阅开启时加载
        if (isDefaultEnabled) {
            try {
                context.assets.open(fileName).use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val presetListType = object : TypeToken<PresetList>() {}.type
                        val presetList: PresetList? = gson.fromJson(reader, presetListType)
                        if (presetList != null) {
                            currentPresetsVersion = presetList.version
                            val processed = processPresets(presetList.presets ?: emptyList(), "asset")
                            allPresets.addAll(processed)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("JsonUtil", "Failed to load presets from assets", e)
            }
        }

        // 2. 加载所有开启的订阅预设 (排除 assets 已经加载的情况，如果是同一个 URL)
        try {
            val enabledSubs = subscriptions.filter { it.isEnabled }
            
            for (sub in enabledSubs) {
                // 如果是默认订阅，且我们已经加载了 assets，则优先加载本地下载的订阅文件（如果有）来覆盖 assets
                // 但为了简单起见，目前的逻辑是 assets 始终加载，如果订阅文件存在则重复加载可能会导致重复
                // 这里的逻辑改为：如果订阅文件存在，则加载订阅文件；如果订阅文件不存在且是默认订阅，则加载 assets
                
                // 检查是否存在下载的订阅文件
                val subFile = java.io.File(context.filesDir, subManager.getFileNameForUrl(sub.url))
                if (subFile.exists()) {
                    // 如果存在订阅文件，加载它
                    subFile.inputStream().use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            val presetListType = object : TypeToken<PresetList>() {}.type
                            val presetList: PresetList? = gson.fromJson(reader, presetListType)
                            if (presetList != null) {
                                val processed = processPresets(presetList.presets ?: emptyList(), sub.url)
                                // 如果是默认订阅，我们可能需要替换掉 assets 中的内容，或者干脆不加 assets
                                // 这里为了简单，如果加载了下载的默认订阅，我们就清空之前加载的 assets
                                if (sub.url == UpdateConfigManager.DEFAULT_PRESET_URL) {
                                    allPresets.clear() 
                                    currentPresetsVersion = presetList.version
                                }
                                allPresets.addAll(processed)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("JsonUtil", "Failed to load presets from subscriptions", e)
        }

        // 如果没有任何预设，返回空
        if (allPresets.isEmpty()) return emptyList()

        cachedPresets = allPresets
        android.util.Log.d("JsonUtil", "Total presets loaded: ${allPresets.size}")
        return allPresets
    }

    private fun processPresets(presets: List<MasterPreset>, sourceId: String): List<MasterPreset> {
        return presets.mapIndexed { index, preset ->
            if (preset.id == null) {
                // 如果没有 ID，基于来源和索引生成
                val newId = generatePresetId("${sourceId}_${preset.name}", index)
                preset.copy(id = newId)
            } else {
                // 如果有 ID，为了避免不同订阅间的冲突，可以加个前缀（如果是远程订阅）
                if (sourceId != "asset") {
                    preset.copy(id = "sub_${sourceId.hashCode().toString(16)}_${preset.id}")
                } else {
                    preset
                }
            }
        }
    }

    /**
     * 【ID 生成算法】
     * 基于预设名称生成简洁的 ID
     * 例如："富士胶片" -> "fuji_film_0", "蓝调时刻" -> "blue_hour_1"
     * 
     * 【算法步骤】
     * 1. 移除音调符号（拼音化）
     * 2. 转换为小写
     * 3. 替换非字母数字字符为下划线
     * 4. 限制长度
     * 5. 添加索引后缀避免重复
     * 
     * @param name 预设名称
     * @param index 索引（用于处理重复名称）
     * @return 生成的 ID
     */
    private fun generatePresetId(name: String, index: Int): String {
        // 1. 移除音调符号（拼音化）
        val normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

        // 2. 转换为小写
        val lowerCase = normalized.lowercase(Locale.getDefault())

        // 3. 替换非字母数字字符为下划线
        val cleaned = lowerCase.replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')  // 移除首尾下划线
            .replace(Regex("_+"), "_")  // 多个下划线合并为一个

        // 4. 限制长度
        val truncated = if (cleaned.length > 30) cleaned.substring(0, 30) else cleaned

        // 5. 如果为空或太短，使用索引
        val baseId = if (cleaned.length < 2) "preset_$index" else truncated

        // 6. 添加索引后缀避免重复
        return "${baseId}_$index"
    }

    /**
     * 【调试工具方法】
     * 将预设列表转换为 JSON 字符串
     * 用于调试或导出数据
     * 
     * @param presets 预设列表
     * @return JSON 格式的字符串
     */
    fun presetsToJson(presets: List<MasterPreset>): String {
        return gson.toJson(PresetList(version = currentPresetsVersion, presets = presets))
    }
    /**
     * Clear in-memory cache so subsequent calls will re-read remote or asset files.
     * Call this after remote presets file is updated.
     */
    fun invalidateCache() {
        cachedPresets = null
        android.util.Log.d("JsonUtil", "Cache invalidated")
    }

    /**
     * 删除远程预设文件（用于数据迁移）
     */
    fun deleteRemotePresets(context: Context) {
        try {
            val remoteFile = java.io.File(context.filesDir, "presets_remote.json")
            if (remoteFile.exists()) {
                remoteFile.delete()
                android.util.Log.d("JsonUtil", "Deleted remote presets file for migration")
            }
            invalidateCache()
        } catch (e: Exception) {
            android.util.Log.e("JsonUtil", "Failed to delete remote presets file", e)
        }
    }
}
