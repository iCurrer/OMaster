package com.silas.omaster.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.CardBorderLight
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.GradientOrangeEnd
import com.silas.omaster.ui.theme.GradientOrangeStart
import com.silas.omaster.ui.theme.HasselbladOrange
import com.silas.omaster.util.UpdateChecker
import com.silas.omaster.util.VersionInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.widget.Toast
import com.silas.omaster.util.UpdateConfigManager
import com.silas.omaster.network.PresetRemoteManager
import com.silas.omaster.data.repository.PresetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit,
    currentVersionCode: Int = VersionInfo.VERSION_CODE,
    currentVersionName: String = VersionInfo.VERSION_NAME
) {
    val scrollState = rememberScrollState()
    var previousScrollValue by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isScrollingUp by remember {
        derivedStateOf {
            val currentScroll = scrollState.value
            val isUp = currentScroll <= previousScrollValue
            previousScrollValue = currentScroll
            isUp
        }
    }

    var isChecking by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
    var checkError by remember { mutableStateOf<String?>(null) }
    var lastCheckTime by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(isScrollingUp) {
        onScrollStateChanged(isScrollingUp)
    }

    val checkFailedText = stringResource(R.string.version_check_failed)

    LaunchedEffect(Unit) {
        delay(500)
        if (updateInfo == null && checkError == null) {
            isChecking = true
            checkError = null
            try {
                val result = UpdateChecker.checkUpdate(currentVersionCode)
                if (result != null) {
                    updateInfo = result
                    lastCheckTime = System.currentTimeMillis()
                } else {
                    checkError = checkFailedText
                }
            } catch (e: Exception) {
                checkError = e.message ?: checkFailedText
            } finally {
                isChecking = false
            }
        }
    }

    val checkForUpdate = {
        scope.launch {
            isChecking = true
            checkError = null
            try {
                val result = UpdateChecker.checkUpdate(currentVersionCode)
                if (result != null) {
                    updateInfo = result
                    lastCheckTime = System.currentTimeMillis()
                } else {
                    checkError = checkFailedText
                }
            } catch (e: Exception) {
                checkError = e.message ?: checkFailedText
            } finally {
                isChecking = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.about_title),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTitleSection(currentVersionName)

            Spacer(modifier = Modifier.height(32.dp))

            UpdateCard(
                currentVersionName = currentVersionName,
                isChecking = isChecking,
                updateInfo = updateInfo,
                checkError = checkError,
                lastCheckTime = lastCheckTime,
                onCheckClick = { checkForUpdate() },
                onDownloadClick = { 
                    updateInfo?.let { info ->
                        UpdateChecker.openDownloadPage(context, info.downloadUrl)
                    }
                },
                onRetryClick = {
                    checkError = null
                    checkForUpdate()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 远程预设更新卡片
            var isFetchingPresets by remember { mutableStateOf(false) }
            var presetMsg by remember { mutableStateOf<String?>(null) }
            var showUrlDialog by remember { mutableStateOf(false) }
            var editUrlText by remember { mutableStateOf("") }

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "配置更新", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = {
                            // 打开设置对话框
                            editUrlText = UpdateConfigManager.getPresetUrl(context)
                            showUrlDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "设置更新链接")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            // 触发检查并下载远程 presets
                            val url = UpdateConfigManager.getPresetUrl(context)
                            scope.launch {
                                try {
                                    isFetchingPresets = true
                                    presetMsg = null
                                    android.util.Log.d("AboutScreen", "Fetching presets from $url")
                                    val success = PresetRemoteManager.fetchAndSave(context, url)
                                    if (success) {
                                        // 刷新仓库
                                        val repo = PresetRepository.getInstance(context)
                                        repo.reloadDefaultPresets()
                                        presetMsg = "配置更新成功"
                                        Toast.makeText(context, "配置更新成功", Toast.LENGTH_SHORT).show()
                                    } else {
                                        presetMsg = "配置更新失败"
                                        Toast.makeText(context, "配置更新失败", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("AboutScreen", "Preset update failed", e)
                                    presetMsg = e.message
                                    Toast.makeText(context, "配置更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isFetchingPresets = false
                                }
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "检查配置更新")
                        }

                        if (isFetchingPresets) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }

                    presetMsg?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = msg, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (showUrlDialog) {
                AlertDialog(
                    onDismissRequest = { showUrlDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            UpdateConfigManager.setPresetUrl(context, editUrlText)
                            showUrlDialog = false
                            Toast.makeText(context, "已保存更新链接", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(text = stringResource(id = R.string.save))
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showUrlDialog = false }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    },
                    title = { Text(text = "设置更新链接") },
                    text = {
                        OutlinedTextField(
                            value = editUrlText,
                            onValueChange = { editUrlText = it },
                            singleLine = true
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard()

            Spacer(modifier = Modifier.height(16.dp))

            CreditsCard(context)

            Spacer(modifier = Modifier.height(24.dp))

            FooterSection(context)
        }
    }
}

@Composable
private fun AppTitleSection(currentVersionName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = HasselbladOrange)) {
                    append("O")
                }
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("Master")
                }
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.app_slogan),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .background(
                    color = HasselbladOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "v$currentVersionName",
                style = MaterialTheme.typography.labelMedium,
                color = HasselbladOrange,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun UpdateCard(
    currentVersionName: String,
    isChecking: Boolean,
    updateInfo: UpdateChecker.UpdateInfo?,
    checkError: String?,
    lastCheckTime: Long?,
    onCheckClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    val hasUpdate = updateInfo?.isNewer == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (hasUpdate) 1.5.dp else 1.dp,
                color = if (hasUpdate) HasselbladOrange.copy(alpha = 0.5f) else CardBorderLight,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 顶部：图标 + 版本号 + 刷新
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (hasUpdate) HasselbladOrange.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = if (hasUpdate) HasselbladOrange else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "v$currentVersionName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (lastCheckTime != null && !isChecking) {
                            val diff = System.currentTimeMillis() - lastCheckTime
                            val timeText = when {
                                diff < 60000 -> stringResource(R.string.time_just_now)
                                diff < 3600000 -> stringResource(R.string.time_minutes_ago, diff / 60000)
                                diff < 86400000 -> stringResource(R.string.time_hours_ago, diff / 3600000)
                                else -> stringResource(R.string.time_days_ago, diff / 86400000)
                            }
                            Text(
                                text = stringResource(R.string.last_check, timeText),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                if (!isChecking) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                        tint = HasselbladOrange.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onCheckClick() }
                    )
                }
            }

            // 状态显示
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    isChecking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = HasselbladOrange,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = stringResource(R.string.checking),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    updateInfo != null -> {
                        if (updateInfo.isNewer) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = HasselbladOrange.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "v${updateInfo.versionName}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = HasselbladOrange,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Button(
                                    onClick = onDownloadClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HasselbladOrange
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.version_download_btn),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.version_is_latest),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    checkError != null -> {
                        Row(
                            modifier = Modifier.clickable { onRetryClick() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.version_retry),
                                style = MaterialTheme.typography.bodyMedium,
                                color = HasselbladOrange
                            )
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.clickable { onCheckClick() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.version_check),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CardBorderLight,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = HasselbladOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.feature_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = stringResource(R.string.feature_desc_1),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )
            Text(
                text = stringResource(R.string.feature_desc_2),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )
        }
    }
}

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
        Contributor("@Aurora", "https://xhslink.com/m/2Ebow4iyVOE")
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
            containerColor = DarkGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = HasselbladOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.credits_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.developer) + "：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(R.string.developer_name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.material_provider),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contributors.take(3).forEach { contributor ->
                        ContributorItem(
                            name = contributor.name,
                            url = contributor.url,
                            context = context
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contributors.drop(3).forEach { contributor ->
                        ContributorItem(
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

@Composable
private fun ContributorItem(
    name: String,
    url: String,
    context: android.content.Context
) {
    Row(
        modifier = Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    HasselbladOrange.copy(alpha = 0.6f),
                    RoundedCornerShape(50)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = HasselbladOrange,
            textDecoration = TextDecoration.Underline
        )
    }
}

@Composable
private fun FooterSection(context: android.content.Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.footer_local),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )

        Text(
            text = stringResource(R.string.privacy_policy),
            style = MaterialTheme.typography.bodySmall,
            color = HasselbladOrange.copy(alpha = 0.8f),
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.umeng.com/page/policy"))
                context.startActivity(intent)
            }
        )
    }
}
