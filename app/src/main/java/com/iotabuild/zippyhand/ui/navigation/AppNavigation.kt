package com.iotabuild.zippyhand.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data object Bookings : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object ProfileDetails : Route

    @Serializable
    data object Offers : Route

    // Feature Screens
    @Serializable
    data class ServiceDetail(val productId: Int) : Route

    @Serializable
    data class ServiceList(val categoryName: String) : Route

    @Serializable
    data class BookingSummary(val productId: Int) : Route

    @Serializable
    data class BookingDetail(val orderId: Long) : Route

    @Serializable
    data class WriteReview(
        val orderId: Long,
        val productId: Long
    ) : Route

    @Serializable
    data object Terms : Route

    @Serializable
    data object Privacy : Route

    @Serializable
    data object AboutUs : Route

    @Serializable
    data object UserAddresses : Route

    @Serializable
    data class Payment(
        val orderId: String,
        val orderAmount: Int
    ) : Route

    // Auth & Onboarding
    @Serializable
    data object LocationGate : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object CreateProfile : Route
}
