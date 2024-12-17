package com.itsyourturn.moscow.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.itsyourturn.moscow.data.repository.AuthRepository
import com.itsyourturn.moscow.data.repository.UserRepository
import com.itsyourturn.moscow.data.model.UserProfile
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val user = authRepository.signInWithEmail(email, password)
                handleAuthSuccess(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val user = authRepository.signUpWithEmail(email, password)
                user?.let {
                    createUserProfile(it.uid, name, email)
                }
                handleAuthSuccess(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val user = authRepository.signInWithGoogle(idToken)
                handleAuthSuccess(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val user = authRepository.signInWithPhone(credential)
                handleAuthSuccess(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Phone authentication failed")
            }
        }
    }

    fun signInAsGuest() {
        val user = authRepository.signInAsGuest()
        handleAuthSuccess(user)
    }

    private fun handleAuthSuccess(user: FirebaseUser?) {
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Error("Authentication failed")
        }
    }

    private suspend fun createUserProfile(userId: String, name: String, email: String) {
        val profile = UserProfile(
            userId = userId,
            name = name,
            email = email
        )
        userRepository.createUserProfile(userId, profile)
    }

    sealed class AuthState {
        object Loading : AuthState()
        data class Authenticated(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
