package com.iotabuild.zippyhand.feature.profile.domain

sealed interface StartUpState{
    data object Loading: StartUpState
    data object LoginRequired: StartUpState
    data object LocationGate: StartUpState
    data object Home: StartUpState
    data class Error(val message: String): StartUpState
}