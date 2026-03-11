package com.silas.omaster.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.data.local.SettingsManager
import com.silas.omaster.data.local.UpdateChannel
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.BrandTheme
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.PureBlack
import com.silas.omaster.util.HapticSettings
import com.silas.omaster.util.perform

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    var vibrationEnabled by remember { mutableStateOf(settingsManager.isVibrationEnabled) }
    val currentTheme by settingsManager.themeFlow.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTabDialog by remember { mutableStateOf(false) }
    var floatingWindowOpacity by remember { mutableStateOf(settingsManager.floatingWindowOpacity) }
    var defaultStartTab by remember { mutableStateOf(settingsManager.defaultStartTab) }
    var updateChannel by remember { mutableStateOf(settingsManager.updateChannel) }
    var showChannelDialog by remember { mutableStateOf(false) }
    var analyticsEnabled by remember { mutableStateOf(settingsManager.isAnalyticsEnabled) }
    val haptic = LocalHapticFeedback.current

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                haptic.perform(HapticFeedbackType.Confirm)
                settingsManager.currentTheme = theme
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
                settingsManager.defaultStartTab = tab
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
                settingsManager.updateChannel = channel
                updateChannel = channel
                showChannelDialog = false
            },
            onDismiss = { showChannelDialog = false }
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(scrollState)
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.settings_title),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

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
                    settingsManager.isVibrationEnabled = enabled
                    HapticSettings.enabled = enabled
                    if (enabled) {
                        haptic.perform(HapticFeedbackType.ToggleOn)
                    }
                }
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Default Start Tab Setting
            SettingsClickableItem(
                icon = Icons.Default.DashboardCustomize,
                title = stringResource(R.string.default_start_tab),
                subtitle = getTabName(defaultStartTab),
                onClick = { showTabDialog = true }
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Floating Window Section
        SettingsSectionCard {
            SettingsSectionTitle(title = stringResource(R.string.settings_section_floating_window))

            // Floating Window Opacity Setting
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.floating_window_opacity),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
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
                        settingsManager.floatingWindowOpacity = newValue
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
                        color = Color.Gray
                    )
                    Text(
                        text = "70%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Update Section
        SettingsSectionCard {
            SettingsSectionTitle(title = "更新设置")

            SettingsClickableItem(
                icon = Icons.Default.Update,
                title = "更新渠道",
                subtitle = when (updateChannel) {
                    UpdateChannel.GITEE -> "Gitee（国内推荐）"
                    UpdateChannel.GITHUB -> "GitHub（国际）"
                },
                onClick = { showChannelDialog = true }
            )
        }

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
                    settingsManager.isAnalyticsEnabled = enabled
                    if (enabled) {
                        haptic.perform(HapticFeedbackType.ToggleOn)
                    } else {
                        haptic.perform(HapticFeedbackType.ToggleOff)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray.copy(alpha = 0.5f)
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
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
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
                color = Color.White
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
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
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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
                color = Color.White
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
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
                tint = Color.Gray
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
        0 -> "全部"
        1 -> "收藏"
        2 -> "我的"
        else -> "全部"
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
                                unselectedColor = Color.Gray
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
        containerColor = DarkGray,
        textContentColor = Color.White
    )
}

@Composable
fun TabSelectionDialog(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val tabs = listOf("全部" to 0, "收藏" to 1, "我的" to 2)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "选择默认启动页面")
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
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
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
        containerColor = DarkGray,
        textContentColor = Color.White
    )
}

@Composable
fun UpdateChannelDialog(
    currentChannel: UpdateChannel,
    onChannelSelected: (UpdateChannel) -> Unit,
    onDismiss: () -> Unit
) {
    val channels = listOf(
        UpdateChannel.GITEE to "Gitee（国内推荐）",
        UpdateChannel.GITHUB to "GitHub（国际）"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "选择更新渠道")
        },
        text = {
            LazyColumn {
                items(channels) { (channel, name) ->
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
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Text(
                                text = when (channel) {
                                    UpdateChannel.GITEE -> "国内访问速度快"
                                    UpdateChannel.GITHUB -> "国际访问，国内可能需要代理"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
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
        containerColor = DarkGray,
        textContentColor = Color.White
    )
}
