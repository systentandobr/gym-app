package com.tadevolta.gym.utils.auth

import com.tadevolta.gym.data.models.User

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
