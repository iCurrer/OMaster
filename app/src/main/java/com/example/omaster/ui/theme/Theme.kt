package com.example.omaster.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * OMaster 深色主题配色方案
 * 采用纯黑背景 + 哈苏橙强调色的组合
 */
private val DarkColorScheme = darkColorScheme(
    primary = HasselbladOrange,
    onPrimary = PureBlack,
    primaryContainer = HasselbladOrangeDark,
    onPrimaryContainer = OffWhite,
    secondary = LightGray,
    onSecondary = PureBlack,
    secondaryContainer = DarkGray,
    onSecondaryContainer = OffWhite,
    tertiary = HasselbladOrangeLight,
    onTertiary = PureBlack,
    tertiaryContainer = MediumGray,
    onTertiaryContainer = OffWhite,
    background = PureBlack,
    onBackground = OffWhite,
    surface = NearBlack,
    onSurface = OffWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    error = ErrorRed,
    onError = OffWhite,
    outline = MediumGray,
    outlineVariant = DarkGray,
    scrim = PureBlack.copy(alpha = 0.8f)
)

/**
 * 浅色主题配色方案（备用）
 */
private val LightColorScheme = lightColorScheme(
    primary = HasselbladOrange,
    onPrimary = OffWhite,
    primaryContainer = HasselbladOrangeLight,
    onPrimaryContainer = PureBlack,
    secondary = DarkGray,
    onSecondary = OffWhite,
    secondaryContainer = LightGray,
    onSecondaryContainer = PureBlack,
    tertiary = HasselbladOrangeDark,
    onTertiary = OffWhite,
    tertiaryContainer = OffWhite,
    onTertiaryContainer = PureBlack,
    background = OffWhite,
    onBackground = PureBlack,
    surface = Color.White,
    onSurface = PureBlack,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    error = ErrorRed,
    onError = Color.White,
    outline = MediumGray,
    outlineVariant = LightGray,
    scrim = PureBlack.copy(alpha = 0.5f)
)

/**
 * OMaster 主题配置
 *
 * @param darkTheme 是否使用深色主题，默认为 true（强制深色模式）
 * @param dynamicColor 是否使用动态颜色，默认为 false
 * @param content 主题内容
 */
@Composable
fun OMasterTheme(
    darkTheme: Boolean = true, // 强制深色模式
    dynamicColor: Boolean = false, // 禁用动态颜色，使用品牌色
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val windowInsetsController = WindowInsetsControllerCompat(window, view)

            // 配置状态栏图标颜色（浅色图标用于深色背景）
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            // 配置导航栏图标颜色
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
