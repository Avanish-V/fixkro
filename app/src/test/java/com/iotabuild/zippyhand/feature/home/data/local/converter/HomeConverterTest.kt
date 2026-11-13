package com.iotabuild.zippyhand.feature.home.data.local.converter

import com.iotabuild.zippyhand.feature.servicecategory.domain.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeConverterTest {

    private val converter = Converter()

    @Test
    fun fromCategoryList_convertsListToJsonString() {
        val categories = listOf(
            Category(categoryId = 1, title = "AC", image = "ac.png"),
            Category(categoryId = 2, title = "Plumbing", image = "plumbing.png")
        )

        val jsonResult = converter.fromCategoryList(categories)

        assertTrue(jsonResult.contains("AC"))
        assertTrue(jsonResult.contains("Plumbing"))
    }

    @Test
    fun toCategoryList_convertsJsonStringToList() {
        val categories = listOf(
            Category(categoryId = 1, title = "AC", image = "ac.png"),
            Category(categoryId = 2, title = "Plumbing", image = "plumbing.png")
        )
        val jsonString = converter.fromCategoryList(categories)

        val resultList = converter.toCategoryList(jsonString)

        assertEquals(2, resultList.size)
        assertEquals(1, resultList[0].categoryId)
        assertEquals("AC", resultList[0].title)
        assertEquals(2, resultList[1].categoryId)
        assertEquals("Plumbing", resultList[1].title)
    }
}
