package com.iotabuild.zippyhand.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iotabuild.zippyhand.feature.home.data.remote.HomeResponse
import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import com.iotabuild.zippyhand.ui.common.FixkroErrorScreen
import com.iotabuild.zippyhand.utils.UiState
import org.koin.androidx.compose.koinViewModel

/**
 * Route Composable for Home Screen (Layer 1)
 * Gets ViewModel, collects state, wires callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onServiceClick: (Int) -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    LocationHeader(
                        locationAddress = "${location?.city}, ${location?.pinCode}",
                        isAvailable = location?.isServiceable ?: false,
                        onLocationClick = {}
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        when (homeState) {
            is UiState.Idle,
            is UiState.Loading -> {
                HomeScreenShimmer(modifier = Modifier.fillMaxSize().padding(paddingValues))
            }
            is UiState.Error -> {
                FixkroErrorScreen(
                    message = "Failed to load",
                    onRetry = {
                        viewModel.onRetry()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            is UiState.Success -> {

                val data = (homeState as UiState.Success<HomeResponse>).data

                LazyColumn(modifier = modifier.padding(paddingValues)) {

                    if (data.banner.isNotBlank()) {
                        item(key = "banner_section") {
                            ServicePromoBanner(
                                banner = data.banner,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (data.categories.isNotEmpty()) {
                        item(key = "categories_section") {
                            val currentCategory = selectedCategory ?: data.categories.firstOrNull()
                            currentCategory?.let { category ->
                                CategoryTabs(
                                    categories = data.categories,
                                    selectedCategory = category,
                                    onCategorySelect = {
                                        viewModel.onCategorySelected(it)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    if (data.isServiceLoading) {
                        item(key = "services_shimmer") {
                            HomeServiceShimmer()
                        }
                    } else {
                        items(
                            items = data.services.chunked(2),
                            key = { chunk -> chunk.map { it.productId }.joinToString("_") }
                        ) { pair ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (service in pair) {
                                    ServiceCard(
                                        service = service,
                                        onClick = { onServiceClick(service.productId) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("service_card_${service.productId}")
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun LocationHeader(
    locationAddress: String,
    isAvailable: Boolean,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .clickable { onLocationClick() }
    ) {
        Text(
            text = if (isAvailable) "1 Hour" else "Coming Soon",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Start
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location icon",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = locationAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Select location",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
