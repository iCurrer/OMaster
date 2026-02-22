package com.silas.omaster.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.silas.omaster.R
import com.silas.omaster.util.PresetI18n
import com.silas.omaster.util.formatSigned
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PresetItem(
    val label: String,
    val value: String,
    val span: Int = 1 // 1: half width, 2: full width
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeString(value)
        parcel.writeInt(span)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PresetItem> {
        override fun createFromParcel(parcel: Parcel): PresetItem {
            return PresetItem(parcel)
        }

        override fun newArray(size: Int): Array<PresetItem?> {
            return arrayOfNulls(size)
        }
    }
}

@Serializable
data class PresetSection(
    val title: String? = null,
    val items: List<PresetItem>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.createTypedArrayList(PresetItem.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeTypedList(items)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PresetSection> {
        override fun createFromParcel(parcel: Parcel): PresetSection {
            return PresetSection(parcel)
        }

        override fun newArray(size: Int): Array<PresetSection?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * 大师模式调色预设数据类
 * 基于一加/OPPO/Realme 大师模式的专业摄影参数
 *
 * @param id 唯一标识符
 * @param name 预设名称
 * @param coverPath 封面图片路径
 * @param galleryImages 详情页图库图片路径列表，用于展示多张样片
 * @param author 滤镜作者
 * @param mode 模式，"auto" 或 "pro"
 * @param filter 滤镜类型，如 "原图", "胶片", "黑白" 等
 * @param whiteBalance 白平衡，字符串如 "2000K", "阴天", "日光" 等，仅在 pro 模式下有效
 * @param colorTone 色调，如 "暖调", "冷调" 或 "+5", "-3" 等，仅在 pro 模式下有效
 * @param exposureCompensation 曝光补偿，如 "-1.0", "+0.7" 或数字，仅在 pro 模式下有效
 * @param colorTemperature 色温数值，范围 2000-8000，仅在 pro 模式下有效
 * @param colorHue 色调数值，范围 -150 到 150，仅在 pro 模式下有效
 * @param iso ISO 感光度，如 "100", "200-400"，仅在 pro 模式下有效
 * @param shutterSpeed 快门速度，如 "1/125", "1/60"，仅在 pro 模式下有效
 * @param softLight 柔光强度，数字 0-100 或文字如 "梦幻"
 * @param tone 影调，范围 -100 到 +100，控制整体明暗对比
 * @param saturation 饱和度，范围 -100 到 +100
 * @param warmCool 冷暖色调，范围 -100 到 +100，负值偏冷，正值偏暖
 * @param cyanMagenta 青品色调，范围 -100 到 +100，负值偏青，正值偏品红
 * @param sharpness 锐度，数字 0-100
 * @param vignette 暗角开关，"开" 或 "关"
 * @param isNew 是否为新预设，用于显示 NEW 标签和置顶（手动控制）
 * @param shootingTips 拍摄建议，包含环境及场景建议（已废弃，仅用于兼容旧版本自定义预设）
 * @param sections 动态参数分组列表，用于替代硬编码的参数显示
 */
@Serializable
data class MasterPreset(
    val id: String? = null,
    val name: String,
    val coverPath: String,
    val galleryImages: List<String>? = null,
    val author: String = "@OPPO影像",
    val mode: String,
    val filter: String,
    val whiteBalance: String?,
    val colorTone: String?,
    val exposureCompensation: String?,
    val colorTemperature: Int? = null,
    val colorHue: Int? = null,
    val iso: String? = null,
    val shutterSpeed: String? = null,
    val softLight: String,
    val tone: Int,
    val saturation: Int,
    val warmCool: Int,
    val cyanMagenta: Int,
    val sharpness: Int,
    val vignette: String,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val isNew: Boolean = false,
    val shootingTips: String? = null,
    val sections: List<PresetSection>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString(),
        name = parcel.readString() ?: "",
        coverPath = parcel.readString() ?: "",
        galleryImages = parcel.createStringArrayList(),
        author = parcel.readString() ?: "@OPPO影像",
        mode = parcel.readString() ?: "auto",
        filter = parcel.readString() ?: "原图",
        whiteBalance = parcel.readString(),
        colorTone = parcel.readString(),
        exposureCompensation = parcel.readString(),
        colorTemperature = parcel.readValue(Int::class.java.classLoader) as? Int,
        colorHue = parcel.readValue(Int::class.java.classLoader) as? Int,
        iso = parcel.readString(),
        shutterSpeed = parcel.readString(),
        softLight = parcel.readString() ?: "无",
        tone = parcel.readInt(),
        saturation = parcel.readInt(),
        warmCool = parcel.readInt(),
        cyanMagenta = parcel.readInt(),
        sharpness = parcel.readInt(),
        vignette = parcel.readString() ?: "关",
        isFavorite = parcel.readByte() != 0.toByte(),
        isCustom = parcel.readByte() != 0.toByte(),
        isNew = parcel.readByte() != 0.toByte(),
        shootingTips = parcel.readString(),
        sections = parcel.createTypedArrayList(PresetSection.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(coverPath)
        parcel.writeStringList(galleryImages)
        parcel.writeString(author)
        parcel.writeString(mode)
        parcel.writeString(filter)
        parcel.writeString(whiteBalance)
        parcel.writeString(colorTone)
        parcel.writeString(exposureCompensation)
        parcel.writeValue(colorTemperature)
        parcel.writeValue(colorHue)
        parcel.writeString(iso)
        parcel.writeString(shutterSpeed)
        parcel.writeString(softLight)
        parcel.writeInt(tone)
        parcel.writeInt(saturation)
        parcel.writeInt(warmCool)
        parcel.writeInt(cyanMagenta)
        parcel.writeInt(sharpness)
        parcel.writeString(vignette)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeByte(if (isCustom) 1 else 0)
        parcel.writeByte(if (isNew) 1 else 0)
        parcel.writeString(shootingTips)
        parcel.writeTypedList(sections)
    }

    fun getDisplaySections(context: Context): List<PresetSection> {
        if (!sections.isNullOrEmpty()) {
            // 检查 sections 中是否包含 shootingTips（兼容旧版自定义预设）
            val hasTips = sections.any { section ->
                section.items.any { it.label == "@string/shooting_tips" }
            }

            // 如果没有包含且 shootingTips 字段有值，则追加
            if (!hasTips && !shootingTips.isNullOrEmpty()) {
                val tipsItem = PresetItem(
                    label = "@string/shooting_tips",
                    value = shootingTips,
                    span = 2
                )
                // 创建新列表以避免修改不可变列表
                val newSections = sections.toMutableList()
                newSections.add(PresetSection(items = listOf(tipsItem)))
                return newSections
            }

            return sections
        }

        // 兼容旧版硬编码逻辑，动态生成 sections
        val generatedSections = mutableListOf<PresetSection>()

        // 1. Pro 模式参数
        if (isProMode) {
            val proItems = mutableListOf<PresetItem>()
            
            iso?.let {
                proItems.add(PresetItem(context.getString(R.string.param_iso), it, 1))
            }
            shutterSpeed?.let {
                proItems.add(PresetItem(context.getString(R.string.param_shutter), it, 1))
            }
            exposureCompensation?.let {
                proItems.add(PresetItem(context.getString(R.string.param_exposure), it, 1))
            }
            
            // 色温/白平衡
            if (colorTemperature != null) {
                proItems.add(PresetItem(context.getString(R.string.param_color_temp), "${colorTemperature}K", 1))
            } else if (whiteBalance != null) {
                proItems.add(PresetItem(context.getString(R.string.param_white_balance), whiteBalance, 1))
            }
            
            // 色调
            if (colorHue != null) {
                proItems.add(PresetItem(context.getString(R.string.param_tone), colorHue.formatSigned(), 1))
            } else if (colorTone != null) {
                proItems.add(PresetItem(context.getString(R.string.param_tone_style), colorTone, 1))
            }

            if (proItems.isNotEmpty()) {
                generatedSections.add(PresetSection(context.getString(R.string.param_pro_adjust), proItems))
            }
        }

        // 2. 调色参数
        val colorItems = mutableListOf<PresetItem>()
        
        // 滤镜 (独占一行)
        colorItems.add(PresetItem(
            context.getString(R.string.param_filter), 
            PresetI18n.getLocalizedFilter(context, filter), 
            2
        ))

        // 柔光 & 影调
        colorItems.add(PresetItem(
            context.getString(R.string.param_soft_light), 
            PresetI18n.getLocalizedSoftLight(context, softLight), 
            1
        ))
        colorItems.add(PresetItem(
            context.getString(R.string.param_tone_curve), 
            tone.formatSigned(), 
            1
        ))

        // 饱和度 & 冷暖
        colorItems.add(PresetItem(
            context.getString(R.string.param_saturation), 
            saturation.formatSigned(), 
            1
        ))
        colorItems.add(PresetItem(
            context.getString(R.string.param_warm_cool), 
            warmCool.formatSigned(), 
            1
        ))

        // 青品 & 锐度
        colorItems.add(PresetItem(
            context.getString(R.string.param_cyan_magenta), 
            cyanMagenta.formatSigned(), 
            1
        ))
        colorItems.add(PresetItem(
            context.getString(R.string.param_sharpness), 
            "$sharpness", 
            1
        ))

        // 暗角 (独占一行)
        colorItems.add(PresetItem(
            context.getString(R.string.param_vignette), 
            PresetI18n.getLocalizedVignette(context, vignette), 
            2
        ))

        generatedSections.add(PresetSection(context.getString(R.string.section_color_grading), colorItems))

        // 3. 拍摄建议 (兼容旧版)
        if (!shootingTips.isNullOrEmpty()) {
            generatedSections.add(PresetSection(items = listOf(
                PresetItem(
                    label = "@string/shooting_tips",
                    value = shootingTips,
                    span = 2
                )
            )))
        }

        return generatedSections
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MasterPreset> {
        override fun createFromParcel(parcel: Parcel): MasterPreset {
            return MasterPreset(parcel)
        }

        override fun newArray(size: Int): Array<MasterPreset?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * 是否为 Pro 模式
     */
    val isProMode: Boolean get() = mode.lowercase() == "pro"

    /**
     * 是否为 Auto 模式
     */
    val isAutoMode: Boolean get() = mode.lowercase() == "auto"

    /**
     * 获取所有展示图片（封面 + 图库）
     */
    val allImages: List<String>
        get() {
            val gallery = galleryImages ?: emptyList()
            return if (gallery.isEmpty()) {
                listOf(coverPath)
            } else {
                listOf(coverPath) + gallery
            }
        }
}

/**
 * 预设列表包装类
 * 用于 Gson 解析 JSON 数据
 */
@Serializable
data class PresetList(
    val presets: List<MasterPreset> = emptyList()
)
