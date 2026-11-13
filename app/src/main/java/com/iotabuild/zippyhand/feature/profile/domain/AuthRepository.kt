package com.iotabuild.zippyhand.feature.profile.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun checkUserExists(idToken: String): Boolean
    suspend fun getUser(idToken: String): Result<UserResponse>
    suspend fun registerUser(idToken: String, name: String? = null, phoneNumber: String? = null): UserResponse
    fun getUserFromLocal(): Flow<UserResponse?>
    suspend fun clearLocalUser()
}

sealed class AuthResult {
    object IDLE : AuthResult()
    object Loading : AuthResult()
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

