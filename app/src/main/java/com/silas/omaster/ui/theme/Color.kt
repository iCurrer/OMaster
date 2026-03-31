package com.silas.omaster.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 哈苏品牌色系
 * 哈苏橙是品牌的标志性颜色
 */
val HasselbladOrange = Color(0xFFEC7223)
val HasselbladOrangeDark = Color(0xFFD15F1A)
val HasselbladOrangeLight = Color(0xFFF08A4A)

/**
 * 品牌主题色
 */
val ZeissBlue = Color(0xFF005A9C)
val LeicaRed = Color(0xFFCC0000)
val RicohGreen = Color(0xFF00A95C)
val FujifilmGreen = Color(0xFF009B3A)
val CanonRed = Color(0xFFCC0000)
val NikonYellow = Color(0xFFFFC20E)
val SonyOrange = Color(0xFFF15A24)
val PhaseOneGrey = Color(0xFF5A5A5A)

/**
 * 纯黑背景系列
 * 用于深色模式的主背景
 */
val PureBlack = Color(0xFF000000)
val NearBlack = Color(0xFF0A0A0A)
val DarkGray = Color(0xFF2D2D2D)  // 从 #1A1A1A 调亮，阳光下更清晰
val MediumGray = Color(0xFF333333)
val LightGray = Color(0xFF999999)
val OffWhite = Color(0xFFF5F5F5)

/**
 * 功能色
 */
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFE53935)
val WarningYellow = Color(0xFFFFB300)

/**
 * UI 扩展色
 */
val CardBorderLight = Color(0xFFFFFFFF).copy(alpha = 0.05f)
val CardBorderHighlight = Color(0xFFEC7223).copy(alpha = 0.3f)
val SurfaceElevated = Color(0xFF222222)
val GradientOrangeStart = Color(0xFFEC7223)
val GradientOrangeEnd = Color(0xFFF08A4A)

/**
 * 玻璃质感 (Glassmorphism) 配色 - 统一系统
 */
object GlassColors {
    // 基础层 - 深色半透明背景
    val Base = Color(0xFF1A1A1A).copy(alpha = 0.75f)

    // 表面层 - 带微妙蓝紫色调的玻璃表面
    val Surface = Color(0xFF2A2A3A).copy(alpha = 0.60f)

    // 边框系统 - 双层边框结构
    val BorderOuter = Color(0xFFFFFFFF).copy(alpha = 0.18f)      // 外边框 - 更明显
    val BorderInner = Color(0xFFFFFFFF).copy(alpha = 0.06f)      // 内边框 -  subtle
    val BorderHighlight = Color(0xFFEC7223).copy(alpha = 0.7f)   // 高亮状态

    // 高光系统 - 玻璃的关键特征
    val HighlightTop = Color(0xFFFFFFFF).copy(alpha = 0.30f)     // 顶部高光
    val HighlightGlow = Color(0xFFEC7223).copy(alpha = 0.20f)    // 橙色光晕

    // 兼容旧代码的别名
    val Background = Base
    val Border = BorderOuter
}

// 保留旧名称兼容已有代码 (标记为废弃，逐步迁移)
@Deprecated("使用 GlassColors.Base 代替", ReplaceWith("GlassColors.Base"))
val GlassBackground = GlassColors.Base

@Deprecated("使用 GlassColors.BorderOuter 代替", ReplaceWith("GlassColors.BorderOuter"))
val GlassBorder = GlassColors.BorderOuter

@Deprecated("使用 GlassColors.BorderHighlight 代替", ReplaceWith("GlassColors.BorderHighlight"))
val GlassBorderHighlight = GlassColors.BorderHighlight

@Deprecated("使用 GlassColors.Surface 代替", ReplaceWith("GlassColors.Surface"))
val GlassSurface = GlassColors.Surface
