package com.silas.omaster.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.silas.omaster.R
import com.silas.omaster.model.PresetItem
import com.silas.omaster.model.PresetSection
import com.silas.omaster.ui.theme.HasselbladOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalCreatePresetScreen(
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: UniversalCreatePresetViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateImageUri(uri)
    }

    // Dialog states
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var currentSectionIndex by remember { mutableIntStateOf(-1) }
    var currentItemIndex by remember { mutableIntStateOf(-1) }
    
    // Edit item state
    var editingItem by remember { mutableStateOf<PresetItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建预设") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("取消", color = Color.Gray)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (viewModel.savePreset()) {
                                onSave()
                            }
                        },
                        enabled = uiState.name.isNotBlank() && uiState.imageUri != null
                    ) {
                        Text("保存", color = if (uiState.name.isNotBlank() && uiState.imageUri != null) HasselbladOrange else Color.Gray)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSectionDialog = true },
                containerColor = HasselbladOrange
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Section")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 基本信息
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("基本信息", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Cover Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.imageUri != null) {
                                AsyncImage(
                                    model = uiState.imageUri,
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("点击上传封面", color = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("预设名称") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 2. Sections List
            itemsIndexed(uiState.sections) { index, section ->
                SectionCard(
                    section = section,
                    onAddItem = {
                        currentSectionIndex = index
                        currentItemIndex = -1
                        editingItem = null
                        showAddItemDialog = true
                    },
                    onRemoveSection = { viewModel.removeSection(index) },
                    onEditItem = { itemIndex, item ->
                        currentSectionIndex = index
                        currentItemIndex = itemIndex
                        editingItem = item
                        showAddItemDialog = true
                    },
                    onRemoveItem = { itemIndex ->
                        viewModel.removeItemFromSection(index, itemIndex)
                    }
                )
            }
            
            // 底部留白，防止 FAB 遮挡
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddSectionDialog) {
        var newSectionTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text("添加分组") },
            text = {
                OutlinedTextField(
                    value = newSectionTitle,
                    onValueChange = { newSectionTitle = it },
                    label = { Text("分组标题") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSectionTitle.isNotBlank()) {
                        viewModel.addSection(newSectionTitle)
                        showAddSectionDialog = false
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showAddItemDialog) {
        var label by remember { mutableStateOf(editingItem?.label ?: "") }
        var value by remember { mutableStateOf(editingItem?.value ?: "") }
        var span by remember { mutableIntStateOf(editingItem?.span ?: 1) }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text(if (editingItem == null) "添加参数" else "编辑参数") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("参数名 (Label)") }
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("参数值 (Value)") }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("宽度占比 (Span): $span")
                        Slider(
                            value = span.toFloat(),
                            onValueChange = { span = it.toInt() },
                            valueRange = 1f..2f,
                            steps = 0
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (label.isNotBlank() && value.isNotBlank()) {
                        val newItem = PresetItem(label, value, span)
                        if (editingItem == null) {
                            viewModel.addItemToSection(currentSectionIndex, newItem)
                        } else {
                            viewModel.updateItemInSection(currentSectionIndex, currentItemIndex, newItem)
                        }
                        showAddItemDialog = false
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SectionCard(
    section: PresetSection,
    onAddItem: () -> Unit,
    onRemoveSection: () -> Unit,
    onEditItem: (Int, PresetItem) -> Unit,
    onRemoveItem: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title ?: "未命名分组",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemoveSection) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Section", tint = Color.Red)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            section.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .clickable { onEditItem(index, item) }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = item.value, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onRemoveItem(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Item", modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            OutlinedButton(
                onClick = onAddItem,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加参数")
            }
        }
    }
}
