package com.iotabuild.zippyhand.feature.home.data.remote

import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import com.iotabuild.zippyhand.feature.servicedetails.domain.Service
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val isServiceLoading: Boolean = false,
    val isError: Boolean = false,
    val banner: String,
    val categories: List<Category>,
    val services: List<Service>
)
