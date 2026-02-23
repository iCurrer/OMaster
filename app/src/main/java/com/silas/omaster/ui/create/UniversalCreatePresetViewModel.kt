package com.silas.omaster.ui.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.model.PresetItem
import com.silas.omaster.model.PresetSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * 通用预设编辑器 ViewModel
 * 支持基于 sections 的灵活配置
 */
class UniversalCreatePresetViewModel(
    private val context: Context,
    private val repository: PresetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversalPresetUiState())
    val uiState: StateFlow<UniversalPresetUiState> = _uiState.asStateFlow()
    
    private var isLoaded = false

    // 加载模版或者现有预设
    fun loadTemplate(presetId: String?) {
        if (isLoaded) return
        isLoaded = true
        
        if (presetId == null) {
            // 从零开始
            _uiState.value = UniversalPresetUiState()
            return
        }

        viewModelScope.launch {
            val preset = repository.getPresetById(presetId)
            if (preset != null) {
                // 如果是旧数据结构，转换为新结构
                val sections = if (preset.sections.isNullOrEmpty()) {
                    convertOldPresetToSections(preset)
                } else {
                    preset.sections
                }
                
                _uiState.value = UniversalPresetUiState(
                    name = if (preset.isCustom) preset.name else "${preset.name} (Copy)",
                    sections = sections,
                    // 如果是自定义预设，保留封面；如果是系统预设，可能需要重新选择或者复制封面（这里简化为重新选择，或者使用默认）
                    // 这里的逻辑是：如果是基于模版创建，我们不复制图片，用户需要上传新图片，或者使用模版的图片（如果是系统预设的图片，可能无法直接引用路径，需要拷贝）
                    // 简单起见，如果是模版，我们重置图片，或者尝试复用。
                    // 考虑到系统预设图片在 assets 中，自定义预设图片在 files 中。
                    // 如果是自定义预设编辑，保留原有图片路径。如果是基于模版新建，置空图片让用户选。
                    // 但是用户体验上，基于模版应该保留模版的所有参数，除了 ID 和 Name。
                    // 图片处理比较复杂，暂时置空，让用户自己传。或者如果用户不传，就不保存 coverPath（但这会导致显示问题）。
                    // 为了简化，我们要求用户上传新图片，或者我们可以提供一个默认占位符。
                    // 这里我们暂时置空 imageUri，要求用户重新上传。
                    // TODO: 优化体验，允许复用图片
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateImageUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun addSection(title: String) {
        val newSection = PresetSection(title = title, items = emptyList())
        val currentSections = _uiState.value.sections.toMutableList()
        currentSections.add(newSection)
        _uiState.value = _uiState.value.copy(sections = currentSections)
    }

    fun removeSection(index: Int) {
        val currentSections = _uiState.value.sections.toMutableList()
        if (index in currentSections.indices) {
            currentSections.removeAt(index)
            _uiState.value = _uiState.value.copy(sections = currentSections)
        }
    }

    fun addItemToSection(sectionIndex: Int, item: PresetItem) {
        val currentSections = _uiState.value.sections.toMutableList()
        if (sectionIndex in currentSections.indices) {
            val section = currentSections[sectionIndex]
            val newItems = section.items.toMutableList()
            newItems.add(item)
            currentSections[sectionIndex] = section.copy(items = newItems)
            _uiState.value = _uiState.value.copy(sections = currentSections)
        }
    }

    fun removeItemFromSection(sectionIndex: Int, itemIndex: Int) {
        val currentSections = _uiState.value.sections.toMutableList()
        if (sectionIndex in currentSections.indices) {
            val section = currentSections[sectionIndex]
            val newItems = section.items.toMutableList()
            if (itemIndex in newItems.indices) {
                newItems.removeAt(itemIndex)
                currentSections[sectionIndex] = section.copy(items = newItems)
                _uiState.value = _uiState.value.copy(sections = currentSections)
            }
        }
    }
    
    fun updateItemInSection(sectionIndex: Int, itemIndex: Int, newItem: PresetItem) {
        val currentSections = _uiState.value.sections.toMutableList()
        if (sectionIndex in currentSections.indices) {
            val section = currentSections[sectionIndex]
            val newItems = section.items.toMutableList()
            if (itemIndex in newItems.indices) {
                newItems[itemIndex] = newItem
                currentSections[sectionIndex] = section.copy(items = newItems)
                _uiState.value = _uiState.value.copy(sections = currentSections)
            }
        }
    }

    fun savePreset(): Boolean {
        val state = _uiState.value
        if (state.name.isBlank()) return false
        // 如果是新建，必须有图；如果是编辑且没改图，可以使用旧图（这里还没处理编辑现有预设的逻辑，假设都是新建）
        // 实际上 loadTemplate 只是加载数据，保存时都是 create new preset currently.
        if (state.imageUri == null) return false 

        return try {
            val coverPath = saveImageToInternalStorage(state.imageUri!!)
            
            val preset = MasterPreset(
                id = UUID.randomUUID().toString(),
                name = state.name,
                coverPath = coverPath,
                author = "@用户自定义",
                sections = state.sections,
                isCustom = true,
                // 其他字段可以置空或者根据 sections 填充（如果需要兼容旧代码显示）
                // 暂时置空，因为新版 UI 应该只依赖 sections
            )
            
            repository.addCustomPreset(preset)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun convertOldPresetToSections(preset: MasterPreset): List<PresetSection> {
        val items = mutableListOf<PresetItem>()
        
        // 尝试从旧字段提取数据
        preset.filter?.let { items.add(PresetItem("滤镜", it, 2)) }
        preset.softLight?.let { items.add(PresetItem("柔光", it, 1)) }
        preset.tone?.let { items.add(PresetItem("影调", it.toString(), 1)) }
        preset.saturation?.let { items.add(PresetItem("饱和度", it.toString(), 1)) }
        preset.warmCool?.let { items.add(PresetItem("冷暖", it.toString(), 1)) }
        preset.cyanMagenta?.let { items.add(PresetItem("青品", it.toString(), 1)) }
        preset.sharpness?.let { items.add(PresetItem("锐度", it.toString(), 1)) }
        preset.vignette?.let { items.add(PresetItem("暗角", it, 2)) }
        
        // Pro 模式参数
        preset.exposureCompensation?.let { items.add(PresetItem("曝光补偿", it, 1)) }
        preset.colorTemperature?.let { items.add(PresetItem("色温", it.toString(), 1)) }
        preset.colorHue?.let { items.add(PresetItem("色调", it.toString(), 1)) }
        
        return listOf(PresetSection("参数配置", items))
    }

    @Throws(IOException::class)
    private fun saveImageToInternalStorage(uri: Uri): String {
        val fileName = "custom_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, "presets/$fileName")
        file.parentFile?.mkdirs()
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("无法打开图片文件")
        
        return "presets/$fileName"
    }
}

data class UniversalPresetUiState(
    val name: String = "",
    val imageUri: Uri? = null,
    val sections: List<PresetSection> = emptyList()
)

class UniversalCreatePresetViewModelFactory(
    private val context: Context,
    private val repository: PresetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniversalCreatePresetViewModel::class.java)) {
            return UniversalCreatePresetViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
