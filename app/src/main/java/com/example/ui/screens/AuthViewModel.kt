package com.example.ui.screens

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    // The Web Client ID from your google-services.json
    private val WEB_CLIENT_ID = "404122407805-k4h30v9led3gb20et1ih9nes7ohb79a2.apps.googleusercontent.com"
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Check if user is already logged in
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser!!.uid)
        }
    }

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    val idToken = credential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    if (authResult.user != null) {
                        _authState.value = AuthState.Success(authResult.user!!.uid)
                    } else {
                        _authState.value = AuthState.Error("Google Auth Failed")
                    }
                } else {
                    _authState.value = AuthState.Error("Unexpected Credential Type")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Login Failed")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val cleanEmail = email.trim()
                if (cleanEmail.isEmpty() || pass.isEmpty()) {
                    _authState.value = AuthState.Error("Email/Password cannot be empty")
                    return@launch
                }
                val result = auth.signInWithEmailAndPassword(cleanEmail, pass).await()
                if (result.user != null) {
                    _authState.value = AuthState.Success(result.user!!.uid)
                } else {
                    _authState.value = AuthState.Error("Unknown Error")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login Failed")
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val cleanEmail = email.trim()
                if (cleanEmail.isEmpty() || pass.isEmpty()) {
                    _authState.value = AuthState.Error("Email/Password cannot be empty")
                    return@launch
                }
                val result = auth.createUserWithEmailAndPassword(cleanEmail, pass).await()
                if (result.user != null) {
                    // Could save name to firestore here
                    _authState.value = AuthState.Success(result.user!!.uid)
                } else {
                    _authState.value = AuthState.Error("Unknown Error")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration Failed")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
