package com.silas.omaster.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silas.omaster.data.local.FloatingWindowGuideManager
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.components.FloatingWindowGuideDialog
import com.silas.omaster.ui.components.ImageGallery
import com.silas.omaster.ui.components.ModeBadge
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.components.ParameterCard
import com.silas.omaster.ui.components.SectionTitle
import com.silas.omaster.ui.components.ShootingTipsCard
import com.silas.omaster.ui.service.FloatingWindowController
import com.silas.omaster.ui.service.FloatingWindowService
import androidx.compose.ui.res.stringResource
import com.silas.omaster.R
import com.silas.omaster.ui.theme.HasselbladOrange
import com.silas.omaster.util.PresetI18n
import com.silas.omaster.util.formatSigned

@Composable
fun DetailScreen(
    presetId: String,
    onBack: () -> Unit,
    onEdit: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember { PresetRepository.getInstance(context) }
    
    // 使用 presetId 作为 key，确保每个预设有独立的 ViewModel
    val viewModel: DetailViewModel = viewModel(
        key = presetId,
        factory = DetailViewModelFactory(repository)
    )

    // 加载预设数据
    LaunchedEffect(presetId) {
        viewModel.loadPreset(presetId)
    }

    val preset by viewModel.preset.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    // 悬浮窗引导对话框状态
    var showFloatingWindowGuide by remember { mutableStateOf(false) }
    val guideManager = remember { FloatingWindowGuideManager.getInstance(context) }

    // 悬浮窗控制器（全局单例，已在 MainActivity 中注册）
    val floatingWindowController = remember { FloatingWindowController.getInstance(context) }

    // 当前显示的预设（用于悬浮窗切换时更新 UI）
    val floatingPreset by floatingWindowController.currentPreset.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OMasterTopAppBar(
            title = preset?.let { PresetI18n.getLocalizedPresetName(it.name) } ?: stringResource(R.string.detail_title),
            subtitle = preset?.author,
            onBack = onBack,
            actions = {
                // 悬浮窗按钮
                IconButton(
                    onClick = {
                        preset?.let { p ->
                            val isFirstTime = guideManager.isFirstTimeUseFloatingWindow()
                            android.util.Log.d("DetailScreen", "悬浮窗按钮点击，是否首次使用: $isFirstTime")
                            // 检查是否是首次使用悬浮窗
                            if (isFirstTime) {
                                showFloatingWindowGuide = true
                                guideManager.markGuideShown()
                                android.util.Log.d("DetailScreen", "显示悬浮窗引导对话框")
                            } else {
                                // 非首次使用，直接处理悬浮窗逻辑（预设列表已在 HomeScreen 中设置）
                                handleFloatingWindowClick(context, p)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = stringResource(R.string.floating_window),
                        tint = HasselbladOrange
                    )
                }

                // 编辑按钮（仅自定义预设显示）
                if (preset?.isCustom == true && onEdit != null) {
                    IconButton(
                        onClick = {
                            preset?.id?.let { presetId ->
                                onEdit(presetId)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = Color.White
                        )
                    }
                }

                // 收藏按钮
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFavorite)
                            Icons.Filled.Favorite
                        else
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) stringResource(R.string.preset_favorited) else stringResource(R.string.preset_favorite),
                        tint = if (isFavorite) HasselbladOrange else Color.White
                    )
                }
            },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (preset == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.detail_load_failed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 图片画廊（支持自动轮播和手动切换）
                    preset?.let {
                        ImageGallery(
                            images = it.allImages,
                            modifier = Modifier.fillMaxWidth(),
                            autoPlayInterval = 3000L
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        // 模式标签
                        ModeBadge(mode = it.mode)

                        Spacer(modifier = Modifier.height(16.dp))
                        // Pro 模式特有参数
                        if (it.isProMode) {
                            ProModeParameters(it)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 通用参数
                        CommonParameters(it, it.isProMode)

                        // 拍摄建议
                        it.shootingTips?.let { tips ->
                            Spacer(modifier = Modifier.height(24.dp))
                            ShootingTipsCard(
                                tips = PresetI18n.getLocalizedShootingTips(it.name, tips)
                            )
                        }
                    }
                }
            }
        }
    }

    // 悬浮窗引导对话框 - 放在最外层确保显示在最上层
    if (showFloatingWindowGuide) {
        FloatingWindowGuideDialog(
            onDismiss = {
                showFloatingWindowGuide = false
                // 用户选择"以后再说"，只是关闭对话框，不执行任何操作
            },
            onGoToSettings = {
                showFloatingWindowGuide = false
                // 用户点击"去开启权限"，跳转到权限设置
                preset?.let { p ->
                    handleFloatingWindowClick(context, p)
                }
            }
        )
    }
}

