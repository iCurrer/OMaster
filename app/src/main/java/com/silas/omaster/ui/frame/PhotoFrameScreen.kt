package com.silas.omaster.ui.frame

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop169
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silas.omaster.R
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import com.silas.omaster.util.ImageExporter

@Composable
fun PhotoFrameScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FrameViewModel = viewModel(factory = FrameViewModelFactory(context))
    val state by viewModel.state.collectAsState()

    var dateTimeText by remember { mutableStateOf("") }
    var exifSynced by remember { mutableStateOf(false) }
    var useRounded by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            exifSynced = false
            viewModel.loadImage(it)
        }
    }

    LaunchedEffect(state.dateTime) {
        if (!exifSynced && state.dateTime != null) {
            dateTimeText = state.dateTime ?: ""
            exifSynced = true
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themedBackground())
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.photoframe_card_title),
            onBack = onBack,
            actions = {
                if (state.renderedBitmap != null) {
                    TextButton(
                        onClick = {
                            val bitmap = state.renderedBitmap ?: return@TextButton
                            val success = ImageExporter.saveToGallery(context, bitmap)
                            val msg = if (success) "已保存到相册" else "保存失败"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                        Text("保存")
                    }
                }
            }
        )

        if (state.sourceBitmap == null) {
            EmptyPickerContent(onPickImage = { imagePickerLauncher.launch("image/*") })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(AppDesign.ScreenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    PreviewArea(state = state)

                    Spacer(Modifier.height(AppDesign.SectionSpacing))

                    EditPanel(
                        dateTimeText = dateTimeText,
                        useRounded = useRounded,
                        onDateTimeChange = {
                            dateTimeText = it
                            viewModel.updateTitle(it)
                        },
                        onRoundedChanged = {
                            useRounded = it
                            viewModel.toggleRoundedCorners(it)
                        },
                        onPickNewImage = {
                            exifSynced = false
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPickerContent(onPickImage: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(AppDesign.ScreenPadding),
            colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
            shape = RoundedCornerShape(20.dp),
            onClick = onPickImage
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.PhotoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("选择照片", style = MaterialTheme.typography.titleLarge, color = themedTextPrimary(), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("自动读取拍摄时间，生成精美分享图", style = MaterialTheme.typography.bodyMedium, color = themedTextSecondary(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PreviewArea(state: FrameState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDesign.MediumRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (state.renderedBitmap != null) {
            val img = remember(state.renderedBitmap) { state.renderedBitmap.asImageBitmap() }
            Image(bitmap = img, contentDescription = "预览图", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth())
        } else if (state.sourceBitmap != null) {
            val img = remember(state.sourceBitmap) { state.sourceBitmap.asImageBitmap() }
            Image(bitmap = img, contentDescription = "原始图", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EditPanel(
    dateTimeText: String,
    useRounded: Boolean,
    onDateTimeChange: (String) -> Unit,
    onRoundedChanged: (Boolean) -> Unit,
    onPickNewImage: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = themedTextPrimary(),
        unfocusedTextColor = themedTextPrimary()
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
        shape = RoundedCornerShape(AppDesign.LargeRadius)
    ) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = dateTimeText,
                onValueChange = onDateTimeChange,
                label = { Text("拍摄时间") },
                placeholder = { Text("如：2024.03.15 14:30") },
                leadingIcon = { Icon(Icons.Default.Schedule, null, tint = themedTextSecondary(), modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text("图片样式", style = MaterialTheme.typography.bodyMedium, color = themedTextPrimary())

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyleButton(
                    icon = Icons.Default.Crop169,
                    label = "圆角",
                    selected = useRounded,
                    onClick = { onRoundedChanged(true) },
                    modifier = Modifier.weight(1f)
                )
                StyleButton(
                    icon = Icons.Default.CropSquare,
                    label = "直角",
                    selected = !useRounded,
                    onClick = { onRoundedChanged(false) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onPickNewImage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                Text("重新选择图片")
            }
        }
    }
}

@Composable
private fun StyleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else themedCardBackground()
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, null,
                tint = if (selected) MaterialTheme.colorScheme.primary else themedTextSecondary(),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else themedTextSecondary(),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
