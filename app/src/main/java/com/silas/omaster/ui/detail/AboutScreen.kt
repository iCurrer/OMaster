package com.silas.omaster.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.HasselbladOrange

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    var previousScrollValue by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // 检测滚动方向，上滑返回 true，下滑返回 false
    val isScrollingUp by remember {
        derivedStateOf {
            val currentScroll = scrollState.value
            val isUp = currentScroll <= previousScrollValue
            previousScrollValue = currentScroll
            isUp
        }
    }

    LaunchedEffect(isScrollingUp) {
        onScrollStateChanged(isScrollingUp)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OMasterTopAppBar(
            title = "关于",
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = HasselbladOrange)) {
                        append("O")
                    }
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append("Master")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "大师模式调色参数库 v1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkGray
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "功能介绍",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HasselbladOrange
                    )
                    Text(
                        text = "OMaster 是一款专为OPPO/一加/Realme手机摄影爱好者设计的调色参数管理工具。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "您可以在这里查看、收藏和复现大师模式的摄影调色参数，包括曝光、对比度、饱和度、色温等专业参数。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkGray
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "制作信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HasselbladOrange
                    )
                    Text(
                        text = "开发者：Silas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "素材提供：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "@OPPO影像",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HasselbladOrange,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://xhslink.com/m/8c2gJYGlCTR"))
                                context.startActivity(intent)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "纯本地化运作，数据存储在本地",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Text(
                        text = "查看《用户协议和隐私政策》",
                        style = MaterialTheme.typography.bodySmall,
                        color = HasselbladOrange,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.umeng.com/page/policy"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
