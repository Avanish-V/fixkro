package com.iotabuild.zippyhand.feature.booking.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iotabuild.zippyhand.feature.address.presentation.AddressSelectionSheet
import com.iotabuild.zippyhand.feature.address.presentation.AddAddressSheet
import com.iotabuild.zippyhand.feature.address.presentation.AddressViewModel
import com.iotabuild.zippyhand.feature.payments.presentation.CashFreePayment
import com.iotabuild.zippyhand.feature.payments.presentation.PaymentViewModel
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import com.iotabuild.zippyhand.ui.common.ZippyLoadingScreen
import com.iotabuild.zippyhand.utils.BillDetailsShimmer
import com.iotabuild.zippyhand.utils.UiState
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryScreen(
    productId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBookingCompleted: () -> Unit = {},
    onProceedToPayment: (orderId: String, amount: Int, name: String, email: String, phone: String) -> Unit,
    viewModel: BookingSummaryViewModel = koinViewModel(),
    paymentViewModel: PaymentViewModel = koinViewModel(),
    addressViewModel: AddressViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.fetchServiceDetail(productId)
        viewModel.fetchAddresses()
        viewModel.fetchOffers(productId.toLong())
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is BookingSummaryUiState.Loading -> {
                ZippyLoadingScreen(modifier = Modifier.align(Alignment.Center))
            }
            is BookingSummaryUiState.Error -> {
                ErrorState(message = state.message, onRetry = { viewModel.fetchServiceDetail(productId) })
            }
            is BookingSummaryUiState.Success -> {
                BookingSummaryContent(
                    service = state.service,
                    onBackClick = onBackClick,
                    onBookingCompleted = onBookingCompleted,
                    viewModel = viewModel,
                    paymentViewModel = paymentViewModel,
                    addressViewModel = addressViewModel
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSummaryContent(
    service: Service,
    onBackClick: () -> Unit,
    onBookingCompleted: () -> Unit,
    viewModel: BookingSummaryViewModel,
    paymentViewModel: PaymentViewModel,
    addressViewModel: AddressViewModel
) {

    val context = LocalContext.current
    val pricingState by viewModel.pricingState.collectAsState()
    val addressState by viewModel.addressState.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val appliedOffer by viewModel.appliedOffer.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSlotSelection by remember { mutableStateOf(false) }
    var showAddressSelection by remember { mutableStateOf(false) }
    var showAddAddress by remember { mutableStateOf(false) }

    if (showSlotSelection) {
        ModalBottomSheet(
            onDismissRequest = { showSlotSelection = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            SlotSelectionSheet(
                onCheckoutClick = { date, time ->
                    viewModel.setSchedule(date.fullDate, time)
                    showSlotSelection = false
                    viewModel.orderRequest.value?.let { request ->
                        val intent = Intent(context, CashFreePayment::class.java).apply {
                            putExtra("order_request", request)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }
    }

    if (showAddAddress) {
        ModalBottomSheet(
            onDismissRequest = { showAddAddress = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            AddAddressSheet(
                onAddClick = { newAddress ->
                    viewModel.addAddress(newAddress)
                    showAddAddress = false
                },
                onCloseClick = { showAddAddress = false }
            )
        }
    }

    if (showAddressSelection) {
        ModalBottomSheet(
            onDismissRequest = { showAddressSelection = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            when(val state = addressState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    AddressSelectionSheet(
                        addresses = state.data,
                        selectedAddress = selectedAddress,
                        onAddressSelected = { selected ->
                            viewModel.selectAddress(selected)
                            viewModel.setServiceAddress(selected.id)
                            showAddressSelection = false
                        },
                        onAddNewAddressClick = {
                            showAddressSelection = false
                            showAddAddress = true
                        },
                        onCloseClick = { showAddressSelection = false }
                    )
                }
                is UiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Booking Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            val totalPrice = (pricingState as? UiState.Success)?.data?.totalPrice ?: BigDecimal.ZERO
            BookingSummaryBottomBar(
                price = totalPrice.toDouble(),
                onSelectSlotClick = { showSlotSelection = true },
                enabled = selectedAddress != null && pricingState is UiState.Success
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummarySectionCard(
                    title = "Product Details",
                    titleIcon = Icons.Default.ShoppingCart
                ) {
                    CartItem(
                        service = service,
                        quantity = quantity,
                        onQuantityChange = { newQty -> viewModel.updateQuantity(newQty) }
                    )
                }

                SummarySectionCard(
                    title = "Offers & Coupons",
                    titleIcon = Icons.Default.ConfirmationNumber
                ) {
                    CouponList(
                        viewModel = viewModel,
                        appliedOffer = appliedOffer,
                        onApplyClick = { code -> viewModel.applyCoupon(code) },
                        onRemoveClick = { viewModel.applyCoupon(null) }
                    )
                }

                SummarySectionCard(
                    title = "Bill Details",
                    titleIcon = Icons.Default.Receipt
                ) {
                    when (val state = pricingState) {
                        is UiState.Loading -> {
                            BillDetailsShimmer()
                        }
                        is UiState.Success -> {
                            BillDetails(
                                itemTotal = state.data.totalPrice,
                                discount = state.data.discount,
                                serviceTax = state.data.taxAmount,
                                basePrice = state.data.basePrice
                            )
                        }
                        is UiState.Error -> {
                            Text("Failed to load price details: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                        else -> {}
                    }
                }

                AddressBar(
                    address = selectedAddress,
                    onChangeClick = { showAddressSelection = true }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
