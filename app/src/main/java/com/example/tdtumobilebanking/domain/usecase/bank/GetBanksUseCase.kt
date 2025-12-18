package com.example.tdtumobilebanking.domain.usecase.bank

import com.example.tdtumobilebanking.domain.repository.BankRepository
import javax.inject.Inject

class GetBanksUseCase @Inject constructor(
    private val bankRepository: BankRepository
) {
    suspend operator fun invoke() = bankRepository.getBanks()
}

