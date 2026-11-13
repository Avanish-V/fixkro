package com.iotabuild.zippyhand.feature.home.data.local.converter

import androidx.room.TypeConverter
import com.iotabuild.zippyhand.feature.home.data.local.HomeEntity
import com.iotabuild.zippyhand.feature.home.data.remote.HomeResponse
import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//fun HomeEntity.toDomain(): HomeResponse = HomeResponse(
//    banner = banner,
//    categories = Converter().toCategoryList(category)
//)
//fun HomeResponse.toEntity(): HomeEntity = HomeEntity(
//    banner = banner,
//    category = Converter().fromCategoryList(categories)
//)

public class Converter{
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromCategoryList(value: List<Category>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toCategoryList(value: String): List<Category> {
        return json.decodeFromString(value)
    }

}