package com.silas.omaster.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.CardBorderLight
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import com.silas.omaster.ui.theme.themedBorderLight
import com.silas.omaster.util.VersionInfo
import com.silas.omaster.util.perform
import com.silas.omaster.util.rememberScrollHaptics

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToOpenSourceLicense: () -> Unit = {},
    currentVersionName: String = VersionInfo.VERSION_NAME
) {
    val scrollState = rememberScrollState()
    var previousScrollValue by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val isScrollingUp by remember {
        derivedStateOf {
            val currentScroll = scrollState.value
            val isUp = currentScroll <= previousScrollValue
            previousScrollValue = currentScroll
            isUp
        }
    }

    // 滚动到顶/底部震感
    rememberScrollHaptics(scrollState)

    LaunchedEffect(isScrollingUp) {
        onScrollStateChanged(isScrollingUp)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.nav_about),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.nav_settings),
                        tint = themedTextPrimary()
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(AppDesign.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTitleSection(currentVersionName)

            Spacer(modifier = Modifier.height(24.dp))

            // 功能入口列表
            ProfileMenuList(
                onNavigateToSubscription = onNavigateToSubscription
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureList()

            Spacer(modifier = Modifier.height(16.dp))

            CreditsCard(context)

            Spacer(modifier = Modifier.height(16.dp))

            ProjectCard(context)

            Spacer(modifier = Modifier.height(16.dp))

            QQGroupCard(context)

            Spacer(modifier = Modifier.height(24.dp))

            FooterSection(context, onNavigateToPrivacyPolicy, onNavigateToOpenSourceLicense)

            // 底部额外留白，避免内容太靠下
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AppTitleSection(currentVersionName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 应用名称（O 使用主题色）
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("O")
                }
                withStyle(style = SpanStyle(color = themedTextPrimary())) {
                    append("Master")
                }
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 副标题
        Text(
            text = stringResource(R.string.app_slogan),
            style = MaterialTheme.typography.bodyLarge,
            color = themedTextSecondary().copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 版本号标签
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Version $currentVersionName",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeatureList() {
    val features = listOf(
        FeatureItem(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.feature_custom_title),
            description = stringResource(R.string.feature_custom_desc)
        ),
        FeatureItem(
            icon = Icons.Default.Cloud,
            title = stringResource(R.string.feature_cloud_title),
            description = stringResource(R.string.feature_cloud_desc)
        ),
        FeatureItem(
            icon = Icons.Default.DashboardCustomize,
            title = stringResource(R.string.feature_update_title),
            description = stringResource(R.string.feature_update_desc)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = themedBorderLight(),
                shape = AppDesign.CardShape
            ),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = AppDesign.CardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDesign.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppDesign.IconButtonSize - 16.dp)
                )
                Text(
                    text = stringResource(R.string.feature_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themedTextPrimary()
                )
            }

            features.forEach { feature ->
                FeatureListItem(feature)
            }
        }
    }
}

@Composable
private fun FeatureListItem(feature: FeatureItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDesign.ItemSpacing / 2),
        horizontalArrangement = Arrangement.spacedBy(AppDesign.ItemSpacing)
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = themedTextPrimary()
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary().copy(alpha = AppDesign.SecondaryAlpha),
                textAlign = TextAlign.Start
            )
        }
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private data class Contributor(
    val name: String,
    val url: String
)

