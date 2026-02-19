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

        // 优先检查应用 private files 目录中是否存在远程更新文件
        try {
            val remoteFile = java.io.File(context.filesDir, "presets_remote.json")
            if (remoteFile.exists()) {
                android.util.Log.d("JsonUtil", "Found remote presets file: ${remoteFile.absolutePath}")
                remoteFile.inputStream().use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val presetListType = object : TypeToken<PresetList>() {}.type
                        val presetList: PresetList? = gson.fromJson(reader, presetListType)
                        if (presetList == null) {
                            android.util.Log.e("JsonUtil", "Failed to parse remote presets: result is null")
                            return emptyList()
                        }
                        val presets = presetList.presets ?: emptyList()
                        val processedPresets = presets.mapIndexed { index, preset ->
                            if (preset.id == null) {
                                val newId = generatePresetId(preset.name, index)
                                android.util.Log.d("JsonUtil", "Generated id for preset: ${preset.name}, id: $newId")
                                preset.copy(id = newId)
                            } else {
                                preset
                            }
                        }
                        cachedPresets = processedPresets
                        android.util.Log.d("JsonUtil", "Loaded and cached ${processedPresets.size} presets from remote file")
                        return processedPresets
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("JsonUtil", "Failed to load presets from remote file", e)
        }

        // Fall back to bundled assets
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    // 使用 Gson 解析 JSON 数据
                    val presetListType = object : TypeToken<PresetList>() {}.type
                    val presetList: PresetList? = gson.fromJson(reader, presetListType)

                    if (presetList == null) {
                        android.util.Log.e("JsonUtil", "Failed to parse presets: result is null")
                        return emptyList()
                    }

                    val presets = presetList.presets ?: emptyList()
                    val processedPresets = presets.mapIndexed { index, preset ->
                        if (preset.id == null) {
                            val newId = generatePresetId(preset.name, index)
                            android.util.Log.d("JsonUtil", "Generated id for preset: ${preset.name}, id: $newId")
                            preset.copy(id = newId)
                        } else {
                            android.util.Log.d("JsonUtil", "Preset already has id: ${preset.name}, id: ${preset.id}")
                            preset
                        }
                    }

                    cachedPresets = processedPresets
                    android.util.Log.d("JsonUtil", "Loaded and cached ${processedPresets.size} presets")
                    processedPresets
                }
            }
        } catch (e: IOException) {
            android.util.Log.e("JsonUtil", "Failed to load presets from assets", e)
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("JsonUtil", "Failed to parse presets JSON", e)
            emptyList()
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
        return gson.toJson(PresetList(presets))
    }
    /**
     * Clear in-memory cache so subsequent calls will re-read remote or asset files.
     * Call this after remote presets file is updated.
     */
    fun invalidateCache() {
        cachedPresets = null
        android.util.Log.d("JsonUtil", "Cache invalidated")
    }

}
