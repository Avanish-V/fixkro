package com.iotabuild.zippyhand.feature.booking.domain

interface BookingRepository {
    suspend fun getMyBookings(): MyOrdersApiResponse
    suspend fun getBookingDetail(orderId: Long): SingleOrderApiResponse
    suspend fun cancelOrder(orderId: Long, reason: String?): SingleOrderApiResponse
}
