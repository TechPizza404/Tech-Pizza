package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechXTopHeader() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "TX" mini logo
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            text = "TX",
                            color = androidx.compose.ui.graphics.Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            )
                        )
                    }
                }

                // TechX title and Analyst tag
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = "TechX",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ANALYST",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        actions = {
            Surface(
                color = com.example.ui.theme.MetricPositiveBg,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = "LIVE DATA",
                    color = com.example.ui.theme.MetricPositive,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 9.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.testTag("techx_top_app_bar")
    )
}

@Composable
fun MainLayout(viewModel: MainViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(0) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    val selectedPhone by viewModel.selectedPhone.collectAsState()
    val detailState by viewModel.detailState.collectAsState()

    val phoneA by viewModel.phoneA.collectAsState()
    val phoneB by viewModel.phoneB.collectAsState()
    val compareState by viewModel.compareState.collectAsState()

    val popularPhones = viewModel.popularPhones

    Scaffold(
        topBar = {
            TechXTopHeader()
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Tab") },
                    label = { Text("Search") },
                    modifier = Modifier.testTag("nav_search_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Detail Tab") },
                    label = { Text("Details") },
                    modifier = Modifier.testTag("nav_details_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Refresh, contentDescription = "Compare Tab") },
                    label = { Text("Compare") },
                    modifier = Modifier.testTag("nav_compare_tab")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> SearchScreen(
                    uiState = searchState,
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearchTriggered = { viewModel.searchPhones(it) },
                    onViewDetail = { model ->
                        viewModel.loadPhoneSpecs(model)
                        currentTab = 1
                    }
                )
                1 -> DetailScreen(
                    uiState = detailState,
                    selectedPhone = selectedPhone,
                    popularPhones = popularPhones,
                    onPhoneSelected = { viewModel.loadPhoneSpecs(it) }
                )
                2 -> CompareScreen(
                    uiState = compareState,
                    phoneA = phoneA,
                    phoneB = phoneB,
                    popularPhones = popularPhones,
                    onPhoneAChanged = { viewModel.updatePhoneA(it) },
                    onPhoneBChanged = { viewModel.updatePhoneB(it) },
                    onCompareTriggered = { a, b -> viewModel.executeComparison(a, b) }
                )
            }
        }
    }
}

