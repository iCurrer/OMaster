package com.silas.omaster.ui.settings

import android.app.DownloadManager
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.data.config.ConfigCenter
import com.silas.omaster.data.local.AppLanguage
import com.silas.omaster.data.local.DarkMode
import com.silas.omaster.data.local.FloatingWindowMode
import com.silas.omaster.data.local.UpdateChannel
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.BrandTheme
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedBorderLight
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import com.silas.omaster.util.HapticSettings
import com.silas.omaster.util.ImageCacheManager
import com.silas.omaster.util.LogExporter
import com.silas.omaster.util.Logger
import com.silas.omaster.util.UpdateChecker
import com.silas.omaster.util.VersionInfo
import com.silas.omaster.util.perform
import com.silas.omaster.util.rememberScrollHaptics
import android.content.Intent
import android.widget.Toast
import com.silas.omaster.MainActivity
import androidx.compose.material.icons.filled.BugReport
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val config = remember { ConfigCenter.getInstance(context) }
    var vibrationEnabled by remember { mutableStateOf(config.isVibrationEnabled) }
    val currentTheme by config.themeFlow.collectAsState()
    val darkMode by config.darkModeFlow.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showTabDialog by remember { mutableStateOf(false) }
    var floatingWindowOpacity by remember { mutableStateOf(config.floatingWindowOpacity) }
    var defaultStartTab by remember { mutableStateOf(config.defaultStartTab) }
    var updateChannel by remember { mutableStateOf(config.updateChannel) }
    var showChannelDialog by remember { mutableStateOf(false) }
    var autoCheckUpdate by remember { mutableStateOf(config.isAutoCheckUpdateEnabled) }
    var analyticsEnabled by remember { mutableStateOf(config.isAnalyticsEnabled) }
    var cacheSize by remember { mutableStateOf(ImageCacheManager.getCacheSize(context)) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var floatingWindowMode by remember { mutableStateOf(config.floatingWindowMode) }
    var showFloatingModeDialog by remember { mutableStateOf(false) }
    var appLanguage by remember { mutableStateOf(config.appLanguage) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var logSize by remember { mutableStateOf(LogExporter.getFormattedLogSize()) }
    var showClearLogDialog by remember { mutableStateOf(false) }
    var premiumGlassEnabled by remember { mutableStateOf(config.isPremiumGlassEnabled) }
    val haptic = LocalHapticFeedback.current

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                haptic.perform(HapticFeedbackType.Confirm)
                config.currentTheme = theme
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showTabDialog) {
        TabSelectionDialog(
            currentTab = defaultStartTab,
            onTabSelected = { tab ->
                haptic.perform(HapticFeedbackType.Confirm)
                defaultStartTab = tab
                config.defaultStartTab = tab
                showTabDialog = false
            },
            onDismiss = { showTabDialog = false }
        )
    }

    if (showChannelDialog) {
        UpdateChannelDialog(
            currentChannel = updateChannel,
            onChannelSelected = { channel ->
                haptic.perform(HapticFeedbackType.Confirm)
                config.updateChannel = channel
                updateChannel = channel
                showChannelDialog = false
            },
            onDismiss = { showChannelDialog = false }
        )
    }

    if (showFloatingModeDialog) {
        FloatingWindowModeDialog(
            currentMode = floatingWindowMode,
            onModeSelected = { mode ->
                haptic.perform(HapticFeedbackType.Confirm)
                config.floatingWindowMode = mode
                floatingWindowMode = mode
                showFloatingModeDialog = false
            },
            onDismiss = { showFloatingModeDialog = false }
        )
    }

    if (showDarkModeDialog) {
        DarkModeSelectionDialog(
            currentMode = darkMode,
            onModeSelected = { mode ->
                haptic.perform(HapticFeedbackType.Confirm)
                config.darkMode = mode
                showDarkModeDialog = false
            },
            onDismiss = { showDarkModeDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = appLanguage,
            onLanguageSelected = { language ->
                haptic.perform(HapticFeedbackType.Confirm)
                // 如果语言没有变化，直接关闭对话框
                if (language == appLanguage) {
                    showLanguageDialog = false
                    return@LanguageSelectionDialog
                }
                config.appLanguage = language
                appLanguage = language
                showLanguageDialog = false
                // 提示用户并重启应用
                Toast.makeText(context, context.getString(R.string.toast_language_changed), Toast.LENGTH_SHORT).show()
                // 延迟一点让用户看到 Toast
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                    Runtime.getRuntime().exit(0)
                }, 800)
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    val scrollState = rememberScrollState()
    rememberScrollHaptics(scrollState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themedBackground())
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.settings_title),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
        // General Section
        SettingsSectionCard {
            SettingsSectionTitle(title = stringResource(R.string.settings_section_general))

            // Vibration Setting
            SettingsSwitchItem(
                icon = Icons.Default.Vibration,
                title = stringResource(R.string.vibration_feedback),
                checked = vibrationEnabled,
                onCheckedChange = { enabled ->
                    vibrationEnabled = enabled
                    config.isVibrationEnabled = enabled
                    HapticSettings.enabled = enabled
                    if (enabled) {
                        haptic.perform(HapticFeedbackType.ToggleOn)
                    }
                }
            )

            HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

            // Default Start Tab Setting
            SettingsClickableItem(
                icon = Icons.Default.DashboardCustomize,
                title = stringResource(R.string.default_start_tab),
                subtitle = getTabName(defaultStartTab),
                onClick = { showTabDialog = true }
            )

            HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

            // Language Setting
            SettingsClickableItem(
                icon = Icons.Default.Cloud,
                title = stringResource(R.string.language),
                subtitle = when (appLanguage) {
                    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                    AppLanguage.CHINESE -> stringResource(R.string.language_chinese)
                    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                },
                onClick = { showLanguageDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Section
        SettingsSectionCard {
            SettingsSectionTitle(title = stringResource(R.string.settings_section_appearance))

            // Theme Setting
            SettingsClickableItem(
                icon = Icons.Default.Brush,
                title = stringResource(R.string.settings_theme_title),
                subtitle = stringResource(currentTheme.brandNameResId),
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primaryColor)
                    )
                },
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

            // 深色模式选择
            SettingsClickableItem(
                icon = Icons.Default.DarkMode,
                title = stringResource(R.string.settings_dark_mode),
                subtitle = when (darkMode) {
                    DarkMode.SYSTEM -> stringResource(R.string.dark_mode_system)
                    DarkMode.LIGHT -> stringResource(R.string.dark_mode_light)
                    DarkMode.DARK -> stringResource(R.string.dark_mode_dark)
                },
                onClick = { showDarkModeDialog = true }
            )

            HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

            // 高级 Glass 质感开关
            SettingsSwitchItem(
                icon = Icons.Default.AutoAwesome,
                title = stringResource(R.string.premium_glass_effect),
                subtitle = stringResource(R.string.premium_glass_effect_desc),
                checked = premiumGlassEnabled,
                onCheckedChange = { enabled ->
                    haptic.perform(HapticFeedbackType.ToggleOn)
                    premiumGlassEnabled = enabled
                    config.isPremiumGlassEnabled = enabled
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Floating Window Section
        SettingsSectionCard {
            SettingsSectionTitle(title = stringResource(R.string.settings_section_floating_window))

            // Floating Window Mode Setting
            SettingsClickableItem(
                icon = Icons.Default.DashboardCustomize,
                title = stringResource(R.string.floating_window_mode),
                subtitle = when (floatingWindowMode) {
                    FloatingWindowMode.STANDARD -> stringResource(R.string.floating_window_mode_standard)
                    FloatingWindowMode.COMPACT -> stringResource(R.string.floating_window_mode_compact)
                },
                onClick = { showFloatingModeDialog = true }
            )

            // Realme 预设提示
            Text(
                text = stringResource(R.string.floating_window_realme_tip),
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDesign.ContentPadding, vertical = 8.dp)
            )

            HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

            // Floating Window Opacity Setting
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDesign.ContentPadding, vertical = AppDesign.ItemSpacing + 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.floating_window_opacity),
                        style = MaterialTheme.typography.bodyLarge,
                        color = themedTextPrimary()
                    )
                    Text(
                        text = "$floatingWindowOpacity%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                var previousOpacity by remember { mutableStateOf(floatingWindowOpacity) }
                Slider(
                    value = floatingWindowOpacity.toFloat(),
                    onValueChange = {
                        val newValue = it.toInt()
                        floatingWindowOpacity = newValue
                        config.floatingWindowOpacity = newValue
                        if (newValue != previousOpacity) {
                            haptic.perform(HapticFeedbackType.TextHandleMove)
                            previousOpacity = newValue
                        }
                    },
                    valueRange = 30f..70f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "30%",
                        style = MaterialTheme.typography.bodySmall,
                        color = themedTextSecondary()
                    )
                    Text(
                        text = stringResource(R.string.recommended) + "56%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "70%",
                        style = MaterialTheme.typography.bodySmall,
                        color = themedTextSecondary()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Update Section - 新版本检查卡片
        SettingsUpdateSection(
            updateChannel = updateChannel,
            onChannelClick = { showChannelDialog = true },
            autoCheckUpdate = autoCheckUpdate,
            onAutoCheckUpdateChange = { enabled ->
                autoCheckUpdate = enabled
                config.isAutoCheckUpdateEnabled = enabled
                if (enabled) {
                    haptic.perform(HapticFeedbackType.ToggleOn)
                } else {
                    haptic.perform(HapticFeedbackType.ToggleOff)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Section
        SettingsSectionCard {
            SettingsSectionTitle(title = stringResource(R.string.settings_section_privacy))

            SettingsSwitchItem(
                icon = Icons.Default.Analytics,
                title = stringResource(R.string.analytics_enabled),
                subtitle = stringResource(R.string.analytics_restart_hint),
                checked = analyticsEnabled,
                onCheckedChange = { enabled ->
                    analyticsEnabled = enabled
                    config.isAnalyticsEnabled = enabled
                    if (enabled) {
                        haptic.perform(HapticFeedbackType.ToggleOn)
                    } else {
                        haptic.perform(HapticFeedbackType.ToggleOff)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cache Section
        SettingsSectionCard {
            SettingsSectionTitle(title = stringResource(R.string.settings_section_storage))

            SettingsClickableItem(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.clear_cache),
                subtitle = if (cacheSize > 0) {
                    stringResource(R.string.cache_size_format, cacheSize)
                } else {
                    stringResource(R.string.clear_cache_desc)
                },
                onClick = { showClearCacheDialog = true }
            )

            HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

            // Export Logs
            SettingsClickableItem(
                icon = Icons.Default.BugReport,
                title = stringResource(R.string.export_logs),
                subtitle = stringResource(R.string.log_size, logSize),
                onClick = {
                    haptic.perform(HapticFeedbackType.Confirm)
                    LogExporter.exportAndShare(context) { success ->
                        if (success) {
                            logSize = LogExporter.getFormattedLogSize()
                        }
                    }
                }
            )
        }

        // Clear Cache Confirmation Dialog
        if (showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { showClearCacheDialog = false },
                title = { Text(stringResource(R.string.clear_cache)) },
                text = {
                    Text(
                        if (cacheSize > 0) {
                            stringResource(R.string.cache_clear_confirm, cacheSize)
                        } else {
                            stringResource(R.string.cache_empty)
                        }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            ImageCacheManager.clearCache(context)
                            cacheSize = 0.0
                            showClearCacheDialog = false
                            haptic.perform(HapticFeedbackType.Confirm)
                        },
                        enabled = cacheSize > 0
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCacheDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = themedCardBackground(),
                textContentColor = themedTextPrimary()
            )
        }

        // Clear Log Confirmation Dialog
        if (showClearLogDialog) {
            AlertDialog(
                onDismissRequest = { showClearLogDialog = false },
                title = { Text(stringResource(R.string.clear_logs)) },
                text = {
                    Text(stringResource(R.string.clear_logs_confirm_msg))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Logger.clearLogs()
                            logSize = LogExporter.getFormattedLogSize()
                            showClearLogDialog = false
                            haptic.perform(HapticFeedbackType.Confirm)
                            Toast.makeText(context, context.getString(R.string.logs_cleared), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearLogDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = themedCardBackground(),
                textContentColor = themedTextPrimary()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 设置页面版本更新检查卡片
 */
@Composable
private fun SettingsUpdateSection(
    updateChannel: UpdateChannel,
    onChannelClick: () -> Unit,
    autoCheckUpdate: Boolean,
    onAutoCheckUpdateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    var isChecking by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
    var checkError by remember { mutableStateOf<String?>(null) }
    var lastCheckTime by remember { mutableStateOf<Long?>(null) }
    
    // 下载相关状态
    var downloadId by remember { mutableStateOf<Long>(-1L) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }

    val currentVersionName = VersionInfo.VERSION_NAME
    val hasUpdate = updateInfo?.isNewer == true

    // 监听下载进度
    LaunchedEffect(isDownloading, downloadId) {
        if (isDownloading && downloadId != -1L) {
            while (isActive) {
                val (status, progress) = UpdateChecker.queryDownloadProgress(context, downloadId)
                downloadProgress = progress
                
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        isDownloading = false
                        downloadProgress = 100
                        break
                    }
                    DownloadManager.STATUS_FAILED -> {
                        isDownloading = false
                        downloadProgress = -1
                        break
                    }
                }
                
                if (!isDownloading) break
                delay(500)
            }
        }
    }

    val checkForUpdate = {
        scope.launch {
            isChecking = true
            checkError = null
            haptic.perform(HapticFeedbackType.TextHandleMove)
            try {
                val result = UpdateChecker.checkUpdate(context, VersionInfo.VERSION_CODE, updateChannel)
                if (result != null) {
                    updateInfo = result
                    lastCheckTime = System.currentTimeMillis()
                    if (result.isNewer) {
                        haptic.perform(HapticFeedbackType.Confirm)
                    }
                } else {
                    checkError = context.getString(R.string.version_check_failed)
                }
            } catch (e: Exception) {
                checkError = e.message ?: context.getString(R.string.version_check_failed)
            } finally {
                isChecking = false
            }
        }
    }

    SettingsSectionCard {
        SettingsSectionTitle(title = stringResource(R.string.settings_section_update))

        // 版本号与检查按钮卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDesign.ContentPadding)
                .padding(bottom = AppDesign.ContentPadding),
            colors = CardDefaults.cardColors(
                containerColor = if (hasUpdate) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                else 
                    themedCardBackground()
            ),
            shape = RoundedCornerShape(12.dp),
            border = if (hasUpdate) {
                androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            } else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 顶部：图标 + 版本号 + 刷新按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (hasUpdate) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                                    else 
                                        themedTextPrimary().copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasUpdate) Icons.Default.Download else Icons.Default.Update,
                                contentDescription = null,
                                tint = if (hasUpdate) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    themedTextSecondary().copy(alpha = 0.6f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "v$currentVersionName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = themedTextPrimary()
                            )
                            if (lastCheckTime != null && !isChecking) {
                                val diff = System.currentTimeMillis() - lastCheckTime!!
                                val timeText = when {
                                    diff < 60000 -> stringResource(R.string.time_just_now)
                                    diff < 3600000 -> stringResource(R.string.time_minutes_ago, diff / 60000)
                                    diff < 86400000 -> stringResource(R.string.time_hours_ago, diff / 3600000)
                                    else -> stringResource(R.string.time_days_ago, diff / 86400000)
                                }
                                Text(
                                    text = stringResource(R.string.last_check, timeText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themedTextSecondary().copy(alpha = 0.6f)
                                )
                            } else if (lastCheckTime == null) {
                                Text(
                                    text = stringResource(R.string.version_check_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themedTextSecondary().copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // 刷新/检查按钮
                    if (!isChecking && !isDownloading) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { checkForUpdate() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // 状态显示区域
                when {
                    isDownloading -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${updateInfo?.versionName ?: ""} - $downloadProgress%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.downloading),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themedTextSecondary()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    UpdateChecker.cancelDownload(context, downloadId)
                                    isDownloading = false
                                    downloadProgress = 0
                                    downloadId = -1L
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = stringResource(R.string.cancel),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    hasUpdate -> {
                        Column {
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
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "v${updateInfo?.versionName}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.new_version_available),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            // 更新日志
                            if (!updateInfo?.releaseNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = updateInfo?.releaseNotes ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themedTextPrimary().copy(alpha = 0.8f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    updateInfo?.let { info ->
                                        downloadId = UpdateChecker.downloadAndInstall(context, info.downloadUrl, info.versionName)
                                        isDownloading = true
                                        downloadProgress = 0
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(stringResource(R.string.version_download_btn))
                            }
                        }
                    }
                    checkError != null -> {
                        Row(
                            modifier = Modifier.clickable { checkForUpdate() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = checkError ?: stringResource(R.string.version_retry),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    lastCheckTime != null -> {
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
                                color = themedTextSecondary().copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

        // 更新渠道设置
        SettingsClickableItem(
            icon = Icons.Default.Cloud,
            title = stringResource(R.string.update_channel),
            subtitle = when (updateChannel) {
                UpdateChannel.GITEE -> stringResource(R.string.update_channel_gitee)
                UpdateChannel.GITHUB -> stringResource(R.string.update_channel_github)
            },
            onClick = onChannelClick
        )

        HorizontalDivider(color = themedTextSecondary().copy(alpha = 0.1f))

        // 自动检查更新开关
        SettingsSwitchItem(
            icon = Icons.Default.Update,
            title = stringResource(R.string.auto_check_update),
            subtitle = stringResource(R.string.auto_check_update_desc),
            checked = autoCheckUpdate,
            onCheckedChange = onAutoCheckUpdateChange
        )
    }
}

@Composable
private fun SettingsSectionCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDesign.ContentPadding),
        shape = AppDesign.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground().copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = AppDesign.ContentPadding, top = AppDesign.ContentPadding, bottom = AppDesign.ItemSpacing)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = themedTextPrimary()
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = themedTextSecondary()
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    uncheckedThumbColor = themedTextSecondary(),
                    uncheckedTrackColor = themedCardBackground()
                )
            )
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = themedTextPrimary()
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = themedTextSecondary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = trailingContent ?: {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = themedTextSecondary()
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun getTabName(tabIndex: Int): String {
    return when (tabIndex) {
        0 -> stringResource(R.string.tab_all)
        1 -> stringResource(R.string.tab_favorites)
        2 -> stringResource(R.string.tab_my)
        else -> stringResource(R.string.tab_all)
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: BrandTheme,
    onThemeSelected: (BrandTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.settings_theme_dialog_title))
        },
        text = {
            LazyColumn {
                items(BrandTheme.entries) { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = theme.primaryColor,
                                unselectedColor = themedTextSecondary()
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = stringResource(theme.brandNameResId),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(theme.colorNameResId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = themedCardBackground(),
        textContentColor = themedTextPrimary()
    )
}

@Composable
fun TabSelectionDialog(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tabs = listOf(
        context.getString(R.string.tab_all) to 0,
        context.getString(R.string.tab_favorites) to 1,
        context.getString(R.string.tab_my) to 2
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_title_select_tab))
        },
        text = {
            LazyColumn {
                items(tabs) { (name, index) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTabSelected(index) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (index == currentTab),
                            onClick = { onTabSelected(index) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = themedTextSecondary()
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = themedTextPrimary()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = themedCardBackground(),
        textContentColor = themedTextPrimary()
    )
}

@Composable
fun UpdateChannelDialog(
    currentChannel: UpdateChannel,
    onChannelSelected: (UpdateChannel) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_title_select_channel))
        },
        text = {
            LazyColumn {
                items(UpdateChannel.entries) { channel ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChannelSelected(channel) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (channel == currentChannel),
                            onClick = { onChannelSelected(channel) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = themedTextSecondary()
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = when (channel) {
                                    UpdateChannel.GITEE -> context.getString(R.string.update_channel_gitee)
                                    UpdateChannel.GITHUB -> context.getString(R.string.update_channel_github)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = themedTextPrimary()
                            )
                            Text(
                                text = when (channel) {
                                    UpdateChannel.GITEE -> "国内访问更快"
                                    UpdateChannel.GITHUB -> "国际访问，可能较慢"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = themedTextSecondary()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = themedCardBackground(),
        textContentColor = themedTextPrimary()
    )
}

@Composable
fun FloatingWindowModeDialog(
    currentMode: FloatingWindowMode,
    onModeSelected: (FloatingWindowMode) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dialog_title_select_floating_mode))
        },
        text = {
            LazyColumn {
                items(FloatingWindowMode.entries) { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == currentMode),
                            onClick = { onModeSelected(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = themedTextSecondary()
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = when (mode) {
                                    FloatingWindowMode.STANDARD -> context.getString(R.string.floating_window_mode_standard)
                                    FloatingWindowMode.COMPACT -> context.getString(R.string.floating_window_mode_compact)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = themedTextPrimary()
                            )
                            Text(
                                text = when (mode) {
                                    FloatingWindowMode.STANDARD -> context.getString(R.string.floating_window_mode_standard_desc)
                                    FloatingWindowMode.COMPACT -> context.getString(R.string.floating_window_mode_compact_desc)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = themedTextSecondary()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = themedCardBackground(),
        textContentColor = themedTextPrimary()
    )
}

@Composable
fun DarkModeSelectionDialog(
    currentMode: DarkMode,
    onModeSelected: (DarkMode) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.settings_dark_mode_dialog_title))
        },
        text = {
            LazyColumn {
                items(DarkMode.entries) { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == currentMode),
                            onClick = { onModeSelected(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = themedTextSecondary()
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = when (mode) {
                                DarkMode.SYSTEM -> context.getString(R.string.dark_mode_system)
                                DarkMode.LIGHT -> context.getString(R.string.dark_mode_light)
                                DarkMode.DARK -> context.getString(R.string.dark_mode_dark)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = themedTextPrimary()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = themedCardBackground(),
        textContentColor = themedTextPrimary()
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.language_dialog_title))
        },
        text = {
            LazyColumn {
                items(AppLanguage.entries) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (language == currentLanguage),
                            onClick = { onLanguageSelected(language) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = themedTextSecondary()
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.SYSTEM -> context.getString(R.string.language_system)
                                AppLanguage.CHINESE -> context.getString(R.string.language_chinese)
                                AppLanguage.ENGLISH -> context.getString(R.string.language_english)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = themedTextPrimary()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = themedCardBackground(),
        textContentColor = themedTextPrimary()
    )
}
