package com.example.tdtumobilebanking.data.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.mapper.toDomain
import com.example.tdtumobilebanking.data.mapper.toDto
import com.example.tdtumobilebanking.data.remote.dto.UserDto
import com.example.tdtumobilebanking.domain.model.KycStatus
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): ResultState<User> = try {
        val credential = auth.signInWithEmailAndPassword(email, password).await()
        val uid = credential.user?.uid ?: return ResultState.Error(IllegalStateException("User not found"))
        val userDoc = firestore.collection("users").document(uid)
        
        // Try to get existing user document
        val snapshot = try {
            userDoc.get().await()
        } catch (e: Exception) {
            // If read fails (e.g., permission denied or document doesn't exist), create new document
            null
        }
        
        val user = if (snapshot != null && snapshot.exists()) {
            // User document exists, parse it
            snapshot.toObject(UserDto::class.java)?.toDomain()
        } else {
            // User document doesn't exist, create bootstrap user
            null
        } ?: run {
            // Create bootstrap user document
            val bootstrap = User(
                uid = uid,
                fullName = credential.user?.displayName.orEmpty().ifBlank { email.substringBefore("@") },
                email = email,
                role = "CUSTOMER",
                phoneNumber = credential.user?.phoneNumber.orEmpty(),
                kycStatus = KycStatus.NONE,
                avatarUrl = ""
            )
            try {
                userDoc.set(bootstrap.toDto()).await()
            } catch (e: Exception) {
                // If create fails, still return the bootstrap user (it will be created on next login)
                return@run bootstrap
            }
            bootstrap
        }
        ResultState.Success(user)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override suspend fun logout(): ResultState<Unit> = try {
        auth.signOut()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override fun currentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(
                user?.let { current ->
                    User(uid = current.uid, email = current.email.orEmpty(), fullName = current.displayName.orEmpty(), role = "", phoneNumber = current.phoneNumber.orEmpty())
                }
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}

