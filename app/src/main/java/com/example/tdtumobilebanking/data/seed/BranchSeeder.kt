package com.example.tdtumobilebanking.data.seed

import com.example.tdtumobilebanking.domain.model.Branch
import javax.inject.Inject

class BranchSeeder @Inject constructor() {
    fun defaultBranches(): List<Branch> = listOf(
        Branch(
            branchId = "branch_tdt_1",
            name = "TDTU Main Branch",
            latitude = 10.732,
            longitude = 106.699,
            address = "19 Nguyen Huu Tho, Tan Phong, District 7, HCMC"
        ),
        Branch(
            branchId = "branch_tdt_2",
            name = "TDTU North Branch",
            latitude = 10.821,
            longitude = 106.629,
            address = "Thu Duc City, HCMC"
        )
    )
}

