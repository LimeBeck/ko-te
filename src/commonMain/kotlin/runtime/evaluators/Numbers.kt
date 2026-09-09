package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.Operation
import dev.limebeck.templateEngine.runtime.KoteRuntimeException

private const val MAX_EXACT_DOUBLE_INTEGER = 9_007_199_254_740_991L

private val Number.isIntegral: Boolean
    get() {
        if (this is Long) return true
        if (this !is Byte && this !is Short && this !is Int && this !is Float && this !is Double) return false
        // Byte/Short type checks alone also match fractional numbers in Kotlin/JS.
        val value = toDouble()
        return value.isFinite() && value >= Long.MIN_VALUE.toDouble() &&
            value < Long.MAX_VALUE.toDouble() && value % 1.0 == 0.0
    }

private fun invalidNumber(reason: String): Nothing = throw KoteRuntimeException(reason)

private fun Number.toCheckedDouble(): Double {
    if (!isIntegral && this !is Float && this !is Double) invalidNumber("Unsupported numeric type")
    if (isIntegral && toLong() !in -MAX_EXACT_DOUBLE_INTEGER..MAX_EXACT_DOUBLE_INTEGER) {
        invalidNumber("Cannot mix an integer outside the exact Double range with a decimal")
    }
    return toDouble().also { if (!it.isFinite()) invalidNumber("Non-finite number") }
}

internal fun calculateNumber(left: Number, right: Number, operation: Operation): Number =
    if (left.isIntegral && right.isIntegral) {
        calculateIntegral(left.toLong(), right.toLong(), operation)
    } else {
        calculateDecimal(left.toCheckedDouble(), right.toCheckedDouble(), operation)
    }

private fun calculateDecimal(left: Double, right: Double, operation: Operation): Number {
    if ((operation == Operation.DIVIDE || operation == Operation.PERCENT) && right == 0.0) {
        invalidNumber("Division by zero")
    }
    val result = when (operation) {
        Operation.PLUS -> left + right
        Operation.MINUS -> left - right
        Operation.MULTIPLY -> left * right
        Operation.DIVIDE -> left / right
        Operation.PERCENT -> left % right
        Operation.EQUALS -> invalidNumber("Equality is not an arithmetic operation")
    }
    if (!result.isFinite()) invalidNumber("Non-finite arithmetic result")
    return if (result.isIntegral) result.toLong() else result
}

private fun calculateIntegral(left: Long, right: Long, operation: Operation): Long {
    if ((operation == Operation.DIVIDE || operation == Operation.PERCENT) && right == 0L) {
        invalidNumber("Division by zero")
    }
    return when (operation) {
        Operation.PLUS -> (left + right).also { result ->
            if (((left xor result) and (right xor result)) < 0) invalidNumber("Long overflow")
        }
        Operation.MINUS -> (left - right).also { result ->
            if (((left xor right) and (left xor result)) < 0) invalidNumber("Long overflow")
        }
        Operation.MULTIPLY -> (left * right).also { result ->
            if ((left == Long.MIN_VALUE && right == -1L) ||
                (right == Long.MIN_VALUE && left == -1L) ||
                (left != 0L && result / left != right)) invalidNumber("Long overflow")
        }
        Operation.DIVIDE -> {
            if (left == Long.MIN_VALUE && right == -1L) invalidNumber("Long overflow")
            left / right
        }
        Operation.PERCENT -> left % right
        Operation.EQUALS -> invalidNumber("Equality is not an arithmetic operation")
    }
}

internal fun numbersEqual(left: Number, right: Number): Boolean {
    if (left.isIntegral && right.isIntegral) return left.toLong() == right.toLong()
    if (!left.isIntegral && !right.isIntegral) return left.toCheckedDouble() == right.toCheckedDouble()
    val integer = if (left.isIntegral) left.toLong() else right.toLong()
    val decimal = if (left.isIntegral) right.toCheckedDouble() else left.toCheckedDouble()
    // Do not round large integers to Double before comparing them.
    return decimal >= Long.MIN_VALUE.toDouble() && decimal < Long.MAX_VALUE.toDouble() &&
        decimal % 1.0 == 0.0 && decimal.toLong() == integer
}
