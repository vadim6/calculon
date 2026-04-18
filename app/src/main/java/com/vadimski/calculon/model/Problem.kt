package com.vadimski.calculon.model

sealed class Problem {
    data class Multiplication(
        val multiplicand: Int,
        val multiplier: Int,
        val level: Int
    ) : Problem()

    data class Division(
        val dividend: Int,
        val divisor: Int,
        val quotient: Int,
        val remainder: Int
    ) : Problem()
}
