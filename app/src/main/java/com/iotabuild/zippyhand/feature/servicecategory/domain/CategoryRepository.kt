package com.iotabuild.zippyhand.feature.servicecategory.domain

interface CategoryRepository {
    suspend fun getCategory(id: Int): CategoryResponse
    suspend fun getCategories(): CategoriesResponse
}
