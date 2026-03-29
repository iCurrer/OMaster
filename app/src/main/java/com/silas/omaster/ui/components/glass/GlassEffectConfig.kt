package com.silas.omaster.ui.components.glass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 玻璃效果配置
 * 用于统一管理和自定义玻璃质感效果的所有参数
 *
 * @param cornerRadius 圆角半径
 * @param haloAlphaFavorite 收藏状态光晕透明度
 * @param haloAlphaNormal 普通状态光晕透明度
 * @param bodyAlphaFavorite 收藏状态主体透明度
 * @param bodyAlphaNormal 普通状态主体透明度
 * @param topGlowAlphaFavorite 收藏状态顶部发光透明度
 * @param topGlowAlphaNormal 普通状态顶部发光透明度
 * @param bottomGlowAlphaFavorite 收藏状态底部发光透明度
 * @param bottomGlowAlphaNormal 普通状态底部发光透明度
 * @param innerShadowAlpha 内阴影透明度
 * @param highlightAlpha 顶部高光透明度
 * @param highlightEndY 顶部高光结束位置
 */
data class GlassEffectConfig(
    val cornerRadius: Dp = 18.dp,
    val haloAlphaFavorite: Float = 0.3f,
    val haloAlphaNormal: Float = 0.1f,
    val haloAlphaFavoriteInner: Float = 0.15f,
    val haloAlphaNormalInner: Float = 0.05f,
    val haloRadius: Float = 0.8f,
    val bodyAlphaFavorite: Float = 0.2f,
    val bodyAlphaNormal: Float = 0.12f,
    val topGlowAlphaFavorite: Float = 0.7f,
    val topGlowAlphaNormal: Float = 0.4f,
    val bottomGlowAlphaFavorite: Float = 0.6f,
    val bottomGlowAlphaNormal: Float = 0.35f,
    val innerShadowAlpha: Float = 0.15f,
    val highlightAlpha: Float = 0.25f,
    val highlightMidAlpha: Float = 0.08f,
    val highlightEndY: Float = 26f,
    val borderWidth: Dp = 0.5.dp,
    val innerShadowWidth: Dp = 1.5.dp
) {
    companion object {
        /**
         * 默认按钮配置
         */
        val DefaultButton = GlassEffectConfig()

        /**
         * 柔和配置 - 更轻量的玻璃效果
         */
        val Soft = GlassEffectConfig(
            haloAlphaFavorite = 0.2f,
            haloAlphaNormal = 0.08f,
            bodyAlphaFavorite = 0.15f,
            bodyAlphaNormal = 0.1f,
            topGlowAlphaFavorite = 0.5f,
            bottomGlowAlphaFavorite = 0.4f
        )

        /**
         * 强烈配置 - 更明显的玻璃效果
         */
        val Strong = GlassEffectConfig(
            haloAlphaFavorite = 0.4f,
            haloAlphaNormal = 0.15f,
            bodyAlphaFavorite = 0.25f,
            bodyAlphaNormal = 0.15f,
            topGlowAlphaFavorite = 0.8f,
            bottomGlowAlphaFavorite = 0.7f,
            highlightAlpha = 0.3f
        )

        /**
         * 导航胶囊配置 - 适用于底部导航栏选中胶囊
         */
        val NavigationCapsule = GlassEffectConfig(
            cornerRadius = 24.dp,
            haloAlphaFavorite = 0.25f,
            haloAlphaNormal = 0.12f,
            bodyAlphaFavorite = 0.18f,
            bodyAlphaNormal = 0.1f,
            topGlowAlphaFavorite = 0.6f,
            bottomGlowAlphaFavorite = 0.5f,
            topGlowAlphaNormal = 0.3f,
            bottomGlowAlphaNormal = 0.25f,
            innerShadowAlpha = 0.12f,
            highlightAlpha = 0.22f,
            highlightMidAlpha = 0.06f,
            highlightEndY = 22f,
            borderWidth = 0.5.dp,
            innerShadowWidth = 1.dp
        )
    }
}
