package com.iotabuild.zippyhand.feature.booking.data

import com.iotabuild.zippyhand.feature.booking.domain.BookingRepository
import com.iotabuild.zippyhand.feature.booking.domain.MyOrdersApiResponse
import com.iotabuild.zippyhand.feature.booking.domain.SingleOrderApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class BookingRepositoryImpl(private val client: HttpClient) : BookingRepository {
    override suspend fun getMyBookings(): MyOrdersApiResponse {
        return client.get("order/my-orders").body()
    }

    override suspend fun getBookingDetail(orderId: Long): SingleOrderApiResponse {
        return client.get("order/$orderId").body()
    }

    override suspend fun cancelOrder(orderId: Long, reason: String?): SingleOrderApiResponse {
        return client.post("order/$orderId/cancel") {
            parameter("reason", reason)
        }.body()
    }
}
