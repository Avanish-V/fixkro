package com.iotabuild.zippyhand.core.network

import com.iotabuild.zippyhand.core.model.ApiError
import com.iotabuild.zippyhand.core.model.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

suspend inline fun <reified T> HttpResponse.parseApiResponse(): ApiResponse<T> {
    return try {
        if (status.isSuccess()) {
            try {
                body<ApiResponse<T>>()
            } catch (e: Exception) {
                val text = bodyAsText()
                val errorMsg = parseErrorMessageFromJson(text)
                ApiResponse(
                    success = false,
                    error = ApiError(message = errorMsg, code = status.value.toString())
                )
            }
        } else {
            val text = bodyAsText()
            val errorMsg = parseErrorMessageFromJson(text)
            ApiResponse(
                success = false,
                error = ApiError(message = errorMsg, code = status.value.toString())
            )
        }
    } catch (e: Exception) {
        val text = try { bodyAsText() } catch (_: Exception) { "" }
        val errorMsg = if (text.isNotBlank()) parseErrorMessageFromJson(text) else (e.message ?: "Network error occurred")
        ApiResponse(
            success = false,
            error = ApiError(message = errorMsg, code = status.value.toString())
        )
    }
}

fun parseErrorMessageFromJson(jsonText: String): String {
    return try {
        val jsonElement = Json.parseToJsonElement(jsonText)
        if (jsonElement is JsonObject) {
            val msg = jsonElement["message"]?.jsonPrimitive?.contentOrNull
            val err = jsonElement["error"]?.jsonPrimitive?.contentOrNull
            val rawMsg = msg ?: err ?: jsonText

            if (rawMsg.contains("authentication Failed", ignoreCase = true) || rawMsg.contains("401", ignoreCase = true)) {
                "Payment gateway authentication failed on backend server. Please check Cashfree API credentials."
            } else {
                rawMsg
            }
        } else {
            jsonText
        }
    } catch (_: Exception) {
        jsonText
    }
}
