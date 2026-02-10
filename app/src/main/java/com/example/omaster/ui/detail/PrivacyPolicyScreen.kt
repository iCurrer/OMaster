package com.example.omaster.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.omaster.ui.components.OMasterTopAppBar
import com.example.omaster.ui.components.PolicyItem
import com.example.omaster.ui.components.PolicySection
import com.example.omaster.ui.theme.DarkGray
import com.example.omaster.ui.theme.HasselbladOrange

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OMasterTopAppBar(
            title = "用户协议和隐私政策",
            onBack = onBack,
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 欢迎语
            PolicySection(
                title = "欢迎使用 OMaster",
                content = "OMaster 是一款专为 OPPO/一加/Realme 手机摄影爱好者设计的调色参数管理工具。在这里，您可以查看、收藏和复现大师模式的摄影调色参数。"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 功能介绍
            PolicySection(
                title = "功能介绍",
                content = "• 查看大师模式调色参数\n• 支持多种预设风格\n• 纯本地化运作，数据存储在本地\n• 无需联网即可使用"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 数据收集说明
            PolicySection(
                title = "数据收集说明",
                content = "为了提供更好的服务，我们使用了友盟统计分析 SDK 来收集应用使用数据。"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 友盟 SDK 信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkGray
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "友盟 SDK 信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HasselbladOrange
                    )

                    PolicyItem(
                        label = "使用 SDK 名称",
                        value = "友盟 SDK"
                    )

                    PolicyItem(
                        label = "服务类型",
                        value = "统计分析"
                    )

                    PolicyItem(
                        label = "收集个人信息类型",
                        value = "设备信息（MAC/Android ID/IDFA/GUID/IP信息等）"
                    )

                    Column {
                        Text(
                            text = "隐私权政策链接：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "https://www.umeng.com/page/policy",
                            style = MaterialTheme.typography.bodyMedium,
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

            Spacer(modifier = Modifier.height(16.dp))

            // 用户权利
            PolicySection(
                title = "用户权利",
                content = "您有权访问、更正、删除您的个人信息。如需行使这些权利，请通过应用内的联系方式与我们联系。"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 联系我们
            PolicySection(
                title = "联系我们",
                content = "如果您对本隐私政策有任何疑问，请联系：\n开发者：Silas \n邮箱：iboy66lee@qq.com"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "最后更新日期：2026-02-09",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
