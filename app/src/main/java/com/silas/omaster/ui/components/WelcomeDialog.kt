package com.silas.omaster.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.silas.omaster.R
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.HasselbladOrange
import com.silas.omaster.ui.theme.PureBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit,
    onViewPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var agreedToPolicy by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = stringResource(R.string.welcome_dialog_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.welcome_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    FeatureCard(
                        icon = "📷",
                        title = stringResource(R.string.feature_1_title),
                        description = stringResource(R.string.feature_1_desc)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureCard(
                        icon = "🎨",
                        title = stringResource(R.string.feature_2_title),
                        description = stringResource(R.string.feature_2_desc)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureCard(
                        icon = "🔒",
                        title = stringResource(R.string.feature_3_title),
                        description = stringResource(R.string.feature_3_desc)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkGray
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreedToPolicy,
                                    onCheckedChange = { agreedToPolicy = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = HasselbladOrange,
                                        uncheckedColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = stringResource(R.string.agree_prefix),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )

                                Text(
                                    text = stringResource(R.string.privacy_policy),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HasselbladOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .clickable { onViewPrivacyPolicy() }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.umeng_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.privacy_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = HasselbladOrange,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { onViewPrivacyPolicy() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isVisible = false
                            scope.launch {
                                delay(300)
                                onDisagree()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.disagree),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (agreedToPolicy) {
                                isVisible = false
                                scope.launch {
                                    delay(300)
                                    onAgree()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = agreedToPolicy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HasselbladOrange,
                            contentColor = Color.White,
                            disabledContainerColor = DarkGray,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.agree_and_start),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }
        }
    }
}
