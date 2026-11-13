package com.iotabuild.zippyhand.feature.servicedetails.usecase

import com.iotabuild.zippyhand.feature.reviews.domain.ReviewRepository
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceDetailsPage
import com.iotabuild.zippyhand.feature.servicedetails.domain.ServiceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetServiceDetailsUseCase(
    private val serviceRepository: ServiceRepository,
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        serviceId: String
    ): ServiceDetailsPage = coroutineScope {

        val serviceDeferred = async {
            serviceRepository.getService(serviceId.toInt())
        }

        val reviewsDeferred = async {
            reviewRepository.getReviewsByProduct(
                productId = serviceId.toLong(),
            )
        }

        val service = serviceDeferred.await()
        val reviewsPage = reviewsDeferred.await().getOrNull()

        ServiceDetailsPage(
            service = service.data,
            reviews = reviewsPage ?: emptyList(),
        )
    }
}