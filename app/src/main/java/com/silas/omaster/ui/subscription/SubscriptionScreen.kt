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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
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
import com.silas.omaster.data.config.ConfigCenter
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.Subscription
import com.silas.omaster.network.PresetRemoteManager
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.CardBorderLight
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.PureBlack
import com.silas.omaster.util.Logger
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val config = remember { ConfigCenter.getInstance(context) }
    val subscriptions by config.subscriptionsFlow.collectAsState()
    val scope = rememberCoroutineScope()
    
    var refreshing by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Subscription?>(null) }
    var selectedSubscription by remember { mutableStateOf<Subscription?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
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
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (refreshing) return@launch
                                refreshing = true
                                Logger.i("SubscriptionScreen", "开始手动刷新订阅，共 ${subscriptions.size} 个订阅")
                                var successCount = 0
                                var upToDateCount = 0
                                var failCount = 0
                                val enabledSubs = subscriptions.filter { it.isEnabled }
                                Logger.d("SubscriptionScreen", "启用的订阅: ${enabledSubs.size} 个")
                                for (sub in enabledSubs) {
                                    Logger.d("SubscriptionScreen", "正在更新: ${sub.name} (${sub.url})")
                                    val result = PresetRemoteManager.fetchAndSave(context, sub.url)
                                    if (result.isSuccess) {
                                        successCount++
                                        Logger.i("SubscriptionScreen", "更新成功: ${sub.name}")
                                    } else if (result.exceptionOrNull()?.message == "无需更新") {
                                        upToDateCount++
                                        Logger.d("SubscriptionScreen", "已是最新: ${sub.name}")
                                    } else {
                                        failCount++
                                        val error = result.exceptionOrNull()?.message ?: "未知错误"
                                        Logger.w("SubscriptionScreen", "更新失败: ${sub.name}, 错误: $error")
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
                                    Logger.i("SubscriptionScreen", "刷新完成: 成功=$successCount, 最新=$upToDateCount, 失败=$failCount")
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                } else {
                                    Logger.d("SubscriptionScreen", "没有启用的订阅，跳过刷新")
                                }
                                refreshing = false
                            }
                        },
                        enabled = !refreshing && subscriptions.isNotEmpty()
                    ) {
                        if (refreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新订阅",
                                tint = if (subscriptions.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
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
                                onToggle = { config.toggleSubscription(sub.url) },
                                onClick = {
                                    selectedSubscription = sub
                                    showBottomSheet = true
                                }
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

        if (showBottomSheet && selectedSubscription != null) {
            SubscriptionDetailBottomSheet(
                sub = selectedSubscription!!,
                onDismiss = { showBottomSheet = false },
                sheetState = sheetState,
                onEdit = {
                    showEditDialog = selectedSubscription
                    showBottomSheet = false
                },
                onDelete = {
                    config.removeSubscription(selectedSubscription!!.url)
                    showBottomSheet = false
                }
            )
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
                            config.addSubscription(
                                url = url,
                                name = presetList.name ?: "",
                                author = presetList.author ?: "",
                                build = presetList.build
                            )
                            // 再次更新状态，确保 presetCount 等信息正确（因为 fetchAndSave 时可能还没 add）
                            config.updateSubscriptionStatus(
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

        if (showEditDialog != null) {
            EditSubscriptionDialog(
                sub = showEditDialog!!,
                onDismiss = { showEditDialog = null },
                onConfirm = { oldUrl, newUrl ->
                    showEditDialog = null
                    scope.launch {
                        config.updateSubscriptionUrl(oldUrl, newUrl)
                        // 更新 URL 后需要重新拉取
                        val result = PresetRemoteManager.fetchAndSave(context, newUrl, forceUpdate = true)
                        result.onSuccess { presetList ->
                            config.updateSubscriptionStatus(
                                url = newUrl,
                                presetCount = presetList.presets.size,
                                lastUpdateTime = System.currentTimeMillis(),
                                name = presetList.name,
                                author = presetList.author,
                                build = presetList.build
                            )
                            PresetRepository.getInstance(context).reloadDefaultPresets()
                            Toast.makeText(context, "订阅更新成功", Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            errorMsg = error.message ?: "更新失败"
                        }
                    }
                }
            )
        }

        if (errorMsg != null) {
            AlertDialog(
                onDismissRequest = { errorMsg = null },
                title = { Text("操作失败") },
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
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
                
                Switch(
                    checked = sub.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailBottomSheet(
    sub: Subscription,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkGray,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.sub_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureBlack.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow(label = "名称", value = if (sub.name.isNotEmpty()) sub.name else "未命名")
                    DetailRow(label = "作者", value = sub.author)
                    DetailRow(label = "Build", value = sub.build.toString())
                    DetailRow(label = "预设数量", value = sub.presetCount.toString())
                    
                    if (sub.lastUpdateTime > 0) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        DetailRow(label = "最后更新", value = sdf.format(Date(sub.lastUpdateTime)))
                    }
                    
                    DetailRow(label = "链接", value = sub.url, isLink = true)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.edit))
                }

                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
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
fun DetailRow(label: String, value: String, isLink: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isLink) MaterialTheme.colorScheme.primary else Color.White,
            maxLines = if (isLink) 2 else 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EditSubscriptionDialog(
    sub: Subscription,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var url by remember { mutableStateOf(sub.url) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sub_edit_title)) },
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
                onClick = { if (url.isNotEmpty()) onConfirm(sub.url, url) },
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
