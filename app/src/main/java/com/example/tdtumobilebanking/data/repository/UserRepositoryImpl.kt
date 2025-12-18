package com.example.tdtumobilebanking.data.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.mapper.toDomain
import com.example.tdtumobilebanking.data.mapper.toDto
import com.example.tdtumobilebanking.data.remote.dto.UserDto
import com.example.tdtumobilebanking.domain.model.KycStatus
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUserProfile(uid: String): Flow<ResultState<User>> = flow {
        emit(ResultState.Loading)
        try {
            android.util.Log.d("UserRepositoryImpl", "========== [START] getUserProfile ==========")
            android.util.Log.d("UserRepositoryImpl", "[STEP 1] Getting user profile for UID: '$uid'")
            
            val snapshot = firestore.collection(COLLECTION).document(uid).get().await()
            android.util.Log.d("UserRepositoryImpl", "[STEP 2] Snapshot retrieved")
            android.util.Log.d("UserRepositoryImpl", "[STEP 2.1] Snapshot exists: ${snapshot.exists()}")
            android.util.Log.d("UserRepositoryImpl", "[STEP 2.2] Snapshot data: ${snapshot.data}")
            android.util.Log.d("UserRepositoryImpl", "[STEP 2.3] Snapshot data keys: ${snapshot.data?.keys}")
            
            if (!snapshot.exists()) {
                android.util.Log.w("UserRepositoryImpl", "[STEP 3] User document does not exist in Firestore")
                android.util.Log.w("UserRepositoryImpl", "[STEP 3.1] UID: '$uid'")
                android.util.Log.w("UserRepositoryImpl", "[STEP 3.2] This might be a new user or data was deleted")
                throw IllegalStateException("User profile missing: Document does not exist for UID '$uid'")
            }
            
            val userDto = snapshot.toObject(UserDto::class.java)
            android.util.Log.d("UserRepositoryImpl", "[STEP 4] UserDto parsed")
            android.util.Log.d("UserRepositoryImpl", "[STEP 4.1] UserDto: $userDto")
            android.util.Log.d("UserRepositoryImpl", "[STEP 4.2] UserDto.uid: '${userDto?.uid}'")
            android.util.Log.d("UserRepositoryImpl", "[STEP 4.3] UserDto.fullName: '${userDto?.fullName}'")
            android.util.Log.d("UserRepositoryImpl", "[STEP 4.4] UserDto.email: '${userDto?.email}'")
            android.util.Log.d("UserRepositoryImpl", "[STEP 4.5] UserDto.role: '${userDto?.role}'")
            android.util.Log.d("UserRepositoryImpl", "[STEP 4.6] UserDto.phoneNumber: '${userDto?.phoneNumber}'")
            
            if (userDto == null) {
                android.util.Log.e("UserRepositoryImpl", "[STEP 5] UserDto is null - cannot parse document data")
                android.util.Log.e("UserRepositoryImpl", "[STEP 5.1] Document exists but data format is invalid")
                android.util.Log.e("UserRepositoryImpl", "[STEP 5.2] Raw data: ${snapshot.data}")
                throw IllegalStateException("User profile missing: Cannot parse user data for UID '$uid'. Document exists but data format is invalid.")
            }
            
            val user = userDto.toDomain()
            android.util.Log.d("UserRepositoryImpl", "[STEP 6] User domain object created")
            android.util.Log.d("UserRepositoryImpl", "[STEP 6.1] User: $user")
            android.util.Log.d("UserRepositoryImpl", "[STEP 6.2] User.uid: '${user.uid}'")
            android.util.Log.d("UserRepositoryImpl", "[STEP 6.3] User.fullName: '${user.fullName}'")
            android.util.Log.d("UserRepositoryImpl", "[STEP 6.4] User.email: '${user.email}'")
            android.util.Log.d("UserRepositoryImpl", "[STEP 6.5] User.role: '${user.role}'")
            
            if (user.uid.isBlank()) {
                android.util.Log.e("UserRepositoryImpl", "[STEP 7] User UID is blank after conversion")
                throw IllegalStateException("User profile missing: User UID is blank for document '$uid'")
            }
            
            android.util.Log.d("UserRepositoryImpl", "[STEP 8] User profile loaded successfully")
            android.util.Log.d("UserRepositoryImpl", "========== [END] getUserProfile ==========")
            emit(ResultState.Success(user))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            android.util.Log.e("UserRepositoryImpl", "========== [ERROR] getUserProfile ==========")
            android.util.Log.e("UserRepositoryImpl", "[ERROR] Exception type: ${e::class.simpleName}")
            android.util.Log.e("UserRepositoryImpl", "[ERROR] Exception message: ${e.message}")
            android.util.Log.e("UserRepositoryImpl", "[ERROR] Exception stack trace", e)
            e.printStackTrace()
            emit(ResultState.Error(e))
        }
    }

    override suspend fun createOrUpdateUser(user: User): ResultState<Unit> = try {
        firestore.collection(COLLECTION).document(user.uid).set(user.toDto()).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override suspend fun updateKycStatus(uid: String, status: KycStatus, avatarUrl: String?): ResultState<Unit> = try {
        val updates = mutableMapOf<String, Any>("kycStatus" to status.name)
        if (!avatarUrl.isNullOrBlank()) updates["avatarUrl"] = avatarUrl
        firestore.collection(COLLECTION).document(uid).update(updates).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override fun getAllCustomers(): Flow<ResultState<List<User>>> = flow {
        emit(ResultState.Loading)
        try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("role", "CUSTOMER")
                .get()
                .await()
            val users = snapshot.documents.mapNotNull { it.toObject(UserDto::class.java)?.toDomain() }
            emit(ResultState.Success(users))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }

    companion object {
        private const val COLLECTION = "users"
    }
}

