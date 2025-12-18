package com.example.tdtumobilebanking.domain.usecase.branch

import com.example.tdtumobilebanking.domain.repository.BranchRepository
import javax.inject.Inject

class GetBranchesUseCase @Inject constructor(
    private val repository: BranchRepository
) {
    operator fun invoke() = repository.getBranches()
}

