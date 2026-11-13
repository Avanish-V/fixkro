package com.iotabuild.zippyhand.feature.servicecategory.domain

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val categoryId: Int,
    val title: String,
    val image: String
)

@Serializable
data class CategoryResponse(
    val success: Boolean,
    val data: Category,
    val error: CategoryError? = null
)

@Serializable
data class CategoriesResponse(
    val success: Boolean,
    val data: List<Category>,
    val error: CategoryError? = null
)

@Serializable
data class CategoryError(
    val code: String,
    val message: String
)
