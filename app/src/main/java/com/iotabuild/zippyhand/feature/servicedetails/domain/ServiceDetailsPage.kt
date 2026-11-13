package com.iotabuild.zippyhand.feature.servicedetails.domain

import com.iotabuild.zippyhand.feature.reviews.domain.Review

data class ServiceDetailsPage(
    val service: Service,
    val reviews: List<Review> = emptyList(),
)