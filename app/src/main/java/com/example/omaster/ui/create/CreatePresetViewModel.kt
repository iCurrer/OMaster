package com.example.omaster.ui.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.omaster.data.repository.PresetRepository
import com.example.omaster.model.MasterPreset
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * 新建预设 ViewModel
 * 处理预设创建和图片保存
 */
class CreatePresetViewModel(
    private val context: Context,
    private val repository: PresetRepository
) : ViewModel() {

    /**
     * 创建新预设
     * @return 成功返回 true，失败返回 false
     */
    fun createPreset(
        name: String,
        imageUri: Uri,
        mode: String,
        filter: String,
        softLight: String,
        tone: Int,
        saturation: Int,
        warmCool: Int,
        cyanMagenta: Int,
        sharpness: Int,
        vignette: String,
        exposure: Float? = null,
        colorTemperature: Float? = null,
        colorHue: Float? = null
    ): Boolean {
        return try {
            // 保存图片到内部存储
            val coverPath = saveImageToInternalStorage(imageUri)

            // 创建预设对象
            val preset = MasterPreset(
                id = UUID.randomUUID().toString(),
                name = name,
                coverPath = coverPath,
                galleryImages = null,
                author = "@用户自定义",
                mode = mode,
                filter = filter,
                whiteBalance = null,
                colorTone = null,
                exposureCompensation = exposure?.let { String.format("%.1f", it) },
                colorTemperature = colorTemperature?.toInt(),
                colorHue = colorHue?.toInt(),
                softLight = softLight,
                tone = tone,
                saturation = saturation,
                warmCool = warmCool,
                cyanMagenta = cyanMagenta,
                sharpness = sharpness,
                vignette = vignette,
                isFavorite = false,
                isCustom = true
            )

            // 保存到仓库
            repository.addCustomPreset(preset)
            true
        } catch (e: Exception) {
            android.util.Log.e("CreatePresetViewModel", "创建预设失败", e)
            false
        }
    }

    /**
     * 将图片保存到内部存储
     * @param uri 图片 URI
     * @return 保存后的相对路径
     * @throws IOException 保存失败时抛出
     */
    @Throws(IOException::class)
    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("无法打开图片文件: $uri")

        val fileName = "custom_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, "presets/$fileName")

        // 确保目录存在
        file.parentFile?.mkdirs()

        // 使用 try-finally 确保资源释放
        try {
            FileOutputStream(file).use { outputStream ->
                inputStream.use { input ->
                    input.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            // 保存失败时删除可能创建的不完整文件
            if (file.exists()) {
                file.delete()
            }
            throw IOException("保存图片失败: ${e.message}", e)
        }

        // 验证文件是否成功保存
        if (!file.exists() || file.length() == 0L) {
            throw IOException("图片文件保存失败或为空")
        }

        android.util.Log.d("CreatePresetViewModel", "图片保存成功: ${file.absolutePath}, 大小: ${file.length()} bytes")

        // 返回相对路径
        return "presets/$fileName"
    }
}

/**
 * CreatePresetViewModel 工厂
 */
class CreatePresetViewModelFactory(
    private val context: Context,
    private val repository: PresetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePresetViewModel::class.java)) {
            return CreatePresetViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
