package com.example.tdtumobilebanking.presentation.branches

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.seed.BranchSeeder
import com.example.tdtumobilebanking.domain.model.Branch
import com.example.tdtumobilebanking.domain.repository.BranchRepository
import com.example.tdtumobilebanking.domain.usecase.branch.GetBranchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BranchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val branches: List<Branch> = emptyList()
)

@HiltViewModel
class BranchViewModel @Inject constructor(
    private val getBranchesUseCase: GetBranchesUseCase,
    private val branchRepository: BranchRepository,
    private val seeder: BranchSeeder
) : BaseViewModel<BranchUiState>(BranchUiState()) {

    init {
        loadBranches()
    }

    fun loadBranches() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getBranchesUseCase().collect { result ->
                when (result) {
                    is ResultState.Error -> {
                        setState { copy(isLoading = false, error = result.throwable.message) }
                        seedDefaults()
                    }
                    ResultState.Loading -> setState { copy(isLoading = true) }
                    is ResultState.Success -> setState { copy(isLoading = false, branches = result.data) }
                }
            }
        }
    }

    private fun seedDefaults() {
        viewModelScope.launch {
            branchRepository.seedBranches(seeder.defaultBranches())
        }
    }
}

