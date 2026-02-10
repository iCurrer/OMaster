package com.example.omaster.util

import android.content.Context
import com.example.omaster.model.MasterPreset
import com.example.omaster.model.PresetList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.Locale
import java.util.UUID

/**
 * JSON 工具类
 * 负责从 assets 目录加载和解析预设数据
 */
object JsonUtil {

    private val gson = Gson()
    private var cachedPresets: List<MasterPreset>? = null

    /**
     * 从 assets 目录加载 presets.json 文件
     *
     * @param context 应用上下文
     * @param fileName JSON 文件名，默认为 "presets.json"
     * @return 解析后的预设列表，如果加载失败则返回空列表
     */
    fun loadPresets(context: Context, fileName: String = "presets.json"): List<MasterPreset> {
        // 如果已有缓存，直接返回缓存
        cachedPresets?.let {
            android.util.Log.d("JsonUtil", "Returning cached presets, count: ${it.size}")
            return it
        }

        return try {
            // 打开 assets 目录下的文件
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    // 使用 Gson 解析 JSON 数据
                    val presetListType = object : TypeToken<PresetList>() {}.type
                    val presetList: PresetList? = gson.fromJson(reader, presetListType)

                    // 处理 null 情况
                    if (presetList == null) {
                        android.util.Log.e("JsonUtil", "Failed to parse presets: result is null")
                        return emptyList()
                    }

                    // 处理 presets 为 null 的情况
                    val presets = presetList.presets ?: emptyList()

                    // 为没有 id 的预设生成基于 name 的 ID
                    val processedPresets = presets.mapIndexed { index, preset ->
                        if (preset.id == null) {
                            // 使用 name 生成简洁的 ID
                            val newId = generatePresetId(preset.name, index)
                            android.util.Log.d("JsonUtil", "Generated id for preset: ${preset.name}, id: $newId")
                            preset.copy(id = newId)
                        } else {
                            android.util.Log.d("JsonUtil", "Preset already has id: ${preset.name}, id: ${preset.id}")
                            preset
                        }
                    }

                    // 缓存结果
                    cachedPresets = processedPresets
                    android.util.Log.d("JsonUtil", "Loaded and cached ${processedPresets.size} presets")
                    processedPresets
                }
            }
        } catch (e: IOException) {
            // 文件读取失败时返回空列表
            android.util.Log.e("JsonUtil", "Failed to load presets from assets", e)
            emptyList()
        } catch (e: Exception) {
            // JSON 解析失败时返回空列表
            android.util.Log.e("JsonUtil", "Failed to parse presets JSON", e)
            emptyList()
        }
    }

    /**
     * 基于预设名称生成简洁的 ID
     * 例如："富士胶片" -> "fuji_film", "蓝调时刻" -> "blue_hour"
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
        val baseId = if (truncated.length < 2) "preset_$index" else truncated

        // 6. 添加索引后缀避免重复
        return "${baseId}_$index"
    }

    /**
     * 将预设列表转换为 JSON 字符串
     * 用于调试或导出数据
     *
     * @param presets 预设列表
     * @return JSON 格式的字符串
     */
    fun presetsToJson(presets: List<MasterPreset>): String {
        return gson.toJson(PresetList(presets))
    }
}
