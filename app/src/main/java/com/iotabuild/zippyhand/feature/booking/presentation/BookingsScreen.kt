package com.iotabuild.zippyhand.feature.booking.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iotabuild.zippyhand.ui.common.FixkroErrorScreen
import com.iotabuild.zippyhand.ui.theme.ZippyhandTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onBookingClick: (Long) -> Unit = {},
    onScrollChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = koinViewModel()
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollState) {
        var lastIndex = 0
        var lastOffset = 0
        
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .map { (index, offset) ->
                val isScrollingDown = if (index != lastIndex) {
                    index > lastIndex
                } else {
                    offset > lastOffset
                }
                lastIndex = index
                lastOffset = offset
                !isScrollingDown
            }
            .distinctUntilChanged()
            .collect { isVisible ->
                if (scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 50) {
                     onScrollChanged(isVisible)
                } else {
                    onScrollChanged(true)
                }
            }
    }
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    var orderIdToCancel by remember { mutableStateOf(-1L) }

    if (showCancelDialog && orderIdToCancel != -1L) {
        CancellationReasonSheet(
            onDismiss = { 
                showCancelDialog = false
                orderIdToCancel = -1L
            },
            onConfirm = { reason ->
                viewModel.cancelBooking(orderIdToCancel, reason)
                showCancelDialog = false
                orderIdToCancel = -1L
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "My Bookings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is BookingsUiState.Loading -> {
                BookingsListShimmer()
            }
            is BookingsUiState.Error -> {
                FixkroErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.fetchBookings() },
                    modifier = Modifier.weight(1f)
                )
            }
            is BookingsUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshBookings() },
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.bookings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No bookings found.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            state = scrollState,
                            contentPadding = PaddingValues(bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.bookings) { booking ->
                                BookingCard(
                                    booking = booking,
                                    onCardClick = {
                                        onBookingClick(booking.id)
                                    },
                                    modifier = Modifier
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingsScreenPreview() {
    ZippyhandTheme {
        Text("Bookings Screen Preview")
    }
}
