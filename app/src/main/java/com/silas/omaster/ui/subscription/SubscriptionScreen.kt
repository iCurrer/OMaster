package com.silas.omaster.ui.subscription

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silas.omaster.R
import com.silas.omaster.data.local.SubscriptionManager
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.Subscription
import com.silas.omaster.network.PresetRemoteManager
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.CardBorderLight
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.PureBlack
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val subManager = remember { SubscriptionManager.getInstance(context) }
    val subscriptions by subManager.subscriptionsFlow.collectAsState()
    val scope = rememberCoroutineScope()
    
    var refreshing by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                var successCount = 0
                var upToDateCount = 0
                val enabledSubs = subscriptions.filter { it.isEnabled }
                for (sub in enabledSubs) {
                    val result = PresetRemoteManager.fetchAndSave(context, sub.url)
                    if (result.isSuccess) {
                        successCount++
                    } else if (result.exceptionOrNull()?.message == "无需更新") {
                        upToDateCount++
                    }
                }
                if (enabledSubs.isNotEmpty()) {
                    PresetRepository.getInstance(context).reloadDefaultPresets()
                    val message = when {
                        successCount > 0 && upToDateCount > 0 -> "成功更新 ${successCount} 个，${upToDateCount} 个已是最新"
                        successCount > 0 -> "成功更新 ${successCount} 个订阅"
                        upToDateCount > 0 -> "所有订阅均已是最新"
                        else -> "更新失败，请检查网络"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                refreshing = false
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OMasterTopAppBar(
                title = stringResource(R.string.sub_title),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )

            Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
                if (subscriptions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.sub_empty), color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(subscriptions, key = { it.url }) { sub ->
                            SubscriptionItem(
                                sub = sub,
                                onToggle = { subManager.toggleSubscription(sub.url) },
                                onDelete = { subManager.removeSubscription(sub.url) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
                .size(64.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.sub_add), modifier = Modifier.size(32.dp))
        }

        if (showAddDialog) {
            AddSubscriptionDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { url ->
                    showAddDialog = false
                    scope.launch {
                        // 添加新订阅时强制更新 (forceUpdate = true)，以确保能正确导入并验证
                        val result = PresetRemoteManager.fetchAndSave(context, url, forceUpdate = true)
                        result.onSuccess { presetList ->
                            subManager.addSubscription(
                                url = url,
                                name = presetList.name ?: "",
                                author = presetList.author ?: "",
                                build = presetList.build
                            )
                            // 再次更新状态，确保 presetCount 等信息正确（因为 fetchAndSave 时可能还没 add）
                            subManager.updateSubscriptionStatus(
                                url = url,
                                presetCount = presetList.presets.size,
                                lastUpdateTime = System.currentTimeMillis(),
                                name = presetList.name,
                                author = presetList.author,
                                build = presetList.build
                            )
                            PresetRepository.getInstance(context).reloadDefaultPresets()
                            Toast.makeText(context, "订阅添加成功", Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            errorMsg = error.message ?: "导入失败"
                        }
                    }
                }
            )
        }

        if (errorMsg != null) {
            AlertDialog(
                onDismissRequest = { errorMsg = null },
                title = { Text("导入失败") },
                text = { Text(errorMsg ?: "未知错误") },
                confirmButton = {
                    TextButton(onClick = { errorMsg = null }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

@Composable
fun SubscriptionItem(
    sub: Subscription,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (sub.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else CardBorderLight,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = DarkGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (sub.name.isNotEmpty()) sub.name else stringResource(R.string.sub_no_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (sub.isEnabled) Color.White else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "作者: ${sub.author} | Build: ${sub.build}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = sub.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = sub.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.Gray)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sub_preset_count, sub.presetCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                if (sub.lastUpdateTime > 0) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    Text(
                        text = stringResource(R.string.sub_last_update, sdf.format(Date(sub.lastUpdateTime))),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.sub_delete_confirm_title)) },
            text = { Text(stringResource(R.string.sub_delete_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sub_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.sub_url_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (url.isNotEmpty()) onConfirm(url) },
                enabled = url.isNotEmpty()
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
