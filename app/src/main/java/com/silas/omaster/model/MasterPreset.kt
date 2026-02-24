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
data class PresetDescription(
    val title: String,
    val content: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(content)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PresetDescription> {
        override fun createFromParcel(parcel: Parcel): PresetDescription {
            return PresetDescription(parcel)
        }

        override fun newArray(size: Int): Array<PresetDescription?> {
            return arrayOfNulls(size)
        }
    }
}

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
 * @param description 描述信息，包含标题和内容，用于替代 shootingTips
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
    val mode: String? = null,
    val filter: String? = null,
    val whiteBalance: String? = null,
    val colorTone: String? = null,
    val exposureCompensation: String? = null,
    val colorTemperature: Int? = null,
    val colorHue: Int? = null,
    val iso: String? = null,
    val shutterSpeed: String? = null,
    val softLight: String? = null,
    val tone: Int? = null,
    val saturation: Int? = null,
    val warmCool: Int? = null,
    val cyanMagenta: Int? = null,
    val sharpness: Int? = null,
    val vignette: String? = null,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val isNew: Boolean = false,
    val description: PresetDescription? = null,
    val shootingTips: String? = null,
    val sections: List<PresetSection>? = null,
    val tags: List<String>? = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString(),
        name = parcel.readString() ?: "",
        coverPath = parcel.readString() ?: "",
        galleryImages = parcel.createStringArrayList(),
        author = parcel.readString() ?: "@OPPO影像",
        mode = parcel.readString() ?: "auto",
        filter = parcel.readString(),
        whiteBalance = parcel.readString(),
        colorTone = parcel.readString(),
        exposureCompensation = parcel.readString(),
        colorTemperature = parcel.readValue(Int::class.java.classLoader) as? Int,
        colorHue = parcel.readValue(Int::class.java.classLoader) as? Int,
        iso = parcel.readString(),
        shutterSpeed = parcel.readString(),
        softLight = parcel.readString(),
        tone = parcel.readValue(Int::class.java.classLoader) as? Int,
        saturation = parcel.readValue(Int::class.java.classLoader) as? Int,
        warmCool = parcel.readValue(Int::class.java.classLoader) as? Int,
        cyanMagenta = parcel.readValue(Int::class.java.classLoader) as? Int,
        sharpness = parcel.readValue(Int::class.java.classLoader) as? Int,
        vignette = parcel.readString(),
        isFavorite = parcel.readByte() != 0.toByte(),
        isCustom = parcel.readByte() != 0.toByte(),
        isNew = parcel.readByte() != 0.toByte(),
        description = parcel.readParcelable(PresetDescription::class.java.classLoader),
        shootingTips = parcel.readString(),
        sections = parcel.createTypedArrayList(PresetSection.CREATOR),
        tags = parcel.createStringArrayList() ?: emptyList()
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
        parcel.writeValue(tone)
        parcel.writeValue(saturation)
        parcel.writeValue(warmCool)
        parcel.writeValue(cyanMagenta)
        parcel.writeValue(sharpness)
        parcel.writeString(vignette)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeByte(if (isCustom) 1 else 0)
        parcel.writeByte(if (isNew) 1 else 0)
        parcel.writeParcelable(description, flags)
        parcel.writeString(shootingTips)
        parcel.writeTypedList(sections)
        parcel.writeStringList(tags)
    }

    fun getDisplaySections(context: Context): List<PresetSection> {
        if (!sections.isNullOrEmpty()) {
            return sections.map { section ->
                section.copy(
                    title = section.title?.let { PresetI18n.resolveString(context, it) },
                    items = section.items.map { item ->
                        item.copy(
                            label = PresetI18n.resolveString(context, item.label),
                            value = PresetI18n.resolveString(context, item.value)
                        )
                    }
                )
            }
        }

        // 兼容旧版硬编码逻辑，动态生成 sections
        val generatedSections = mutableListOf<PresetSection>()

        // 1. Pro 模式参数
        // 兼容逻辑：如果有 ISO 或快门等参数，自动归类为 Pro 模式参数
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

        // 2. 调色参数
        val colorItems = mutableListOf<PresetItem>()
        
        // 滤镜 (独占一行)
        filter?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_filter), 
                PresetI18n.getLocalizedFilter(context, it), 
                2
            ))
        }

        // 柔光 & 影调
        softLight?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_soft_light), 
                PresetI18n.getLocalizedSoftLight(context, it), 
                1
            ))
        }
        tone?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_tone_curve), 
                it.formatSigned(), 
                1
            ))
        }

        // 饱和度 & 冷暖
        saturation?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_saturation), 
                it.formatSigned(), 
                1
            ))
        }
        warmCool?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_warm_cool), 
                it.formatSigned(), 
                1
            ))
        }

        // 青品 & 锐度
        cyanMagenta?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_cyan_magenta), 
                it.formatSigned(), 
                1
            ))
        }
        sharpness?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_sharpness), 
                "$it", 
                1
            ))
        }

        // 暗角 (独占一行)
        vignette?.let {
            colorItems.add(PresetItem(
                context.getString(R.string.param_vignette), 
                PresetI18n.getLocalizedVignette(context, it), 
                2
            ))
        }

        if (colorItems.isNotEmpty()) {
            generatedSections.add(PresetSection(context.getString(R.string.section_color_grading), colorItems))
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
    val name: String? = null,
    val author: String? = null,
    val build: Int = 1,
    val version: Int = 1, // 兼容旧版
    val presets: List<MasterPreset> = emptyList()
)
