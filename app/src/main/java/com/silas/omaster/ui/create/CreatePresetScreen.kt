package com.silas.omaster.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.silas.omaster.ui.animation.AnimationSpecs
import com.silas.omaster.ui.components.ModernSlider
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.HasselbladOrange
import com.silas.omaster.ui.theme.NearBlack
import com.silas.omaster.ui.theme.PureBlack
import com.silas.omaster.util.formatFilterWithIntensity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePresetScreen(
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreatePresetViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 表单状态
    var name by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var mode by remember { mutableStateOf("auto") }
    var filter by remember { mutableStateOf("标准") }
    var filterIntensity by remember { mutableFloatStateOf(100f) }
    var softLight by remember { mutableStateOf("无") }
    var tone by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0f) }
    var warmCool by remember { mutableFloatStateOf(0f) }
    var cyanMagenta by remember { mutableFloatStateOf(0f) }
    var sharpness by remember { mutableFloatStateOf(0f) }
    var vignette by remember { mutableStateOf("关") }

    // Pro 模式特有参数
    var exposure by remember { mutableFloatStateOf(0f) }
    var colorTemperature by remember { mutableFloatStateOf(5500f) }
    var colorHue by remember { mutableFloatStateOf(0f) }

    // 图片选择器
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val isFormValid = name.isNotBlank() && selectedImageUri != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "新建预设",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    Text(
                        text = "取消",
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable { onBack() }
                    )
                },
                actions = {
                    Text(
                        text = "保存",
                        color = if (isFormValid) HasselbladOrange else Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable(enabled = isFormValid) {
                                selectedImageUri?.let { uri ->
                                    val filterWithIntensity = formatFilterWithIntensity(filter, filterIntensity.toInt())
                                    val success = viewModel.createPreset(
                                        name = name,
                                        imageUri = uri,
                                        mode = mode,
                                        filter = filterWithIntensity,
                                        softLight = softLight,
                                        tone = tone.toInt(),
                                        saturation = saturation.toInt(),
                                        warmCool = warmCool.toInt(),
                                        cyanMagenta = cyanMagenta.toInt(),
                                        sharpness = sharpness.toInt(),
                                        vignette = vignette,
                                        exposure = if (mode == "pro") exposure else null,
                                        colorTemperature = if (mode == "pro") colorTemperature else null,
                                        colorHue = if (mode == "pro") colorHue else null
                                    )
                                    if (success) {
                                        onSave()
                                    } else {
                                        // 显示错误提示（可以在这里添加 Toast 或 Snackbar）
                                        android.widget.Toast.makeText(
                                            context,
                                            "保存失败，请重试",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack,
                    titleContentColor = Color.White
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        containerColor = PureBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 图片选择区域 - 更高级的设计
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NearBlack
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { imagePicker.launch("image/*") }
                        .border(
                            width = if (selectedImageUri == null) 2.dp else 0.dp,
                            color = if (selectedImageUri == null) DarkGray else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 使用轻量级的动画规格
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedImageUri != null,
                        enter = fadeIn(animationSpec = tween(AnimationSpecs.FadeInSpec.durationMillis)),
                        exit = fadeOut(animationSpec = tween(AnimationSpecs.FadeOutSpec.durationMillis))
                    ) {
                        selectedImageUri?.let { uri ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "封面图片",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // 渐变遮罩
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.4f)
                                                ),
                                                startY = 150f
                                            )
                                        )
                                )
                                // 更换图片提示
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "点击更换图片",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedImageUri == null,
                        enter = fadeIn(animationSpec = tween(AnimationSpecs.FadeInSpec.durationMillis)),
                        exit = fadeOut(animationSpec = tween(AnimationSpecs.FadeOutSpec.durationMillis))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DarkGray)
                                    .border(
                                        width = 2.dp,
                                        color = HasselbladOrange.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = HasselbladOrange,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "选择封面图片",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "建议使用 16:9 比例的图片",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 预设名称 - 更简洁的设计
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("预设名称", color = Color.White.copy(alpha = 0.6f)) },
                placeholder = { Text("给你的预设起个名字", color = Color.White.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HasselbladOrange,
                    unfocusedBorderColor = DarkGray,
                    cursorColor = HasselbladOrange,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            // 基础参数卡片
            ParameterCard(title = "基础参数") {
                // 模式选择 - 使用 Chip
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "拍摄模式",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableChip(
                            text = "Auto",
                            selected = mode == "auto",
                            onClick = { mode = "auto" }
                        )
                        SelectableChip(
                            text = "Pro",
                            selected = mode == "pro",
                            onClick = { mode = "pro" }
                        )
                    }
                }

                // 滤镜选择
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "滤镜风格",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // 滤镜选项网格布局
                    val filterOptions = listOf(
                        "标准", "霓虹", "清新", "复古", "通透", "明艳",
                        "童话", "人文", "自然", "美味", "冷调", "暖调",
                        "浓郁", "高级灰", "黑白", "单色", "赛博朋克"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filterOptions.chunked(4).forEach { rowOptions ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowOptions.forEach { option ->
                                    SelectableChip(
                                        text = option,
                                        selected = filter == option,
                                        onClick = { filter = option }
                                    )
                                }
                            }
                        }
                    }

                    // 滤镜强度滑块
                    if (filter != "标准") {
                        Spacer(modifier = Modifier.height(16.dp))
                        ModernSlider(
                            label = "滤镜强度",
                            value = filterIntensity,
                            range = 0f..100f,
                            onValueChange = { filterIntensity = it }
                        )
                    }
                }

                // Pro 模式特有参数 - 使用优化的动画规格
                androidx.compose.animation.AnimatedVisibility(
                    visible = mode == "pro",
                    enter = fadeIn(animationSpec = tween(AnimationSpecs.NormalTween.durationMillis)),
                    exit = fadeOut(animationSpec = tween(AnimationSpecs.FadeOutSpec.durationMillis))
                ) {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "专业参数",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 曝光补偿
                        ModernSlider(
                            label = "曝光补偿",
                            value = exposure,
                            range = -3f..3f,
                            onValueChange = { exposure = it }
                        )

                        // 色温
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "色温",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                                androidx.compose.material3.Surface(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    color = DarkGray
                                ) {
                                    Text(
                                        text = "${colorTemperature.toInt()}K",
                                        color = HasselbladOrange,
                                        fontSize = 13.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = colorTemperature,
                                onValueChange = { colorTemperature = it },
                                valueRange = 2000f..8000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = HasselbladOrange,
                                    activeTrackColor = HasselbladOrange,
                                    inactiveTrackColor = DarkGray.copy(alpha = 0.5f),
                                    activeTickColor = Color.Transparent,
                                    inactiveTickColor = Color.Transparent
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // 色调
                        ModernSlider(
                            label = "色调",
                            value = colorHue,
                            range = -150f..150f,
                            onValueChange = { colorHue = it }
                        )
                    }
                }

                // 柔光选择
                Column {
                    Text(
                        text = "柔光效果",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("无", "柔美", "梦幻", "朦胧").forEach { option ->
                            SelectableChip(
                                text = option,
                                selected = softLight == option,
                                onClick = { softLight = option }
                            )
                        }
                    }
                }
            }

            // 调色参数卡片
            ParameterCard(title = "调色参数") {
                ModernSlider(label = "影调", value = tone, range = -100f..100f, onValueChange = { tone = it })
                ModernSlider(label = "饱和度", value = saturation, range = -100f..100f, onValueChange = { saturation = it })
                ModernSlider(label = "冷暖", value = warmCool, range = -100f..100f, onValueChange = { warmCool = it })
                ModernSlider(label = "青品", value = cyanMagenta, range = -100f..100f, onValueChange = { cyanMagenta = it })
                ModernSlider(label = "锐度", value = sharpness, range = 0f..100f, onValueChange = { sharpness = it })
            }

            // 暗角选择
            ParameterCard(title = "其他") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "暗角",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableChip(
                            text = "开启",
                            selected = vignette == "开",
                            onClick = { vignette = "开" }
                        )
                        SelectableChip(
                            text = "关闭",
                            selected = vignette == "关",
                            onClick = { vignette = "关" }
                        )
                    }
                }
            }

            // 底部保存按钮
            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        val filterWithIntensity = formatFilterWithIntensity(filter, filterIntensity.toInt())
                        val success = viewModel.createPreset(
                            name = name,
                            imageUri = uri,
                            mode = mode,
                            filter = filterWithIntensity,
                            softLight = softLight,
                            tone = tone.toInt(),
                            saturation = saturation.toInt(),
                            warmCool = warmCool.toInt(),
                            cyanMagenta = cyanMagenta.toInt(),
                            sharpness = sharpness.toInt(),
                            vignette = vignette,
                            exposure = if (mode == "pro") exposure else null,
                            colorTemperature = if (mode == "pro") colorTemperature else null,
                            colorHue = if (mode == "pro") colorHue else null
                        )
                        if (success) {
                            onSave()
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "保存失败，请重试",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HasselbladOrange,
                    disabledContainerColor = DarkGray.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isFormValid) 4.dp else 0.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "保存预设",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ParameterCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = NearBlack
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HasselbladOrange.copy(alpha = 0.2f),
            selectedLabelColor = HasselbladOrange,
            containerColor = DarkGray,
            labelColor = Color.White.copy(alpha = 0.8f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) HasselbladOrange else DarkGray,
            selectedBorderColor = HasselbladOrange
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

