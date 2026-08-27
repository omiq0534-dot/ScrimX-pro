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

import com.example.FirebaseHelper

class AuthViewModel : ViewModel() {
    private fun getAuth(): FirebaseAuth? = FirebaseHelper.getAuth()
    
    // The Web Client ID from your google-services.json
    private val WEB_CLIENT_ID = "404122407805-o3qparuqg40rair571gmsp669j11qs17.apps.googleusercontent.com"
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Check if user is already logged in
        try {
            val auth = getAuth()
            if (auth?.currentUser != null) {
                _authState.value = AuthState.Success(auth.currentUser!!.uid)
            }
        } catch (e: Exception) {
            // Ignored safely
        }
    }

    fun loginWithGoogle(context: Context) {
        val fbAuth = getAuth()
        if (fbAuth == null) {
            _authState.value = AuthState.Error("Connecting to Firebase... please try again.")
            return
        }
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
                    
                    val authResult = fbAuth.signInWithCredential(firebaseCredential).await()
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

    private fun formatAuthError(msg: String?): String {
        val m = msg ?: return "Authentication Failed"
        return when {
            m.contains("incorrect", ignoreCase = true) || 
            m.contains("invalid-credential", ignoreCase = true) || 
            m.contains("user-not-found", ignoreCase = true) ||
            m.contains("wrong-password", ignoreCase = true) -> 
                "Wrong password or Account doesn't exist! If you are new, tap 'Register' tab above."
            m.contains("email-already-in-use", ignoreCase = true) || m.contains("already in use", ignoreCase = true) ->
                "This Email is already registered! Please tap 'Login' tab above."
            m.contains("weak-password", ignoreCase = true) ->
                "Password must be at least 6 characters."
            m.contains("badly formatted", ignoreCase = true) || m.contains("invalid-email", ignoreCase = true) ->
                "Please enter a valid email address."
            m.contains("network", ignoreCase = true) ->
                "Network error! Please check your internet connection."
            else -> m
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        val fbAuth = getAuth()
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            onResult(false, "Please enter your email address first")
            return
        }
        if (fbAuth == null) {
            onResult(false, "Connecting to Firebase... please try again")
            return
        }
        viewModelScope.launch {
            try {
                fbAuth.sendPasswordResetEmail(cleanEmail).await()
                onResult(true, "Password reset email sent to $cleanEmail! Check inbox/spam.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to send reset link")
            }
        }
    }

    fun login(email: String, pass: String) {
        val fbAuth = getAuth()
        if (fbAuth == null) {
            _authState.value = AuthState.Error("Connecting to Firebase... please try again.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val cleanEmail = email.trim()
                if (cleanEmail.isEmpty() || pass.isEmpty()) {
                    _authState.value = AuthState.Error("Email/Password cannot be empty")
                    return@launch
                }
                val result = fbAuth.signInWithEmailAndPassword(cleanEmail, pass).await()
                if (result.user != null) {
                    _authState.value = AuthState.Success(result.user!!.uid)
                } else {
                    _authState.value = AuthState.Error("Unknown Error")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(formatAuthError(e.message))
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        val fbAuth = getAuth()
        if (fbAuth == null) {
            _authState.value = AuthState.Error("Connecting to Firebase... please try again.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val cleanEmail = email.trim()
                if (cleanEmail.isEmpty() || pass.isEmpty()) {
                    _authState.value = AuthState.Error("Email/Password cannot be empty")
                    return@launch
                }
                val result = fbAuth.createUserWithEmailAndPassword(cleanEmail, pass).await()
                if (result.user != null) {
                    val uid = result.user!!.uid
                    val cleanName = name.trim().ifBlank { cleanEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                    try {
                        val db = FirebaseHelper.getFirestore()
                        db?.collection("users")?.document(uid)?.set(
                            mapOf(
                                "uid" to uid,
                                "email" to cleanEmail,
                                "name" to cleanName,
                                "realMoney" to 100,
                                "appMoney" to 0
                            )
                        )
                    } catch (ex: Exception) {
                        // ignore
                    }
                    _authState.value = AuthState.Success(uid)
                } else {
                    _authState.value = AuthState.Error("Unknown Error")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(formatAuthError(e.message))
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
