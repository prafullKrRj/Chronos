package com.prafullkumar.chronos.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.prafullkumar.chronos.R
import com.prafullkumar.chronos.core.Resource
import com.prafullkumar.chronos.data.dtos.UserDto
import com.prafullkumar.chronos.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class LoginRepositoryImpl(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : LoginRepository {
    private val credentialManager = CredentialManager.create(context)

    override fun loginUser(context: Context): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            Log.d("LoginRepository", "Attempting to get Google ID token")
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()
            Log.d("LoginRepository", "Google ID option created: $googleIdOption")
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Get the credential from Google
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            Log.d("LoginRepository", "Credential obtained: $result")
            // Extract the Google ID token
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            // Create Firebase credential
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

            // Sign in with Firebase
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

            if (authResult.user != null) {
                // Create or update user in Firestore
                val userDto = UserDto(
                    uid = authResult.user!!.uid,
                    displayName = authResult.user!!.displayName ?: "",
                    photoUrl = authResult.user!!.photoUrl?.toString() ?: "",
                    lastLogin = Timestamp.now(),
                )
                val unitResult = createOrUpdateUser(userDto)
                if (unitResult.isSuccess) {
                    emit(Resource.Success(true))
                } else {
                    emit(
                        Resource.Error(
                            unitResult.exceptionOrNull()?.message ?: "Failed to update user"
                        )
                    )
                }
            } else {
                emit(Resource.Error("Authentication failed"))
            }
        } catch (e: GetCredentialException) {
            emit(Resource.Error("Credential error: ${e.message}"))
            Log.e("LoginRepository", "Credential error: ${e.message}", e)
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred during login"))
            Log.e("LoginRepository", "Login error: ${e.message}", e)
        }
    }

    override suspend fun createOrUpdateUser(user: UserDto): Result<Unit> = try {
        val userDoc = firestore.collection("users").document(user.uid)

        val existingUser = userDoc.get().await()

        if (existingUser.exists()) {
            userDoc.update("lastLoginAt", Timestamp.now()).await()
        } else {
            userDoc.set(user).await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun signOutUser(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            firebaseAuth.signOut()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred during sign out"))
        }
    }

    // Helper function to check if user is currently signed in
    fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Helper function to get current user
    fun getCurrentUser() = firebaseAuth.currentUser
}