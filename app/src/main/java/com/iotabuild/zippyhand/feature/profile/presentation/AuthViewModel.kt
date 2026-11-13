package com.iotabuild.zippyhand.feature.profile.presentation

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.iotabuild.zippyhand.R
import com.iotabuild.zippyhand.feature.profile.domain.AuthRepository
import com.iotabuild.zippyhand.feature.profile.domain.AuthResult
import com.iotabuild.zippyhand.feature.profile.domain.UserResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private var signInJob: Job? = null

    private val _authState = MutableStateFlow<AuthResult?>(AuthResult.IDLE)
    val authState = _authState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserResponse?>(null)
    val userProfile: StateFlow<UserResponse?> = _userProfile.asStateFlow()

    init {
        observeLocalUser()
        fetchUserProfile()
    }

    private fun observeLocalUser() {
        viewModelScope.launch {
            repository.getUserFromLocal().collect { user ->
                if (user != null) {
                    _userProfile.value = user
                }
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                try {
                    val idToken = firebaseUser.getIdToken(false).await().token
                    if (idToken != null) {
                        val response = repository.getUser(idToken)
                        response.fold(
                            onSuccess = {
                                _userProfile.value = it
                            },
                            onFailure = {
                                Log.e("AuthViewModel", "Error fetching user profile", it)
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error fetching user profile", e)
                }
            }
        }
    }

    fun launchGoogleSignIn(context: Context) {
        if (signInJob?.isActive == true) return

        signInJob = viewModelScope.launch {
            _authState.value = AuthResult.Loading

            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(context, request)
                val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken

                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val firebaseResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                val firebaseUser = firebaseResult.user

                if (firebaseUser != null) {
                    val idToken = firebaseUser.getIdToken(true).await().token
                    if (idToken != null) {
                        // 1. Read-only fetch: check if user exists in backend via GET /users/me
                        val getUserResult = repository.getUser(idToken)
                        if (getUserResult.isSuccess) {
                            _authState.value = AuthResult.Success
                        } else {
                            // 2. First-time sign in / New user: Upsert via POST /users/register
                            try {
                                repository.registerUser(
                                    idToken = idToken,
                                    name = firebaseUser.displayName,
                                    phoneNumber = firebaseUser.phoneNumber
                                )
                                _authState.value = AuthResult.Success
                                fetchUserProfile()
                            } catch (e: Exception) {
                                logout()
                                _authState.value = AuthResult.Error(e.message ?: "Registration failed")
                                _authState.value = AuthResult.IDLE
                            }
                        }
                    } else {
                        logout()
                        _authState.value = AuthResult.Error("Token missing")
                        _authState.value = AuthResult.IDLE
                    }
                } else {
                    logout()
                    _authState.value = AuthResult.Error("User not found")
                    _authState.value = AuthResult.IDLE
                }

            } catch (e: Exception) {
                logout()
                _authState.value = AuthResult.Error(e.message ?: "Google Sign-In Failed")
                _authState.value = AuthResult.IDLE
            }
        }
    }

    fun completeProfile(name: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                try {
                    val idToken = firebaseUser.getIdToken(true).await().token
                    if (idToken != null) {
                        // Write / Upsert via POST /users/register
                        repository.registerUser(idToken, name, phoneNumber)
                        _authState.value = AuthResult.Success
                        fetchUserProfile()
                    } else {
                        _authState.value = AuthResult.Error("Token missing")
                    }
                } catch (e: Exception) {
                    _authState.value = AuthResult.Error(e.message ?: "Update failed")
                }
            } else {
                _authState.value = AuthResult.Error("User not authenticated")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            repository.clearLocalUser()
            _userProfile.value = null
            _authState.value = AuthResult.IDLE
        }
    }

    fun resetState() {
        _authState.value = null
    }
}
