package com.example.tdtumobilebanking.data.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.mapper.toDomain
import com.example.tdtumobilebanking.data.mapper.toDto
import com.example.tdtumobilebanking.data.remote.dto.BranchDto
import com.example.tdtumobilebanking.domain.model.Branch
import com.example.tdtumobilebanking.domain.repository.BranchRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BranchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BranchRepository {

    override fun getBranches(): Flow<ResultState<List<Branch>>> = flow {
        emit(ResultState.Loading)
        try {
            val snapshot = firestore.collection(COLLECTION).get().await()
            val branches = snapshot.documents.mapNotNull { it.toObject(BranchDto::class.java)?.toDomain() }
            emit(ResultState.Success(branches))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }

    override suspend fun seedBranches(branches: List<Branch>): ResultState<Unit> = try {
        val batch = firestore.batch()
        branches.forEach { branch ->
            val docRef = firestore.collection(COLLECTION).document(branch.branchId)
            batch.set(docRef, branch.toDto())
        }
        batch.commit().await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    companion object {
        private const val COLLECTION = "branches"
    }
}

