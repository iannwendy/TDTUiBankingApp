package com.example.tdtumobilebanking.core.di

import com.example.tdtumobilebanking.data.repository.AccountRepositoryImpl
import com.example.tdtumobilebanking.data.repository.AuthRepositoryImpl
import com.example.tdtumobilebanking.data.repository.BillRepositoryImpl
import com.example.tdtumobilebanking.data.repository.BranchRepositoryImpl
import com.example.tdtumobilebanking.data.repository.TransactionRepositoryImpl
import com.example.tdtumobilebanking.data.repository.UserRepositoryImpl
import com.example.tdtumobilebanking.data.repository.BankRepositoryImpl
import com.example.tdtumobilebanking.domain.repository.AccountRepository
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.repository.BillRepository
import com.example.tdtumobilebanking.domain.repository.BranchRepository
import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.example.tdtumobilebanking.domain.repository.BankRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBranchRepository(impl: BranchRepositoryImpl): BranchRepository

    @Binds
    @Singleton
    abstract fun bindBankRepository(impl: BankRepositoryImpl): BankRepository

    @Binds
    @Singleton
    abstract fun bindBillRepository(impl: BillRepositoryImpl): BillRepository
}

