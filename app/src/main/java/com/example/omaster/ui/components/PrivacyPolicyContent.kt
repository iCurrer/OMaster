package com.example.omaster.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.omaster.ui.theme.DarkGray
import com.example.omaster.ui.theme.HasselbladOrange

@Composable
fun PrivacyPolicyContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "隐私政策",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = HasselbladOrange,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        PolicySection(
            title = "欢迎使用 OMaster",
            content = "OMaster 是一款专为 OPPO/一加/Realme 手机摄影爱好者设计的调色参数管理工具。在这里，您可以查看、收藏和复现大师模式的摄影调色参数。"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PolicySection(
            title = "功能介绍",
            content = "• 查看大师模式调色参数\n• 支持多种预设风格\n• 纯本地化运作，数据存储在本地\n• 无需联网即可使用"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PolicySection(
            title = "数据收集说明",
            content = "为了提供更好的服务，我们使用了友盟统计分析 SDK 来收集应用使用数据。"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkGray
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "友盟 SDK 信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HasselbladOrange
                )

                Spacer(modifier = Modifier.height(8.dp))

                PolicyItem(
                    label = "使用 SDK 名称",
                    value = "友盟 SDK"
                )

                PolicyItem(
                    label = "服务类型",
                    value = "使用量统计分析"
                )

                PolicyItem(
                    label = "收集个人信息类型",
                    value = "设备信息（IMEI/MAC/Android ID/IDFA/OpenUDID/GUID/IP 地址/SIM 卡 IMSI 信息等）"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "隐私权政策链接：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HasselbladOrange,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "https://www.umeng.com/page/policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = HasselbladOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PolicySection(
            title = "用户权利",
            content = "您有权访问、更正、删除您的个人信息。如需行使这些权利，请通过应用内的联系方式与我们联系。"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PolicySection(
            title = "联系我们",
            content = "如果您对本隐私政策有任何疑问，请联系：\n开发者：Silas\n邮箱：[您的邮箱地址]"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "最后更新日期：2026-02-09",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
