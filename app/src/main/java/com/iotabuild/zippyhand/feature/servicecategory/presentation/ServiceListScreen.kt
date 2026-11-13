package com.iotabuild.zippyhand.feature.servicecategory.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotabuild.zippyhand.feature.home.presentation.ServiceCard
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import com.iotabuild.zippyhand.ui.theme.ZippyhandTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    categoryName: String,
    onBackClick: () -> Unit,
    onServiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val services = listOf(
        Service(
            productId = 1,
            categoryId = 1,
            title = "Light Replace & Repair",
            rating = 5.0,
            reviewCount = 130,
            price = 40,
            originalPrice = 45.0,
            providerName = "Robert Fox",
            providerImageUrl = "https://randomuser.me/api/portraits/men/1.jpg",
            imageUrl = "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            serviceType = "Indoor",
            status = true
        ),
        Service(
            productId = 2,
            categoryId = 1,
            title = "Switch Board Repair",
            rating = 5.0,
            reviewCount = 55,
            price = 20,
            originalPrice = 25.0,
            providerName = "Mitchel Marsh",
            providerImageUrl = "https://randomuser.me/api/portraits/men/2.jpg",
            imageUrl = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            serviceType = "Indoor",
            status = true
        ),
        Service(
            productId = 3,
            categoryId = 1,
            title = "AC Switchboard Installtion",
            rating = 5.0,
            reviewCount = 240,
            price = 35,
            originalPrice = 50.0,
            providerName = "Johnson Allen",
            providerImageUrl = "https://randomuser.me/api/portraits/men/3.jpg",
            imageUrl = "https://images.unsplash.com/photo-1527689368864-3a821dbccc34?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
            serviceType = "Indoor",
            status = true
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "$categoryName Services",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(services.chunked(2)) { pair ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    for (service in pair) {
                        ServiceCard(
                            service = service,
                            onClick = { onServiceClick(service.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceListScreenPreview() {
    ZippyhandTheme {
        ServiceListScreen(
            categoryName = "Electrician",
            onBackClick = {},
            onServiceClick = {}
        )
    }
}
