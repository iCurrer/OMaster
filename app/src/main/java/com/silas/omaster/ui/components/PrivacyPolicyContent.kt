package com.silas.omaster.ui.components

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
import androidx.compose.ui.res.stringResource
import com.silas.omaster.R
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.HasselbladOrange

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
            text = stringResource(R.string.privacy_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = HasselbladOrange,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        PolicySection(
            title = stringResource(R.string.welcome_title),
            content = stringResource(R.string.welcome_desc)
        )

        Spacer(modifier = Modifier.height(12.dp))

        PolicySection(
            title = stringResource(R.string.features_title),
            content = stringResource(R.string.features_list)
        )

        Spacer(modifier = Modifier.height(12.dp))

        PolicySection(
            title = stringResource(R.string.data_collection_title),
            content = stringResource(R.string.data_collection_desc)
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
                    text = stringResource(R.string.sdk_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HasselbladOrange
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.privacy_link_label),
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
            title = stringResource(R.string.user_rights_title),
            content = stringResource(R.string.user_rights_desc)
        )

        Spacer(modifier = Modifier.height(12.dp))

        PolicySection(
            title = stringResource(R.string.contact_title),
            content = stringResource(R.string.contact_info)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.last_updated, "2026-02-09"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
