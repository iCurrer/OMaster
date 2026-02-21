package com.silas.omaster.ui.edit

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.animation.AnimationSpecs
import com.silas.omaster.ui.components.ModernSlider
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.HasselbladOrange
import com.silas.omaster.ui.theme.NearBlack
import com.silas.omaster.ui.theme.PureBlack
import androidx.compose.ui.res.stringResource
import com.silas.omaster.R
import com.silas.omaster.util.PresetI18n
import com.silas.omaster.util.formatFilterWithIntensity
import java.io.File
import com.silas.omaster.util.hapticClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.silas.omaster.util.perform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPresetScreen(
    presetId: String,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditPresetViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    // 滚动到顶/底部震感
    var lastScrollValue by remember { mutableIntStateOf(0) }
    var hasHapticAtTop by remember { mutableStateOf(false) }
    var hasHapticAtBottom by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        val currentValue = scrollState.value
        val maxValue = scrollState.maxValue

        if (currentValue == 0 && !hasHapticAtTop) {
            haptic.perform(HapticFeedbackType.TextHandleMove)
            hasHapticAtTop = true
            hasHapticAtBottom = false
        } else if (maxValue > 0 && currentValue >= maxValue && !hasHapticAtBottom) {
            haptic.perform(HapticFeedbackType.TextHandleMove)
            hasHapticAtBottom = true
            hasHapticAtTop = false
        } else if (currentValue > 0 && currentValue < maxValue) {
            hasHapticAtTop = false
            hasHapticAtBottom = false
        }
        lastScrollValue = currentValue
    }

    LaunchedEffect(presetId) {
        viewModel.loadPreset(presetId)
    }

    val preset by viewModel.preset.collectAsState()

    var name by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentCoverPath by remember { mutableStateOf<String?>(null) }
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
    var exposure by remember { mutableFloatStateOf(0f) }
    var colorTemperature by remember { mutableFloatStateOf(5500f) }
    var colorHue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(preset) {
        preset?.let { p ->
            name = p.name
            currentCoverPath = p.coverPath
            mode = p.mode
            val (baseFilter, intensity) = parseFilterWithIntensity(p.filter)
            filter = baseFilter
            filterIntensity = intensity
            softLight = p.softLight
            tone = p.tone.toFloat()
            saturation = p.saturation.toFloat()
            warmCool = p.warmCool.toFloat()
            cyanMagenta = p.cyanMagenta.toFloat()
            sharpness = p.sharpness.toFloat()
            vignette = p.vignette
            p.exposureCompensation?.toFloatOrNull()?.let { exposure = it }
            p.colorTemperature?.let { colorTemperature = it.toFloat() }
            p.colorHue?.let { colorHue = it.toFloat() }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            currentCoverPath = null
        }
    }

    val isFormValid = name.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_preset_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .hapticClickable { onBack() }
                    )
                },
                actions = {
                    Text(
                        text = stringResource(R.string.save),
                        color = if (isFormValid) HasselbladOrange else Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .hapticClickable(
                                type = HapticFeedbackType.Confirm,
                                enabled = isFormValid
                            ) {
                                val filterWithIntensity = formatFilterWithIntensity(filter, filterIntensity.toInt())
                                val success = viewModel.updatePreset(
                                    name = name,
                                    newImageUri = selectedImageUri,
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
                                    haptic.perform(HapticFeedbackType.Reject)
                                    android.widget.Toast.makeText(
                                        context,
                                        context.getString(R.string.save_failed),
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
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
                        .hapticClickable { imagePicker.launch("image/*") }
                        .border(
                            width = if (selectedImageUri == null && currentCoverPath == null) 2.dp else 0.dp,
                            color = if (selectedImageUri == null && currentCoverPath == null) DarkGray else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedImageUri != null || currentCoverPath != null,
                        enter = fadeIn(animationSpec = tween(AnimationSpecs.FadeInSpec.durationMillis)),
                        exit = fadeOut(animationSpec = tween(AnimationSpecs.FadeOutSpec.durationMillis))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                selectedImageUri != null -> {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "封面图片",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                currentCoverPath != null -> {
                                    val imageFile = File(context.filesDir, currentCoverPath!!)
                                    AsyncImage(
                                        model = imageFile,
                                        contentDescription = stringResource(R.string.cover_image),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
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
                                    text = stringResource(R.string.change_image),
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedImageUri == null && currentCoverPath == null,
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
                                text = stringResource(R.string.select_image),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.image_ratio_hint),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.preset_name), color = Color.White.copy(alpha = 0.6f)) },
                placeholder = { Text(stringResource(R.string.preset_name_hint), color = Color.White.copy(alpha = 0.3f)) },
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

            ParameterCard(title = stringResource(R.string.section_basic)) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(R.string.shooting_mode),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableChip(
                            text = "Auto",
                            selected = mode == "auto",
                            onClick = {
                                haptic.perform(HapticFeedbackType.ToggleOn)
                                mode = "auto"
                            }
                        )
                        SelectableChip(
                            text = "Pro",
                            selected = mode == "pro",
                            onClick = {
                                haptic.perform(HapticFeedbackType.ToggleOn)
                                mode = "pro"
                            }
                        )
                    }
                }

                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(R.string.filter_style),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
                                        text = PresetI18n.getLocalizedFilterNameOnly(option),
                                        selected = filter == option,
                                        onClick = {
                                            haptic.perform(HapticFeedbackType.ToggleOn)
                                            filter = option
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (filter != "标准") {
                        Spacer(modifier = Modifier.height(16.dp))
                        ModernSlider(
                            label = stringResource(R.string.filter_intensity),
                            value = filterIntensity,
                            range = 0f..100f,
                            onValueChange = { filterIntensity = it }
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = mode == "pro",
                    enter = fadeIn(animationSpec = tween(AnimationSpecs.NormalTween.durationMillis)),
                    exit = fadeOut(animationSpec = tween(AnimationSpecs.FadeOutSpec.durationMillis))
                ) {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = stringResource(R.string.section_pro),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ModernSlider(
                            label = stringResource(R.string.param_exposure),
                            value = exposure,
                            range = -3f..3f,
                            onValueChange = { exposure = it }
                        )

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.param_color_temp),
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
                                        fontWeight = FontWeight.SemiBold,
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

                        ModernSlider(
                            label = stringResource(R.string.param_tone),
                            value = colorHue,
                            range = -150f..150f,
                            onValueChange = { colorHue = it }
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.soft_light_effect),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("无", "柔美", "梦幻", "朦胧").forEach { option ->
                            SelectableChip(
                                text = PresetI18n.getLocalizedSoftLight(option),
                                selected = softLight == option,
                                onClick = {
                                    haptic.perform(HapticFeedbackType.ToggleOn)
                                    softLight = option
                                }
                            )
                        }
                    }
                }
            }

            ParameterCard(title = stringResource(R.string.section_color_grading)) {
                ModernSlider(label = stringResource(R.string.param_tone_curve), value = tone, range = -100f..100f, onValueChange = { tone = it })
                ModernSlider(label = stringResource(R.string.param_saturation), value = saturation, range = -100f..100f, onValueChange = { saturation = it })
                ModernSlider(label = stringResource(R.string.param_warm_cool), value = warmCool, range = -100f..100f, onValueChange = { warmCool = it })
                ModernSlider(label = stringResource(R.string.param_cyan_magenta), value = cyanMagenta, range = -100f..100f, onValueChange = { cyanMagenta = it })
                ModernSlider(label = stringResource(R.string.param_sharpness), value = sharpness, range = 0f..100f, onValueChange = { sharpness = it })
            }

            ParameterCard(title = stringResource(R.string.section_other)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.param_vignette),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableChip(
                            text = PresetI18n.getLocalizedVignette("开"),
                            selected = vignette == "开",
                            onClick = {
                                haptic.perform(HapticFeedbackType.ToggleOn)
                                vignette = "开"
                            }
                        )
                        SelectableChip(
                            text = PresetI18n.getLocalizedVignette("关"),
                            selected = vignette == "关",
                            onClick = {
                                haptic.perform(HapticFeedbackType.ToggleOn)
                                vignette = "关"
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    haptic.perform(HapticFeedbackType.Confirm)
                    val filterWithIntensity = formatFilterWithIntensity(filter, filterIntensity.toInt())
                    val success = viewModel.updatePreset(
                        name = name,
                        newImageUri = selectedImageUri,
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
                        haptic.perform(HapticFeedbackType.Reject)
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.save_failed),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
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
                        text = stringResource(R.string.save_changes),
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

private fun parseFilterWithIntensity(filterString: String): Pair<String, Float> {
    val intensityMatch = Regex("^(.+?)\\s*\\((\\d+)%\\)$").find(filterString)
    return if (intensityMatch != null) {
        val baseFilter = intensityMatch.groupValues[1]
        val intensity = intensityMatch.groupValues[2].toFloatOrNull() ?: 100f
        Pair(baseFilter, intensity)
    } else {
        Pair(filterString, 100f)
    }
}
