package com.example.data.repository

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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface AuthAttemptResult {
    data class Success(val user: FirebaseUser) : AuthAttemptResult
    data object Cancelled : AuthAttemptResult
    data class Failure(val message: String) : AuthAttemptResult
}

class BetZoneAuthRepository(context: Context) {

    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    fun currentUser(): FirebaseUser? = try {
        Firebase.auth.currentUser
    } catch (_: Exception) {
        null
    }

    private fun resolveWebClientId(): String? = try {
        appContext.getString(R.string.default_web_client_id).takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }

    suspend fun attemptAutoSignIn(activityContext: Context): FirebaseUser? {
        currentUser()?.let { return it }
        val clientId = resolveWebClientId() ?: return null
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val cred = credentialManager.getCredential(activityContext, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await().user
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d("Auth", "Silent auto sign-in unavailable: ${e.message}")
            null
        }
    }

    suspend fun signInWithGoogleInteractive(activity: Activity): AuthAttemptResult {
        val clientId = resolveWebClientId()
            ?: return AuthAttemptResult.Failure("Google Sign-In configuration missing: default_web_client_id not found.")
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val cred = credentialManager.getCredential(activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                val result = Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                val user = result.user
                if (user != null) {
                    AuthAttemptResult.Success(user)
                } else {
                    AuthAttemptResult.Failure("Authentication succeeded without a user session.")
                }
            } else {
                AuthAttemptResult.Failure("Unexpected credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            AuthAttemptResult.Cancelled
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            AuthAttemptResult.Failure(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
        }
    }

    suspend fun signOut() {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                try {
                    Firebase.auth.signOut()
                } catch (e: Exception) {
                    Log.w("Auth", "Firebase signOut warning: ${e.message}")
                }
            }
        }
    }
}
