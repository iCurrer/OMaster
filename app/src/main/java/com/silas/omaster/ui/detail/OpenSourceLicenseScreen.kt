package com.silas.omaster.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary

@Composable
fun OpenSourceLicenseScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themedBackground())
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.open_source_license_title),
            onBack = onBack,
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 本应用许可协议
            AppLicenseCard()

            // 使用的开源库
            OpenSourceLibrariesCard()

            // 致谢
            AcknowledgementsCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AppLicenseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.app_license_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themedTextPrimary()
                )
            }

            Text(
                text = stringResource(R.string.app_license_name),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.app_license_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = themedTextPrimary().copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            )

            // CC BY-NC-SA 4.0 核心条款
            LicenseTermsSection()

            // 查看完整协议链接
            val context = LocalContext.current
            Text(
                text = stringResource(R.string.view_full_license),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh")
                    )
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun LicenseTermsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.license_terms_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = themedTextSecondary().copy(alpha = 0.7f)
        )

        LicenseTermItem(
            icon = Icons.Default.AccountBalance,
            title = stringResource(R.string.term_attribution),
            desc = stringResource(R.string.term_attribution_desc)
        )

        LicenseTermItem(
            icon = Icons.Default.Info,
            title = stringResource(R.string.term_non_commercial),
            desc = stringResource(R.string.term_non_commercial_desc)
        )

        LicenseTermItem(
            icon = Icons.Default.LibraryBooks,
            title = stringResource(R.string.term_share_alike),
            desc = stringResource(R.string.term_share_alike_desc)
        )
    }
}

@Composable
private fun LicenseTermItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = themedTextPrimary()
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary().copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun OpenSourceLibrariesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.libraries_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themedTextPrimary()
                )
            }

            Text(
                text = stringResource(R.string.libraries_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = themedTextPrimary().copy(alpha = 0.8f)
            )

            // 开源库列表
            OpenSourceLibraryList()
        }
    }
}

@Composable
private fun OpenSourceLibraryList() {
    val libraries = listOf(
        LibraryInfo(
            name = "Jetpack Compose",
            license = "Apache License 2.0",
            link = "https://developer.android.com/jetpack/compose"
        ),
        LibraryInfo(
            name = "Kotlin",
            license = "Apache License 2.0",
            link = "https://kotlinlang.org/"
        ),
        LibraryInfo(
            name = "Coil",
            license = "Apache License 2.0",
            link = "https://coil-kt.github.io/coil/"
        ),
        LibraryInfo(
            name = "Gson",
            license = "Apache License 2.0",
            link = "https://github.com/google/gson"
        ),
        LibraryInfo(
            name = "Kotlinx Serialization",
            license = "Apache License 2.0",
            link = "https://github.com/Kotlin/kotlinx.serialization"
        ),
        LibraryInfo(
            name = "Ktor",
            license = "Apache License 2.0",
            link = "https://ktor.io/"
        ),
        LibraryInfo(
            name = "AndroidX Lifecycle",
            license = "Apache License 2.0",
            link = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
        ),
        LibraryInfo(
            name = "AndroidX Navigation",
            license = "Apache License 2.0",
            link = "https://developer.android.com/jetpack/androidx/releases/navigation"
        ),
        LibraryInfo(
            name = "AndroidX Activity",
            license = "Apache License 2.0",
            link = "https://developer.android.com/jetpack/androidx/releases/activity"
        ),
        LibraryInfo(
            name = "友盟统计 UMEng",
            license = "自定义许可证",
            link = "https://www.umeng.com/"
        ),
        LibraryInfo(
            name = "Material Design Icons",
            license = "Apache License 2.0",
            link = "https://fonts.google.com/icons"
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        libraries.forEach { library ->
            LibraryItem(library = library)
        }
    }
}

@Composable
private fun LibraryItem(library: LibraryInfo) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(library.link))
                context.startActivity(intent)
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = library.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = themedTextPrimary()
            )
            Text(
                text = library.license,
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary().copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

private data class LibraryInfo(
    val name: String,
    val license: String,
    val link: String
)

@Composable
private fun AcknowledgementsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.acknowledgements_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themedTextPrimary()
                )
            }

            Text(
                text = stringResource(R.string.acknowledgements_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = themedTextPrimary().copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            )

            Text(
                text = stringResource(R.string.acknowledgements_thanks),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