@Composable
private fun CreditsCard(context: android.content.Context) {
    val contributors = listOf(
        Contributor("@OPPO影像", "https://xhslink.com/m/8c2gJYGlCTR"),
        Contributor("@蘭州白鴿", "https://xhslink.com/m/4h5lx4Lg37n"),
        Contributor("@派瑞特凯", "https://xhslink.com/m/AkrgUI0kgg1"),
        Contributor("@ONESTEP™", "https://xhslink.com/m/4LZ8zRdNCSv"),
        Contributor("@盒子叔", "https://xhslink.com/m/4mje9mimNXJ"),
        Contributor("@Aurora", "https://xhslink.com/m/2Ebow4iyVOE"),
        Contributor("@屋顶橙子味", "https://v.douyin.com/YkVXPX9kZgY/"),
        Contributor("@尼克lin", "https://xhslink.com/m/AjhYASTkUwq"),
        Contributor("@波子Booz", "https://xhslink.com/m/9leyjjz68Et")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CardBorderLight,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.credits_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themedTextPrimary()
                )
            }

            // 开发者区域
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.developer),
                    style = MaterialTheme.typography.bodySmall,
                    color = themedTextSecondary().copy(alpha = 0.6f)
                )

                val developers = listOf(
                    "Silas" to "https://xhslink.com/m/2gh56F1blnO",
                    "Luminary" to "https://github.com/fengyec2"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    developers.forEach { (name, url) ->
                        DeveloperChip(name = name, url = url, context = context)
                    }
                }
            }

            // 素材提供区域
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.material_provider),
                    style = MaterialTheme.typography.bodySmall,
                    color = themedTextSecondary().copy(alpha = 0.6f)
                )

                // 贡献者标签云
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contributors.forEach { contributor ->
                        ContributorChip(
                            name = contributor.name,
                            url = contributor.url,
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContributorChip(
    name: String,
    url: String,
    context: android.content.Context
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(themedTextPrimary().copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = themedTextPrimary().copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = themedTextPrimary().copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun DeveloperChip(name: String, url: String, context: android.content.Context) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProjectCard(context: android.content.Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = themedBorderLight(),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/iCurrer/OMaster"))
            context.startActivity(intent)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "项目地址",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themedTextPrimary()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "GitHub - iCurrer/OMaster",
                        style = MaterialTheme.typography.bodySmall,
                        color = themedTextSecondary().copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun QQGroupCard(context: android.content.Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CardBorderLight,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            // 群号
            val groupId = "1083543279"
            
            // 尝试使用URL Scheme打开QQ群
            val schemes = listOf(
                // 方式1：使用腾讯官方API（推荐）
                "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$groupId&card_type=group&source=qrcode",
                // 方式2：使用QQ群加群链接（兼容旧版）
                "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fc%2Fcgi-bin%2Fqm%2Fqr%3Fk%3D$groupId",
                // 方式3：使用简化版scheme
                "mqq://card/show_pslcard?src_type=internal&version=1&uin=$groupId&card_type=group"
            )

            var opened = false
            for (scheme in schemes) {
                if (!opened) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        // 检查是否有应用可以处理这个intent
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                            opened = true
                            break
                        }
                    } catch (e: Exception) {
                        // 尝试下一个方式
                    }
                }
            }

            // 如果所有scheme都失败，使用网页版链接（浏览器会提示打开QQ）
            if (!opened) {
                try {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://qm.qq.com/cgi-bin/qm/qr?k=$groupId&jump_from=webapi"))
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(webIntent)
                } catch (e: Exception) {
                    // 如果浏览器也打不开，提示用户
                    android.widget.Toast.makeText(context, "无法打开QQ群，请检查是否安装QQ", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "QQ",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "加入QQ群",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themedTextPrimary()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "群号: 1083543279",
                        style = MaterialTheme.typography.bodySmall,
                        color = themedTextSecondary().copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileMenuList(
    onNavigateToSubscription: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = themedBorderLight(),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // 订阅管理
            ProfileMenuItem(
                icon = Icons.Default.Cloud,
                title = stringResource(R.string.sub_title),
                onClick = onNavigateToSubscription
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = themedTextPrimary()
            )
        }
        Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = themedTextSecondary().copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
        }
    }

@Composable
private fun FooterSection(
    context: android.content.Context,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToOpenSourceLicense: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "© 2026 OMaster",
            style = MaterialTheme.typography.bodySmall,
            color = themedTextSecondary().copy(alpha = 0.6f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.user_agreement),
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary().copy(alpha = 0.7f),
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ocnquih40x3i.feishu.cn/docx/WHVldUhumozJAUx7ZFhcO9uznaf?from=from_copylink"))
                    context.startActivity(intent)
                }
            )

            androidx.compose.material3.Divider(
                modifier = Modifier
                    .height(12.dp)
                    .width(1.dp),
                color = themedTextSecondary().copy(alpha = 0.2f)
            )

            Text(
                text = stringResource(R.string.privacy_policy),
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary().copy(alpha = 0.7f),
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ocnquih40x3i.feishu.cn/docx/NSgednMU0oeq9RxnGmcc9vRenvd?from=from_copylink"))
                    context.startActivity(intent)
                }
            )

            androidx.compose.material3.Divider(
                modifier = Modifier
                    .height(12.dp)
                    .width(1.dp),
                color = themedTextSecondary().copy(alpha = 0.2f)
            )

            Text(
                text = stringResource(R.string.open_source_license),
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary().copy(alpha = 0.7f),
                modifier = Modifier.clickable {
                    onNavigateToOpenSourceLicense()
                }
            )
        }

        Text(
            text = "豫 ICP 备 2026011707 号 -1A",
            style = MaterialTheme.typography.bodySmall,
            color = themedTextSecondary().copy(alpha = 0.5f),
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://beian.miit.gov.cn"))
                context.startActivity(intent)
            }
        )
    }
}
