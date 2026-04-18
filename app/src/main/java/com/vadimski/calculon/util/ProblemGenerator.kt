package com.vadimski.calculon.util

import com.vadimski.calculon.model.DivisionStep
import com.vadimski.calculon.model.MultiplicationStep
import com.vadimski.calculon.model.Problem
import kotlin.random.Random

fun generateMultiplication(level: Int): Problem.Multiplication = when (level) {
    1 -> Problem.Multiplication(
        multiplicand = Random.nextInt(1, 10),
        multiplier = Random.nextInt(1, 10),
        level = 1
    )
    2 -> Problem.Multiplication(
        multiplicand = Random.nextInt(100, 1000),
        multiplier = Random.nextInt(2, 10),
        level = 2
    )
    else -> {
        var multiplier: Int
        do { multiplier = Random.nextInt(11, 100) } while (multiplier % 10 == 0)
        Problem.Multiplication(
            multiplicand = Random.nextInt(100, 1000),
            multiplier = multiplier,
            level = 3
        )
    }
}

fun generateDivision(): Problem.Division {
    val divisor = Random.nextInt(2, 10)
    val dividend = Random.nextInt(10, 1000)
    return Problem.Division(
        dividend = dividend,
        divisor = divisor,
        quotient = dividend / divisor,
        remainder = dividend % divisor
    )
}

// Each row = one multiplier digit. Each step = one multiplicand digit (right to left).
// carryIn is pre-computed so the ViewModel only needs to check rawProduct.
fun computeMultiplicationSteps(problem: Problem.Multiplication): List<MultiplicationStep> {
    val multiplicandDigits = problem.multiplicand.toString().map { it.digitToInt() }.reversed()
    val multiplierDigits = problem.multiplier.toString().map { it.digitToInt() }.reversed()
    val steps = mutableListOf<MultiplicationStep>()
    for ((rowIndex, mDigit) in multiplierDigits.withIndex()) {
        var carry = 0
        for ((pos, cDigit) in multiplicandDigits.withIndex()) {
            val raw = cDigit * mDigit
            steps.add(
                MultiplicationStep(
                    multiplicandDigit = cDigit,
                    multiplierDigit = mDigit,
                    rawProduct = raw,
                    carryIn = carry,
                    partialRowIndex = rowIndex,
                    digitPosition = pos
                )
            )
            carry = (raw + carry) / 10
        }
    }
    return steps
}

// Each step: bring digits into `current` until current >= divisor, then divide.
// Remainder of each step becomes the start of the next.
fun computeDivisionSteps(dividend: Int, divisor: Int): List<DivisionStep> {
    val digits = dividend.toString().map { it.digitToInt() }
    val steps = mutableListOf<DivisionStep>()
    var current = 0
    for (digit in digits) {
        current = current * 10 + digit
        if (steps.isEmpty() && current < divisor) continue
        val q = current / divisor
        steps.add(DivisionStep(current, q, q * divisor, current % divisor))
        current = current % divisor
    }
    return steps.ifEmpty {
        val q = dividend / divisor
        listOf(DivisionStep(dividend, q, q * divisor, dividend % divisor))
    }
}
