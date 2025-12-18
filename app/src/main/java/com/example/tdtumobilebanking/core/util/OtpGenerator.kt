package com.example.tdtumobilebanking.core.util

import kotlin.random.Random

object OtpGenerator {
    fun generateSixDigitCode(): String = Random.nextInt(100000, 999999).toString()
}

