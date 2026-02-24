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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) stringResource(R.string.edit_preset_title) else stringResource(R.string.create_preset_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(stringResource(R.string.cancel), color = Color.Gray)
                    }
                },
                actions = {
                    val isSaveEnabled = uiState.name.isNotBlank() && (uiState.imageUri != null || uiState.originalCoverPath != null)
                    TextButton(
                        onClick = {
                            if (viewModel.savePreset()) {
                                onSave()
                            }
                        },
                        enabled = isSaveEnabled
                    ) {
                        Text(stringResource(R.string.save), color = if (isSaveEnabled) MaterialTheme.colorScheme.primary else Color.Gray)
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
                                .clip(RoundedCornerShape(8.dp))
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
            title = { Text(stringResource(R.string.add_section)) },
            text = {
                OutlinedTextField(
                    value = newSectionTitle,
                    onValueChange = { newSectionTitle = it },
                    label = { Text(stringResource(R.string.section_name)) }
                )
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

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text(if (editingItem == null) stringResource(R.string.add_item) else stringResource(R.string.edit_item)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(stringResource(R.string.param_label)) }
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text(stringResource(R.string.param_value)) }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.span_label, span))
                        Slider(
                            value = span.toFloat(),
                            onValueChange = { 
                                // Use roundToInt to make selection of 2 easier
                                span = it.roundToInt() 
                            },
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
                    text = section.title ?: stringResource(R.string.unnamed_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRemoveSection) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.remove_section), tint = Color.Red)
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
