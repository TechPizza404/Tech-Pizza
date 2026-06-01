package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.PhoneDetailResponse
import com.example.api.PhoneDetailSpecs
import com.example.api.PhoneDetailReview
import com.example.ui.DetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    selectedPhone: String,
    popularPhones: List<String>,
    onPhoneSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customInput by remember { mutableStateOf(selectedPhone) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Sync input field value if selectedPhone changes from external screen (e.g. search screen)
    LaunchedEffect(selectedPhone) {
        customInput = selectedPhone
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Platform Specs Header
        Text(
            text = "Smartphone Specifications",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Request deep technical audits and dynamic reviews for any mobile device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom device input
        OutlinedTextField(
            value = customInput,
            onValueChange = { customInput = it },
            label = { Text("Enter Model Name (e.g. Galaxy S24 Ultra)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("detail_text_input"),
            trailingIcon = {
                if (customInput.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onPhoneSelected(customInput)
                        },
                        modifier = Modifier.testTag("get_specs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Search Specs Action"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Popular models rapid picker chips
        Text(
            text = "Curated Flagships:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(popularPhones) { model ->
                ElevatedFilterChip(
                    selected = selectedPhone == model,
                    onClick = {
                        customInput = model
                        focusManager.clearFocus()
                        onPhoneSelected(model)
                    },
                    label = {
                        Text(
                            text = model,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Render Specs Response States
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            when (uiState) {
                is DetailUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Specs Idle Icon",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No smartphone model selected.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Choose from the flagships above or type in a target device spec audit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is DetailUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("detail_progress_bar")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aggregating mobile hardware node parameters...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                is DetailUiState.Success -> {
                    val detail = uiState.detail
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Hero Info
                        SpecsHeroHeader(brand = detail.brand, model = detail.model, releaseDate = detail.releaseDate)

                        // Hardware spec modules grid
                        TechnicalSpecsGridSection(specs = detail.specs)

                        // Unbiased Review card
                        UnbiasedHardwareAuditCard(review = detail.review)
                    }
                }
                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Specification Retrieval Failure",
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
fun SpecsHeroHeader(brand: String, model: String, releaseDate: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = brand.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (releaseDate.isNotEmpty()) {
                    Text(
                        text = "Released: $releaseDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = model,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TechnicalSpecsGridSection(specs: PhoneDetailSpecs) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "AUDITED TECHNICAL SPECIFICATIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid items
            val itemsList = listOf(
                SpecItemData("Display", specs.display, Icons.Default.PlayArrow),
                SpecItemData("Processor", specs.processor, Icons.Default.Info),
                SpecItemData("Memory (RAM)", specs.ram, Icons.Default.Build),
                SpecItemData("Storage Options", specs.storage, Icons.Default.Menu),
                SpecItemData("Battery & Charging", specs.battery, Icons.Default.Warning),
                SpecItemData("Main Camera array", specs.cameraMain, Icons.Default.Star),
                SpecItemData("Selfie Camera lens", specs.cameraSelfie, Icons.Default.AccountBox)
            )

            itemsList.forEach { spec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = spec.label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = spec.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = spec.value,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
        }
    }
}

data class SpecItemData(val label: String, val value: String, val icon: ImageVector)

@Composable
fun UnbiasedHardwareAuditCard(review: PhoneDetailReview) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pros & Cons Outlined Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = "SPEC ADVANTAGES & LIMITATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Dynamic Pros List
                if (review.pros.isNotEmpty()) {
                    Text(
                        text = "ADVANTAGES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.MetricPositive,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    review.pros.forEach { pro ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "▪",
                                color = com.example.ui.theme.MetricPositive,
                                modifier = Modifier.padding(end = 8.dp),
                                fontSize = 12.sp
                            )
                            Text(
                                text = pro,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Dynamic Cons List
                if (review.cons.isNotEmpty()) {
                    Text(
                        text = "LIMITATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.MetricNegative,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    review.cons.forEach { con ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "▪",
                                color = com.example.ui.theme.MetricNegative,
                                modifier = Modifier.padding(end = 8.dp),
                                fontSize = 12.sp
                            )
                            Text(
                                text = con,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Deep Navy Blue Verdict Card matching Design HTML styling
        if (review.summary.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.example.ui.theme.HighDensityVerdictBg
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Gavel alternative icon",
                            tint = androidx.compose.ui.graphics.Color(0xFF93C5FD), // text-blue-300
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "OBJECTIVE VERDICT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFF93C5FD),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = review.summary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 18.sp),
                        fontWeight = FontWeight.Normal,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f)
                    )
                }
            }
        }
    }
}
