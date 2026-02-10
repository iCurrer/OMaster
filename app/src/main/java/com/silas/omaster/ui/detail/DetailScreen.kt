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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.components.ImageGallery
import com.silas.omaster.ui.components.ModeBadge
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.components.ParameterCard
import com.silas.omaster.ui.components.SectionTitle
import com.silas.omaster.ui.service.FloatingWindowService
import com.silas.omaster.ui.theme.HasselbladOrange
import com.silas.omaster.util.formatSigned

@Composable
fun DetailScreen(
    presetId: String,
    onBack: () -> Unit
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

    Column(modifier = Modifier.fillMaxSize()) {
        OMasterTopAppBar(
            title = preset?.name ?: "预设详情",
            subtitle = preset?.author,
            onBack = onBack,
            actions = {
                // 悬浮窗按钮
                IconButton(
                    onClick = {
                        preset?.let { p ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!Settings.canDrawOverlays(context)) {
                                    // 请求悬浮窗权限
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } else {
                                    // 启动悬浮窗服务
                                    FloatingWindowService.show(context, p)
                                }
                            } else {
                                FloatingWindowService.show(context, p)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = "悬浮窗",
                        tint = HasselbladOrange
                    )
                }

                // 收藏按钮
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFavorite)
                            Icons.Filled.Favorite
                        else
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "已收藏" else "收藏",
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
                        text = "预设数据加载失败",
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
                    }
                }
            }
        }
    }
}

@Composable
private fun ProModeParameters(preset: MasterPreset?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "专业参数",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f)
        )

        // 第一行：曝光补偿、色温
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preset?.exposureCompensation?.let {
                ParameterCard(
                    label = "曝光补偿",
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
            preset?.colorTemperature?.let {
                ParameterCard(
                    label = "色温",
                    value = "${it}K",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 第二行：色调数值、色调（字符串）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            preset?.colorHue?.let {
                ParameterCard(
                    label = "色调",
                    value = it.formatSigned(),
                    modifier = Modifier.weight(1f)
                )
            }
            preset?.colorTone?.let {
                ParameterCard(
                    label = "色调风格",
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 第三行：白平衡
        preset?.whiteBalance?.let {
            ParameterCard(
                label = "白平衡",
                value = it,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CommonParameters(preset: MasterPreset?, isProMode: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 调色参数标题放在滤镜上方
        SectionTitle(title = "调色参数")
        Spacer(modifier = Modifier.height(8.dp))

        // 滤镜类型
        preset?.let {
            ParameterCard(
                label = "滤镜",
                value = it.filter,
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
                    label = "柔光",
                    value = it.softLight,
                    modifier = Modifier.weight(1f)
                )
                ParameterCard(
                    label = "影调",
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
                    label = "饱和度",
                    value = it.saturation.formatSigned(),
                    modifier = Modifier.weight(1f)
                )
                ParameterCard(
                    label = "冷暖",
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
                    label = "青品",
                    value = it.cyanMagenta.formatSigned(),
                    modifier = Modifier.weight(1f)
                )
                ParameterCard(
                    label = "锐度",
                    value = "${it.sharpness}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 暗角
        preset?.let {
            ParameterCard(
                label = "暗角",
                value = it.vignette,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