/**
 * 处理悬浮窗按钮点击逻辑
 */
private fun handleFloatingWindowClick(
    context: android.content.Context,
    preset: MasterPreset
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!Settings.canDrawOverlays(context)) {
            // 请求悬浮窗权限
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        } else {
            // 使用全局控制器显示悬浮窗（预设列表已在 HomeScreen 中设置）
            FloatingWindowController.getInstance(context).showFloatingWindow(preset)
        }
    } else {
        FloatingWindowController.getInstance(context).showFloatingWindow(preset)
    }
}

@Composable
private fun ProModeParameters(preset: MasterPreset?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.param_pro_adjust),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f)
        )

        // 第一行：ISO、快门速度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preset?.iso?.let {
                ParameterCard(
                    label = stringResource(R.string.param_iso),
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
            preset?.shutterSpeed?.let {
                ParameterCard(
                    label = stringResource(R.string.param_shutter),
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 第二行：曝光补偿、色温（优先显示 colorTemperature，没有则显示 whiteBalance）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preset?.exposureCompensation?.let {
                ParameterCard(
                    label = stringResource(R.string.param_exposure),
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
            // 优先显示数值型色温，如果没有则显示字符串型白平衡
            when {
                preset?.colorTemperature != null -> {
                    ParameterCard(
                        label = stringResource(R.string.param_color_temp),
                        value = "${preset.colorTemperature}K",
                        modifier = Modifier.weight(1f)
                    )
                }
                preset?.whiteBalance != null -> {
                    ParameterCard(
                        label = stringResource(R.string.param_white_balance),
                        value = preset.whiteBalance,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 第三行：色调（优先显示 colorHue，没有则显示 colorTone）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 优先显示数值型色调，如果没有则显示字符串型色调风格
            when {
                preset?.colorHue != null -> {
                    ParameterCard(
                        label = stringResource(R.string.param_tone),
                        value = preset.colorHue.formatSigned(),
                        modifier = Modifier.weight(1f)
                    )
                }
                preset?.colorTone != null -> {
                    ParameterCard(
                        label = stringResource(R.string.param_tone_style),
                        value = preset.colorTone,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommonParameters(preset: MasterPreset?, isProMode: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 调色参数标题放在滤镜上方
        SectionTitle(title = stringResource(R.string.section_color_grading))
        Spacer(modifier = Modifier.height(8.dp))

        // 滤镜类型
        preset?.let {
            ParameterCard(
                label = stringResource(R.string.param_filter),
                value = PresetI18n.getLocalizedFilter(it.filter),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 柔光、影调
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preset?.let {
                ParameterCard(
                    label = stringResource(R.string.param_soft_light),
                    value = PresetI18n.getLocalizedSoftLight(it.softLight),
                    modifier = Modifier.weight(1f)
                )
                ParameterCard(
                    label = stringResource(R.string.param_tone_curve),
                    value = it.tone.formatSigned(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 饱和度、冷暖
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preset?.let {
                ParameterCard(
                    label = stringResource(R.string.param_saturation),
                    value = it.saturation.formatSigned(),
                    modifier = Modifier.weight(1f)
                )
                ParameterCard(
                    label = stringResource(R.string.param_warm_cool),
                    value = it.warmCool.formatSigned(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 青品、锐度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preset?.let {
                ParameterCard(
                    label = stringResource(R.string.param_cyan_magenta),
                    value = it.cyanMagenta.formatSigned(),
                    modifier = Modifier.weight(1f)
                )
                ParameterCard(
                    label = stringResource(R.string.param_sharpness),
                    value = "${it.sharpness}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 暗角
        preset?.let {
            ParameterCard(
                label = stringResource(R.string.param_vignette),
                value = PresetI18n.getLocalizedVignette(it.vignette),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


