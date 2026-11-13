package com.iotabuild.zippyhand.feature.booking.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iotabuild.zippyhand.feature.address.domain.AddressResponse
import com.iotabuild.zippyhand.feature.address.domain.AddressRepository
import com.iotabuild.zippyhand.feature.address.domain.AddressRequest
import com.iotabuild.zippyhand.feature.booking.domain.OrderRequest
import com.iotabuild.zippyhand.feature.booking.domain.PriceDetailResponse
import com.iotabuild.zippyhand.feature.booking.domain.PricingRepository
import com.iotabuild.zippyhand.feature.booking.domain.PricingRequest
import com.iotabuild.zippyhand.feature.offers.domain.Offer
import com.iotabuild.zippyhand.feature.offers.domain.OfferRepository
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
sealed class BookingSummaryUiState {
    object Loading : BookingSummaryUiState()
    data class Success(val service: Service) : BookingSummaryUiState()
    data class Error(val message: String) : BookingSummaryUiState()
    object OrderCreating : BookingSummaryUiState()
    data class OrderCreated(
        val orderId: String,
        val cfOrderId: String,
        val paymentSessionId: String
    ) : BookingSummaryUiState()
    data class OrderError(val message: String) : BookingSummaryUiState()
}

class BookingSummaryViewModel(
    private val repository: ServiceRepository,
    private val pricingRepository: PricingRepository,
    private val addressRepository: AddressRepository,
    private val offerRepository: OfferRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _pricingState = MutableStateFlow<UiState<PriceDetailResponse>>(UiState.Idle)
    val pricingState: StateFlow<UiState<PriceDetailResponse>> = _pricingState.asStateFlow()

    private val _uiState = MutableStateFlow<BookingSummaryUiState>(BookingSummaryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _addressState = MutableStateFlow<UiState<List<AddressResponse>>>(UiState.Loading)
    val addressState: StateFlow<UiState<List<AddressResponse>>> = _addressState.asStateFlow()

    val selectedAddress = MutableStateFlow<AddressResponse?>(null)

    private val _productId = MutableStateFlow<Long?>(null)
    private val _quantity = MutableStateFlow(1)
    private val _appliedOffer = MutableStateFlow<String?>(null)

    val quantity: StateFlow<Int> = _quantity.asStateFlow()
    val appliedOffer: StateFlow<String?> = _appliedOffer.asStateFlow()

    private val _availableOffers = MutableStateFlow<UiState<List<Offer>>>(UiState.Loading)
    val availableOffers: StateFlow<UiState<List<Offer>>> = _availableOffers.asStateFlow()

    private val _orderRequest = MutableStateFlow<OrderRequest?>(null)
    val orderRequest = _orderRequest.asStateFlow()

    init {
        observePricingInputs()
    }

    fun fetchAddresses() {
        viewModelScope.launch {
            _addressState.value = UiState.Loading
            try {
                val data = addressRepository.getAddresses()
                if (data.success) {
                    val addresses = data.data ?: emptyList()
                    _addressState.value = UiState.Success(addresses)
                    if (selectedAddress.value == null && addresses.isNotEmpty()) {
                        selectAddress(addresses.first())
                    }
                } else {
                    _addressState.value = UiState.Error(data.error?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _addressState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun addAddress(address: AddressRequest) {
        viewModelScope.launch {
            val result = addressRepository.addAddress(address)
            if (result.success) {
                val newAddress = result.data
                val currentState = _addressState.value
                if (newAddress != null && currentState is UiState.Success) {
                    val updatedList = listOf(newAddress) + currentState.data
                    _addressState.value = UiState.Success(updatedList)
                    selectAddress(newAddress)
                } else {
                    fetchAddresses()
                }
            }
        }
    }

    fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            val currentState = _addressState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.filter { it.id?.toLong() != addressId }
                _addressState.value = UiState.Success(updatedList)
                
                if (selectedAddress.value?.id?.toLong() == addressId) {
                    selectedAddress.value = updatedList.firstOrNull()
                    selectedAddress.value?.let { setServiceAddress(it.id) }
                }
            }

            val result = addressRepository.deleteAddress(addressId)
            if (!result.success) {
                fetchAddresses()
            }
        }
    }

    fun fetchOffers(productId: Long? = null) {
        viewModelScope.launch {
            _availableOffers.value = UiState.Loading
            try {
                Log.d("BookingSummaryViewModel", "Fetching offers for product ID: $productId")
               val response =  productId?.let {
                     offerRepository.getOffersByProduct(it)
                }
                Log.d("BookingSummaryViewModel", "Fetching offers for product ID: $response")
                
                if (response?.success ?: false) {
                    _availableOffers.value = UiState.Success(response.data)
                } else {
                    _availableOffers.value = UiState.Error(response?.error?.message ?: "Failed to fetch offers")
                }
            } catch (e: Exception) {
                _availableOffers.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    private fun observePricingInputs() {
        combine(_productId, _quantity, _appliedOffer) { id, qty, coupon ->
            if (id != null) Triple(id, qty, coupon) else null
        }
            .distinctUntilChanged()
            .debounce(500)
            .onEach { inputs ->
                if (inputs == null) return@onEach
                _pricingState.value = UiState.Loading
            }
            .flatMapLatest { inputs ->
                kotlinx.coroutines.flow.flow {
                    if (inputs == null) return@flow
                    try {
                        val response = pricingRepository.getPricing(
                            PricingRequest(inputs.first, inputs.second, inputs.third)
                        )
                        emit(response)
                    } catch (e: Exception) {
                        Log.e("BookingSummary", "Pricing fetch failed", e)
                        emit(null)
                    }
                }
            }
            .onEach { response ->
                when {
                    response == null -> {
                        _pricingState.value = UiState.Error("Connectivity error")
                    }
                    response.success -> {
                        _pricingState.value = UiState.Success(response.data)
                    }
                    else -> {
                        _pricingState.value = UiState.Error(response.error?.message ?: "Coupon or pricing invalid")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateQuantity(qty: Int = 1) {
        val cappedQty = qty.coerceIn(1, 5)
        _quantity.value = cappedQty
        _orderRequest.value = ensureOrderRequest().copy(
            quantity = cappedQty,
        )
    }

    fun applyCoupon(code: String?) {
        _appliedOffer.value = code
        _orderRequest.value = ensureOrderRequest().copy(
            offerCode = code,
        )
    }

    fun fetchServiceDetail(productId: Int) {
        val current = _uiState.value
        if (current is BookingSummaryUiState.Success && current.service.productId == productId) {
            _productId.value = current.service.productId.toLong()
            setProduct(current.service.productId.toLong())
            return
        }

        viewModelScope.launch {
            _uiState.value = BookingSummaryUiState.Loading
            try {
                val response = repository.getService(productId)
                if (response.success) {
                    _productId.value = response.data.productId.toLong()
                    _uiState.value = BookingSummaryUiState.Success(response.data)
                    setProduct(response.data.productId.toLong())
                } else {
                    _uiState.value = BookingSummaryUiState.Error(response.error?.message ?: "Failed to fetch service details")
                }
            } catch (e: Exception) {
                _uiState.value = BookingSummaryUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun createOrder(orderRequest: OrderRequest) {
        viewModelScope.launch {
            _uiState.value = BookingSummaryUiState.OrderCreating
            try {
                val response = repository.createOrder(orderRequest)
                if (response.success) {
                    val data = response.data
                    _uiState.value = BookingSummaryUiState.OrderCreated(
                        orderId = data?.orderId ?: "UNKNOWN",
                        cfOrderId = data?.cfOrderId ?: "",
                        paymentSessionId = data?.paymentSessionId ?: ""
                    )
                } else {
                    _uiState.value = BookingSummaryUiState.OrderError(response.error?.message ?: "Failed to crate order")
                }
            } catch (e: Exception) {
                _uiState.value = BookingSummaryUiState.OrderError(e.message ?: "Network error during order creation")
            }
        }
    }

    fun selectAddress(address: AddressResponse) {
        selectedAddress.value = address
        address.id?.let { setServiceAddress(it) }
    }

    private fun ensureOrderRequest(): OrderRequest {
        return _orderRequest.value ?: OrderRequest(
            addressId = 0,
            productId = 0,
            quantity = 1,
            offerCode = null,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
            serviceSchedule = ""
        )
    }

    fun setProduct(productId: Long) {
        _orderRequest.value = ensureOrderRequest().copy(productId = productId)
    }

    fun setServiceAddress(addressId: Long?) {
        _orderRequest.value = addressId?.let { ensureOrderRequest().copy(addressId = it) }
    }

    fun setSchedule(date: String, time: String) {
        _orderRequest.value = ensureOrderRequest().copy(serviceSchedule = "$date $time")
    }
}
