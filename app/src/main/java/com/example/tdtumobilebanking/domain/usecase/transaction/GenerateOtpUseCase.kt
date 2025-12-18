package com.example.tdtumobilebanking.domain.usecase.transaction

import com.example.tdtumobilebanking.core.util.OtpGenerator
import javax.inject.Inject

class GenerateOtpUseCase @Inject constructor() {
    operator fun invoke(): String = OtpGenerator.generateSixDigitCode()
}

