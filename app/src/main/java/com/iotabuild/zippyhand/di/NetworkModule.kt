package com.iotabuild.zippyhand.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient(Android) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            defaultRequest {
                url(com.iotabuild.zippyhand.utils.AppConstants.BASE_URL)
            }
        }.also { client ->
            // Use this approach to avoid DSL name collisions with okhttp3/AsyncTask
            client.sendPipeline.intercept(HttpSendPipeline.State) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    try {
                        val token = currentUser.getIdToken(false).await().token
                        if (token != null && context.headers["Authorization"] == null) {
                            Log.d("networkModule", "Adding Token: $token")
                            context.header("Authorization", "Bearer $token")
                        }
                    } catch (e: Exception) {
                        Log.e("networkModule", "Token fetch failed", e)
                    }
                }
                proceed()
            }
        }
    }
}
