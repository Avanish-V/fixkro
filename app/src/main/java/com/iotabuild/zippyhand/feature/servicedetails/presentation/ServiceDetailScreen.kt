package com.iotabuild.zippyhand.feature.servicedetails.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iotabuild.zippyhand.feature.reviews.domain.Review
import com.iotabuild.zippyhand.feature.servicedetails.domain.DescriptionStep
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceDetailsPage
import com.iotabuild.zippyhand.ui.common.FixkroErrorScreen
import com.iotabuild.zippyhand.ui.theme.ZippyhandTheme
import com.iotabuild.zippyhand.utils.UiState
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun ServiceDetailScreen(
    productId: Int,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServiceDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.fetchServiceDetail(productId)
    }

    Scaffold(
        bottomBar = {
            if (uiState is UiState.Success) {
                val service = (uiState as UiState.Success<ServiceDetailsPage>).data.service
                BottomBookingBar(price = service.price.toDouble(), onBookClick = onBookClick)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (val state = uiState) {
                is UiState.Loading, UiState.Idle -> {
                    ServiceDetailShimmer()
                }
                is UiState.Error -> {
                    FixkroErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.fetchServiceDetail(productId) },
                        modifier = Modifier
                    )
                }
                is UiState.Success -> {
                    val service = state.data.service
                    val reviews = state.data.reviews
                    ServiceDetailContent(
                        service = service,
                        reviews = reviews,
                        onBackClick = onBackClick
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceDetailContent(
    service: Service,
    reviews: List<Review>,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = service.imageUrl,
                    contentDescription = "Service Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .statusBarsPadding()
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = service.serviceType,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", service.rating),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " (${reviews.size} Reviews)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "₹${service.price.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (service.originalPrice > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "₹${service.originalPrice.toInt()}",
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val savings = ((service.originalPrice - service.price) / service.originalPrice * 100).toInt()
                        Text(
                            text = "$savings% OFF",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                if (service.descriptions.isNotEmpty()) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val descriptionSteps = service.descriptions.map { desc ->
                        DescriptionStep(
                            title = desc.title,
                            description = desc.shortDescription
                        )
                    }
                    ServiceDescriptionStepper(steps = descriptionSteps)
                    Spacer(modifier = Modifier.height(32.dp))
                }

                ServiceHighlights()
                
                Spacer(modifier = Modifier.height(32.dp))

                val distribution = remember(reviews) {
                    if (reviews.isEmpty()) {
                        mapOf(5 to 1f, 4 to 0f, 3 to 0f, 2 to 0f, 1 to 0f)
                    } else {
                        val counts = reviews.groupingBy { it.rating.toInt().coerceIn(1, 5) }.eachCount()
                        (1..5).associateWith { star ->
                            (counts[star] ?: 0).toFloat() / reviews.size
                        }
                    }
                }

                RatingSummary(
                    rating = service.rating,
                    reviewCount = if (reviews.isEmpty()) 0 else reviews.size,
                    distribution = distribution
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "What our customers say",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (reviews.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reviews yet. Be the first to review!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(reviews) { review ->
                ReviewItem(
                    review = review,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceDetailScreenPreview() {
    ZippyhandTheme {
        ServiceDetailScreen(
            productId = 1,
            onBackClick = {},
            onBookClick = {}
        )
    }
}
