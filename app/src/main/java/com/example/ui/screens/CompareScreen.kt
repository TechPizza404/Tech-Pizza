package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.CompareResponse
import com.example.ui.CompareUiState
import com.example.ui.theme.MetricPositive
import com.example.ui.theme.WarningYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    uiState: CompareUiState,
    phoneA: String,
    phoneB: String,
    popularPhones: List<String>,
    onPhoneAChanged: (String) -> Unit,
    onPhoneBChanged: (String) -> Unit,
    onCompareTriggered: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputA by remember { mutableStateOf(phoneA) }
    var inputB by remember { mutableStateOf(phoneB) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Keep inputs synced
    LaunchedEffect(phoneA) { inputA = phoneA }
    LaunchedEffect(phoneB) { inputB = phoneB }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Compare Header
        Text(
            text = "Hardware Comparison Matrix",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Generate objective, parameters-driven, side-by-side analysis for two smartphones.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Select Inputs A & B
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = inputA,
                onValueChange = {
                    inputA = it
                    onPhoneAChanged(it)
                },
                label = { Text("Device A") },
                placeholder = { Text("e.g., iPhone 15 Pro") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("phone_a_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = inputB,
                onValueChange = {
                    inputB = it
                    onPhoneBChanged(it)
                },
                label = { Text("Device B") },
                placeholder = { Text("e.g., Pixel 8 Pro") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("phone_b_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Compare Button
        Button(
            onClick = {
                if (inputA.trim().isNotEmpty() && inputB.trim().isNotEmpty()) {
                    focusManager.clearFocus()
                    onCompareTriggered(inputA, inputB)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("compare_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            enabled = inputA.trim().isNotEmpty() && inputB.trim().isNotEmpty()
        ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = "Compare specs action")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EXECUTE COMPARISON MATRIX",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Popular combination buttons for rapid demonstration
        Text(
            text = "Popular Comparisons:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        val exampleComparisons = listOf(
            Pair("iPhone 15 Pro", "Galaxy S24 Ultra"),
            Pair("Pixel 8 Pro", "OnePlus 12"),
            Pair("Galaxy S24 Ultra", "Xiaomi 14 Ultra")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            exampleComparisons.forEach { pair ->
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            inputA = pair.first
                            inputB = pair.second
                            onPhoneAChanged(pair.first)
                            onPhoneBChanged(pair.second)
                            focusManager.clearFocus()
                            onCompareTriggered(pair.first, pair.second)
                        }
                ) {
                    Text(
                        text = "${pair.first} vs ${pair.second}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp),
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Render Compare States
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            when (uiState) {
                is CompareUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Compare Idle Icon",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Awaiting device specifications compare query.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Fill in specifications above or click one of the pre-composed comparison profiles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is CompareUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("compare_progress_bar")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Processing dual-device hardware parameters...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                is CompareUiState.Success -> {
                    val comp = uiState.comparison
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Winner banner card
                        ComparisonWinnerBanner(winner = comp.winner, phoneA = inputA, phoneB = inputB)

                        // Analytical summary
                        ComparisonSummaryCard(summary = comp.comparisonSummary)

                        // Side by side advantages
                        SideBySideAdvantagesCard(
                            phoneAName = inputA,
                            phoneBName = inputB,
                            advantages = comp.advantages
                        )

                        // Target audience guidelines
                        TargetAudienceJustificationCard(
                            phoneAName = inputA,
                            phoneBName = inputB,
                            targetAudience = comp.targetAudience
                        )
                    }
                }
                is CompareUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Comparison Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonWinnerBanner(winner: String, phoneA: String, phoneB: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.HighDensityVerdictBg
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BEST HARDWARE SPECIFICATIONS MATCH",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = androidx.compose.ui.graphics.Color(0xFF93C5FD), // text-blue-300
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = winner,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Black,
                color = if (winner.contains("Tie", ignoreCase = true)) com.example.ui.theme.WarningYellow else androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ComparisonSummaryCard(summary: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "OBJECTIVE EVALUATION SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SideBySideAdvantagesCard(
    phoneAName: String,
    phoneBName: String,
    advantages: com.example.api.ComparisonAdvantages
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "KEY STRENGTHS SIDE-BY-SIDE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Phone A Pros
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = phoneAName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    advantages.phoneA_advantages.forEach { adv ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "▪",
                                color = com.example.ui.theme.MetricPositive,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 6.dp),
                                fontSize = 12.sp
                            )
                            Text(
                                text = adv,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(IntrinsicSize.Max)
                )

                // Phone B Pros
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = phoneBName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    advantages.phoneB_advantages.forEach { adv ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "▪",
                                color = com.example.ui.theme.MetricPositive,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 6.dp),
                                fontSize = 12.sp
                            )
                            Text(
                                text = adv,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TargetAudienceJustificationCard(
    phoneAName: String,
    phoneBName: String,
    targetAudience: com.example.api.ComparisonTargetAudience
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TARGET AUDIENCE STRATEGIES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )

            // Buy Phone A
            Column {
                Text(
                    text = "CHOOSE $phoneAName IF:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = targetAudience.buyPhoneAIf,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Buy Phone B
            Column {
                Text(
                    text = "CHOOSE $phoneBName IF:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = targetAudience.buyPhoneBIf,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
