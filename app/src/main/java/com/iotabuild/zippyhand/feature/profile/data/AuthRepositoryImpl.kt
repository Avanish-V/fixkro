package com.iotabuild.zippyhand.feature.profile.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iotabuild.zippyhand.core.model.ApiResponse
import com.iotabuild.zippyhand.feature.home.data.local.HomeDao
import com.iotabuild.zippyhand.location.data.local.LocationDao
import com.iotabuild.zippyhand.feature.profile.data.local.UserDao
import com.iotabuild.zippyhand.feature.profile.data.local.toDomain
import com.iotabuild.zippyhand.feature.profile.data.local.toEntity
import com.iotabuild.zippyhand.feature.profile.domain.AuthRepository
import com.iotabuild.zippyhand.feature.profile.domain.AuthResult
import com.iotabuild.zippyhand.feature.profile.domain.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val client: HttpClient,
    private val userDao: UserDao,
    private val locationDao: LocationDao,
    private val homeDao: HomeDao
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            AuthResult.Loading
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google Sign-In Failed")
        }
    }

    override suspend fun checkUserExists(idToken: String): Boolean {
        return getUser(idToken).isSuccess
    }

    override suspend fun getUser(idToken: String): Result<UserResponse> {
        return try {
            val response = client.get("users/me") {
                header("Authorization", "Bearer $idToken")
            }
            if (response.status.value == 200) {
                val apiResponse: ApiResponse<UserResponse> = response.body()
                val user = apiResponse.data ?: return Result.failure(
                    Exception(
                        apiResponse.error?.message ?: "User not found"
                    )
                )
                userDao.insertUser(user.toEntity())
                return Result.success(user)
            } else {
                return Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            val cachedUser = userDao.getUserOnce()
            if (cachedUser != null) {
                Result.success(cachedUser.toDomain())
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun registerUser(idToken: String, name: String?, phoneNumber: String?): UserResponse {
        Log.d("AuthRepositoryImpl", "registerUser: $idToken with name: $name")
        val apiResponse: ApiResponse<UserResponse> = client.post("users/register") {
            header("Authorization", "Bearer $idToken")
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "name" to name,
                "phoneNumber" to phoneNumber
            ))
        }.body()
        val user = apiResponse.data ?: throw Exception(apiResponse.error?.message ?: "Registration failed")
        userDao.insertUser(user.toEntity())
        return user
    }

    override fun getUserFromLocal(): Flow<UserResponse?> {
        return userDao.getUser().map { it?.toDomain() }
    }

    override suspend fun clearLocalUser() {
        userDao.clearUser()
        locationDao.clearLocation()
        homeDao.clearHome()
    }
}
