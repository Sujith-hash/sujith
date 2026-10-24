package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthManager {
    private const val TAG = "AuthManager"

    val currentUser: FirebaseUser?
        get() = try { Firebase.auth.currentUser } catch (_: Exception) { null }

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        var listener: FirebaseAuth.AuthStateListener? = null
        try {
            listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser)
            }
            Firebase.auth.addAuthStateListener(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Error adding AuthStateListener", e)
            trySend(null)
        }

        awaitClose {
            listener?.let {
                try {
                    Firebase.auth.removeAuthStateListener(it)
                } catch (_: Exception) {}
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val credentialManager = CredentialManager.create(context)
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "default_web_client_id resource not found", e)
            onError("Google Sign-In configuration missing")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val activity = context as? Activity
                if (activity == null) {
                    onError("Invalid activity context for Google Sign-In")
                    return@launch
                }
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        Log.d(TAG, "Google Sign-In successful: ${user.displayName} (${user.uid})")
                        onSuccess(user)
                    } else {
                        onError("Firebase user is null after sign in")
                    }
                } else {
                    onError("Unexpected credential type returned")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut(context: Context, scope: CoroutineScope, onComplete: () -> Unit) {
        val credentialManager = CredentialManager.create(context)
        Firebase.auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed clearing credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
