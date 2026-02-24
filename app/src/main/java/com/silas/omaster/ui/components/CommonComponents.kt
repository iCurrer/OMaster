package com.silas.omaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.silas.omaster.R
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.animation.AnimationSpecs
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.PureBlack
import java.io.File

/**
 * 通用顶部导航栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OMasterTopAppBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
            } ?: Box {}
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PureBlack,
            titleContentColor = Color.White
        ),
        modifier = modifier
    )
}

/**
 * 功能特性卡片组件
 */
@Composable
fun FeatureCard(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    iconSize: TextUnit = 32.sp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = iconSize
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 参数显示项组件
 */
@Composable
fun ParameterItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 通用卡片组件
 */
@Composable
fun OMasterCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        content = content
    )
}

/**
 * 垂直间距组件
 */
@Composable
fun VerticalSpacer(height: Dp) = Spacer(modifier = Modifier.height(height))

/**
 * 水平间距组件
 */
@Composable
fun HorizontalSpacer(width: Dp) = Spacer(modifier = Modifier.width(width))

/**
 * 模式标签组件
 * 支持显示多个标签
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeBadge(
    tags: List<String>?,
    modifier: Modifier = Modifier
) {
    if (tags.isNullOrEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGray)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 预设图片组件
 * 支持 assets 和内部存储两种路径
 * 优化：使用更短的 crossfade 动画时长
 */
@Composable
fun PresetImage(
    preset: MasterPreset,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    // 判断图片路径类型
    val imageUri = when {
        // 网络图片：以 http 或 https 开头
        preset.coverPath.startsWith("http") -> preset.coverPath
        // 绝对路径
        preset.coverPath.startsWith("/") -> File(preset.coverPath).toUri().toString()
        // 自定义预设：路径以 presets/ 开头，使用内部存储
        preset.isCustom || preset.coverPath.startsWith("presets/") -> {
            File(context.filesDir, preset.coverPath).toUri().toString()
        }
        // 默认预设：使用 assets
        else -> "file:///android_asset/${preset.coverPath}"
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(AnimationSpecs.FastTween.durationMillis) // 使用快速动画规格
            .diskCachePolicy(CachePolicy.ENABLED) // 确保开启磁盘缓存
            .build(),
        contentDescription = preset.name,
        contentScale = contentScale,
        modifier = modifier
    )
}

/**
 * 章节标题组件
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/**
 * 带卡片的参数项组件
 */
@Composable
fun ParameterCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 拍摄建议卡片组件
 */
@Composable
fun ShootingTipsCard(
    tips: String,
    modifier: Modifier = Modifier
) {
    DescriptionCard(
        title = stringResource(R.string.shooting_tips),
        content = tips,
        modifier = modifier
    )
}

/**
 * 通用描述卡片组件
 */
@Composable
fun DescriptionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容
            content.split("\n").forEach { line ->
                if (line.isNotBlank()) {
                    Text(
                        text = line.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
