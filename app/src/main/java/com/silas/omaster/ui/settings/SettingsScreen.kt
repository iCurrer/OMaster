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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.data.local.SettingsManager
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.BrandTheme
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.PureBlack
import com.silas.omaster.util.HapticSettings

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

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
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
                defaultStartTab = tab
                settingsManager.defaultStartTab = tab
                showTabDialog = false
            },
            onDismiss = { showTabDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.settings_title),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        // General Section
        SettingsSectionHeader(title = stringResource(R.string.settings_section_general))

        // Vibration Setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val newValue = !vibrationEnabled
                    vibrationEnabled = newValue
                    settingsManager.isVibrationEnabled = newValue
                    HapticSettings.enabled = newValue
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.vibration_feedback),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Switch(
                checked = vibrationEnabled,
                onCheckedChange = { enabled ->
                    vibrationEnabled = enabled
                    settingsManager.isVibrationEnabled = enabled
                    HapticSettings.enabled = enabled
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }

        // Default Start Tab Setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTabDialog = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.default_start_tab),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            
            Text(
                text = getTabName(defaultStartTab),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Appearance Section
        SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))

        // Theme Setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showThemeDialog = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_theme_title),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color Dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(currentTheme.primaryColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(currentTheme.brandNameResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Floating Window Section
        SettingsSectionHeader(title = stringResource(R.string.settings_section_floating_window))

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
            
            Slider(
                value = floatingWindowOpacity.toFloat(),
                onValueChange = { 
                    floatingWindowOpacity = it.toInt()
                    settingsManager.floatingWindowOpacity = it.toInt()
                },
                valueRange = 30f..70f,
                steps = 39, // (70-30)/1 - 1 = 39 steps for integer values
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
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 16.dp)
    )
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
                        
                        // Color Preview
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
