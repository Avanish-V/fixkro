package com.iotabuild.zippyhand.feature.payments.presentation

import com.iotabuild.zippyhand.core.model.ApiError
import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.feature.booking.domain.OrderRequest
import com.iotabuild.zippyhand.feature.booking.domain.OrderResponse
import com.iotabuild.zippyhand.feature.payments.domain.*
import com.iotabuild.zippyhand.feature.reviews.domain.ReviewsApiResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.ProductResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.ProductsResponse
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import com.iotabuild.zippyhand.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakePaymentRepository : PaymentRepository {
    var createSessionCalledCount = 0
    var verifyPaymentCalledCount = 0
    var retryPaymentCalledCount = 0

    var verifyPaymentResponse: ApiResponse<CashfreeOrderResponse> = ApiResponse(
        success = true,
        data = CashfreeOrderResponse(
            order_id = "ORD1234",
            cf_order_id = "cf_order_1234",
            order_amount = 500.0,
            order_status = "PENDING"
        )
    )

    var retryPaymentResponse: ApiResponse<RetryPaymentResponse> = ApiResponse(
        success = true,
        data = RetryPaymentResponse(
            orderId = "ORD1234",
            cfOrderId = "cf_order_1234",
            paymentSessionId = "session_retry_123"
        )
    )

    override suspend fun createPaymentSession(request: PaymentSessionRequest): ApiResponse<PaymentSessionData> {
        createSessionCalledCount++
        return ApiResponse(
            success = true,
            data = PaymentSessionData(
                orderId = "ORD1234",
                cfOrderId = "cf_order_1234",
                paymentSessionId = "session_123",
                orderAmount = 500.0,
                orderCurrency = "INR",
                customerDetails = CustomerDetails("1", "User", "user@test.com", "9999999999")
            )
        )
    }

    override suspend fun verifyPayment(request: PaymentVerificationRequest): ApiResponse<CashfreeOrderResponse> {
        verifyPaymentCalledCount++
        return verifyPaymentResponse
    }

    override suspend fun retryPayment(orderId: String): ApiResponse<RetryPaymentResponse> {
        retryPaymentCalledCount++
        return retryPaymentResponse
    }
}

class FakeServiceRepositoryForPayment : ServiceRepository {
    var createOrderCalledCount = 0

    override suspend fun getServices(categoryId: Int?): ProductsResponse =
        ProductsResponse(success = true, data = emptyList())

    override suspend fun getService(productId: Int): ProductResponse {
        throw NotImplementedError()
    }

    override suspend fun getReviews(productId: Int): ReviewsApiResponse =
        ReviewsApiResponse(success = true, data = emptyList())

    override suspend fun createOrder(orderRequest: OrderRequest): ApiResponse<OrderResponse> {
        createOrderCalledCount++
        return ApiResponse(
            success = true,
            data = OrderResponse(
                orderId = "ORD1234",
                cfOrderId = "cf_order_1234",
                paymentSessionId = "session_123"
            )
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var paymentRepository: FakePaymentRepository
    private lateinit var serviceRepository: FakeServiceRepositoryForPayment
    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        paymentRepository = FakePaymentRepository()
        serviceRepository = FakeServiceRepositoryForPayment()
        viewModel = PaymentViewModel(paymentRepository, serviceRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `verifyPayment does not call createOrder and sets PENDING state on pending status`() = runTest {
        paymentRepository.verifyPaymentResponse = ApiResponse(
            success = true,
            data = CashfreeOrderResponse(
                order_id = "ORD1234",
                cf_order_id = "cf_1234",
                order_amount = 500.0,
                order_status = "PENDING"
            )
        )

        viewModel.verifyPayment("ORD1234")
        advanceUntilIdle()

        // 1. Verification called exactly once
        assertEquals(1, paymentRepository.verifyPaymentCalledCount)
        // 2. createOrder was NEVER called!
        assertEquals(0, serviceRepository.createOrderCalledCount)
        // 3. State is Success with PENDING order_status
        assertTrue(viewModel.paymentVerificationState is UiState.Success)
        val data = (viewModel.paymentVerificationState as UiState.Success).data
        assertEquals("PENDING", data?.order_status)
    }

    @Test
    fun `verifyPayment multiple times never creates another order`() = runTest {
        viewModel.verifyPayment("ORD1234")
        advanceUntilIdle()
        viewModel.verifyPayment("ORD1234")
        advanceUntilIdle()
        viewModel.verifyPayment("ORD1234")
        advanceUntilIdle()

        assertEquals(3, paymentRepository.verifyPaymentCalledCount)
        assertEquals(0, serviceRepository.createOrderCalledCount)
    }

    @Test
    fun `retryPayment reuses order and updates session without calling createOrder`() = runTest {
        paymentRepository.retryPaymentResponse = ApiResponse(
            success = true,
            data = RetryPaymentResponse(
                orderId = "ORD1234",
                cfOrderId = "cf_1234",
                paymentSessionId = "session_reused_999"
            )
        )

        viewModel.retryPayment("ORD1234")
        advanceUntilIdle()

        // 1. Retry endpoint called
        assertEquals(1, paymentRepository.retryPaymentCalledCount)
        // 2. createOrder was NOT called
        assertEquals(0, serviceRepository.createOrderCalledCount)
        // 3. Payment session state is updated to Success to relaunch SDK
        assertTrue(viewModel.paymentSessionState is UiState.Success)
        val sessionData = (viewModel.paymentSessionState as UiState.Success).data
        assertEquals("ORD1234", sessionData.orderId)
        assertEquals("session_reused_999", sessionData.paymentSessionId)
        // 4. Verification state reset to Idle
        assertTrue(viewModel.paymentVerificationState is UiState.Idle)
    }

    @Test
    fun `retryPayment failure sets verification error without creating new order`() = runTest {
        paymentRepository.retryPaymentResponse = ApiResponse(
            success = false,
            data = null,
            error = ApiError(
                code = "PAYMENT_PENDING",
                message = "A payment attempt is currently pending. Please check status."
            )
        )

        viewModel.retryPayment("ORD1234")
        advanceUntilIdle()

        assertEquals(1, paymentRepository.retryPaymentCalledCount)
        assertEquals(0, serviceRepository.createOrderCalledCount)
        assertTrue(viewModel.paymentVerificationState is UiState.Error)
        val errorMsg = (viewModel.paymentVerificationState as UiState.Error).message
        assertEquals("A payment attempt is currently pending. Please check status.", errorMsg)
    }

    @Test
    fun `verifyPayment with empty orderId sets error immediately without network call`() = runTest {
        viewModel.verifyPayment("")
        advanceUntilIdle()

        assertEquals(0, paymentRepository.verifyPaymentCalledCount)
        assertTrue(viewModel.paymentVerificationState is UiState.Error)
    }
}
