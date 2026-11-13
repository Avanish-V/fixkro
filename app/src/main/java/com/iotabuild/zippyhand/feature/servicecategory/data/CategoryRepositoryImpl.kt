package com.iotabuild.zippyhand.feature.servicecategory.data

import com.iotabuild.zippyhand.feature.servicecategory.domain.CategoriesResponse
import com.iotabuild.zippyhand.feature.servicecategory.domain.CategoryRepository
import com.iotabuild.zippyhand.feature.servicecategory.domain.CategoryResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class CategoryRepositoryImpl(private val client: HttpClient) : CategoryRepository {
    private var cachedCategories: CategoriesResponse? = null

    override suspend fun getCategory(id: Int): CategoryResponse {
        return client.get("category/$id").body()
    }

    override suspend fun getCategories(): CategoriesResponse {
        cachedCategories?.let { return it }
        val response: CategoriesResponse = client.get("categories").body()
        if (response.success) {
            cachedCategories = response
        }
        return response
    }
}
