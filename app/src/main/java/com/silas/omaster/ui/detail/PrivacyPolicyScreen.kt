package com.silas.omaster.ui.detail

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.components.PolicyItem
import com.silas.omaster.ui.components.PolicySection
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.HasselbladOrange

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.privacy_title),
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
                title = stringResource(R.string.welcome_title),
                content = stringResource(R.string.welcome_desc)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 功能介绍
            PolicySection(
                title = stringResource(R.string.features_title),
                content = stringResource(R.string.features_list)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 数据收集说明
            PolicySection(
                title = stringResource(R.string.data_collection_title),
                content = stringResource(R.string.data_collection_desc)
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
                        text = stringResource(R.string.sdk_info_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HasselbladOrange
                    )

                    PolicyItem(
                        label = stringResource(R.string.sdk_name_label),
                        value = stringResource(R.string.sdk_name)
                    )

                    PolicyItem(
                        label = stringResource(R.string.service_type_label),
                        value = stringResource(R.string.service_type)
                    )

                    PolicyItem(
                        label = stringResource(R.string.collect_info_label),
                        value = stringResource(R.string.collect_info)
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.privacy_link_label),
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
                title = stringResource(R.string.user_rights_title),
                content = stringResource(R.string.user_rights_desc)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 联系我们
            PolicySection(
                title = stringResource(R.string.contact_title),
                content = stringResource(R.string.contact_info)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.last_updated, "2026-02-09"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
