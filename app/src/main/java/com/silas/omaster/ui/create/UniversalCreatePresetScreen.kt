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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.silas.omaster.util.PresetI18n

import java.io.File
import kotlin.math.roundToInt

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
    
    // Unsaved changes dialog
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    
    // Delete confirmation dialog
    var sectionToDelete by remember { mutableIntStateOf(-1) }
    var itemToDelete by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Check for unsaved changes
    fun hasUnsavedChanges(): Boolean {
        return uiState.name.isNotBlank() || 
               uiState.sections.isNotEmpty() || 
               uiState.imageUri != null
    }
    
    // Handle back navigation with unsaved changes check
    fun handleBack() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) stringResource(R.string.edit_preset_title) else stringResource(R.string.create_preset_title)) },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    val isSaveEnabled = uiState.name.isNotBlank() && (uiState.imageUri != null || uiState.originalCoverPath != null)
                    
                    // Show validation hint when save is disabled
                    if (!isSaveEnabled && uiState.name.isNotBlank()) {
                        Text(
                            text = "请上传封面图片",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    
                    TextButton(
                        onClick = {
                            if (viewModel.savePreset()) {
                                onSave()
                            }
                        },
                        enabled = isSaveEnabled
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSectionDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_section))
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
                        Text(stringResource(R.string.section_basic), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Cover Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.DarkGray)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.imageUri != null) {
                                AsyncImage(
                                    model = uiState.imageUri,
                                    contentDescription = stringResource(R.string.cover_image),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (uiState.originalCoverPath != null) {
                                val context = LocalContext.current
                                val imageModel = remember(uiState.originalCoverPath) {
                                      val path = uiState.originalCoverPath ?: ""
                                      when {
                                          path.startsWith("http") -> path
                                          path.startsWith("/") -> File(path) // Absolute path
                                          path.startsWith("presets/") -> File(context.filesDir, path)
                                          else -> "file:///android_asset/$path"
                                      }
                                  }
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = stringResource(R.string.cover_image),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(R.string.upload_cover_hint), color = Color.White)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text(stringResource(R.string.preset_name)) },
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
                    onRemoveSection = { sectionToDelete = index },
                    onEditItem = { itemIndex, item ->
                        currentSectionIndex = index
                        currentItemIndex = itemIndex
                        editingItem = item
                        showAddItemDialog = true
                    },
                    onRemoveItem = { itemIndex ->
                        itemToDelete = index to itemIndex
                    }
                )
            }
            
            // 底部留白，防止 FAB 遮挡
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddSectionDialog) {
        var newSectionTitle by remember { mutableStateOf("") }
        var selectedSectionType by remember { mutableStateOf<String?>(null) }
        
        // ✅ 常用分区预设（精简版，去掉容易混淆的"基础参数"）
        val commonSections = listOf(
            "调色参数" to "@string/section_color_grading",  // JSON 预设使用的标准名称
            "专业参数" to "@string/section_pro",            // Pro 模式专用
            "自定义分区" to null
        )
        
        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text(stringResource(R.string.add_section)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 分区类型选择
                    Text(
                        text = "选择常用分区：",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    commonSections.forEach { (name, resource) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSectionType = resource
                                    newSectionTitle = resource ?: ""
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSectionType == resource,
                                onClick = {
                                    selectedSectionType = resource
                                    newSectionTitle = resource ?: ""
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    // 自定义输入提示
                    if (selectedSectionType == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newSectionTitle,
                            onValueChange = { 
                                newSectionTitle = it
                                selectedSectionType = null
                            },
                            label = { Text(stringResource(R.string.section_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("或手动输入分区名称") }
                        )
                        
                        // ✅ 帮助提示
                        Text(
                            text = "💡 提示：\n• 调色参数：包含滤镜、柔光、饱和度等 8 个常用调色参数（推荐）\n• 专业参数：ISO、快门、曝光等专业模式参数（仅 Pro 模式）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSectionTitle.isNotBlank()) {
                        viewModel.addSection(newSectionTitle)
                        showAddSectionDialog = false
                    }
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAddItemDialog) {
        var label by remember { mutableStateOf(editingItem?.label ?: "") }
        var value by remember { mutableStateOf(editingItem?.value ?: "") }
        var span by remember { mutableIntStateOf(editingItem?.span ?: 1) }
        
        // 获取当前分区类型
        val currentSection = uiState.sections.getOrNull(currentSectionIndex)
        val isProSection = currentSection?.title?.contains("pro") == true || 
                          currentSection?.title?.contains("专业") == true ||
                          currentSection?.title?.contains("@string/section_pro") == true
        
        // 根据分区类型设置不同的示例
        val paramLabelHint = if (isProSection) "例如：ISO、快门、曝光补偿" else "例如：滤镜、柔光、影调"
        val paramLabelExamples = if (isProSection) {
            "支持中文或英文，例如：ISO、快门、曝光补偿、色温、白平衡、色调"
        } else {
            "支持中文或英文，例如：滤镜、柔光、影调、饱和度、冷暖、锐度、暗角"
        }
        val paramValueHint = if (isProSection) "例如：100、1/125、-0.7" else "例如：复古、+3、100%"
        val paramValueExamples = if (isProSection) {
            "ISO值：100/200/400/800/1600\n快门速度：1/125/1/60/1/30\n曝光补偿：-2.0/-1.0/+0.7/+1.0"
        } else {
            "滤镜值：复古/霓虹/通透/明艳/冷调/暖调/浓郁/黑白/原图\n强度值：+3/-2 或 100%"
        }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text(if (editingItem == null) stringResource(R.string.add_item) else stringResource(R.string.edit_item)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 参数名输入
                    Column {
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("参数名") },
                            placeholder = { Text(paramLabelHint) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = paramLabelExamples,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                    
                    // 参数值输入
                    Column {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text("参数值") },
                            placeholder = { Text(paramValueHint) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = paramValueExamples,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                    
                    // 宽度占比设置 - 使用 Switch 更直观
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "显示宽度",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (span == 1) "半宽" else "全宽",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = span == 2,
                                    onCheckedChange = { span = if (it) 2 else 1 },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        uncheckedThumbColor = Color(0xFFE0E0E0),
                                        uncheckedTrackColor = Color(0xFF757575)
                                    )
                                )
                            }
                        }
                        Text(
                            text = "半宽 = 两个参数并排显示 | 全宽 = 独占一行",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (label.isNotBlank() && value.isNotBlank()) {
                            val newItem = PresetItem(label, value, span)
                            if (editingItem == null) {
                                viewModel.addItemToSection(currentSectionIndex, newItem)
                            } else {
                                viewModel.updateItemInSection(currentSectionIndex, currentItemIndex, newItem)
                            }
                            showAddItemDialog = false
                        }
                    },
                    enabled = label.isNotBlank() && value.isNotBlank()
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // 未保存更改确认对话框
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("放弃更改？") },
            text = { Text("您有未保存的更改，确定要放弃吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onBack()
                    }
                ) {
                    Text("放弃", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text("继续编辑")
                }
            }
        )
    }
    
    // 删除分区确认对话框
    if (sectionToDelete >= 0) {
        AlertDialog(
            onDismissRequest = { sectionToDelete = -1 },
            title = { Text("确认删除分区") },
            text = { Text("确定要删除这个分区吗？分区内的所有参数也将被删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeSection(sectionToDelete)
                        sectionToDelete = -1
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sectionToDelete = -1 }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 删除参数确认对话框
    itemToDelete?.let { (sectionIdx, itemIdx) ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("确认删除参数") },
            text = { Text("确定要删除这个参数吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeItemFromSection(sectionIdx, itemIdx)
                        itemToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
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
    val context = LocalContext.current
    
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
                // ✅ 解析分区标题的资源 ID
                Text(
                    text = section.title?.let { PresetI18n.resolveString(context, it) } 
                        ?: stringResource(R.string.unnamed_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemoveSection) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = stringResource(R.string.remove_section), 
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            section.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { onEditItem(index, item) }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // ✅ 解析参数标签的资源 ID
                        Text(
                            text = PresetI18n.resolveString(context, item.label),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // ✅ 解析参数值的资源 ID
                        Text(
                            text = PresetI18n.resolveValue(context, item.value),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { onRemoveItem(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_item), modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            OutlinedButton(
                onClick = onAddItem,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add_item))
            }
        }
    }
}
