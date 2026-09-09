package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.Operation
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

// Keep the public helpers in this file for JVM binary compatibility.
operator fun Number.plus(other: Number): Number = calculateNumber(this, other, Operation.PLUS)
operator fun Number.minus(other: Number): Number = calculateNumber(this, other, Operation.MINUS)
operator fun Number.times(other: Number): Number = calculateNumber(this, other, Operation.MULTIPLY)
operator fun Number.div(other: Number): Number = calculateNumber(this, other, Operation.DIVIDE)
operator fun Number.rem(other: Number): Number = calculateNumber(this, other, Operation.PERCENT)

object OperationEvaluator : Evaluator<AstLexeme.InfixOperation, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.InfixOperation, context: RuntimeContext): EvalResult<RuntimeObject> {
        val left = CoreEvaluator.eval(lexeme.left, context).result
        val right = CoreEvaluator.eval(lexeme.right, context).result
        val result = when {
            lexeme.operation == Operation.EQUALS -> RuntimeObject.BooleanWrapper(when {
                left is RuntimeObject.NumberWrapper && right is RuntimeObject.NumberWrapper ->
                    numbersEqual(left.number, right.number)
                left is RuntimeObject.StringWrapper && right is RuntimeObject.StringWrapper -> left.string == right.string
                left is RuntimeObject.BooleanWrapper && right is RuntimeObject.BooleanWrapper -> left.value == right.value
                else -> false
            })
            left is RuntimeObject.NumberWrapper && right is RuntimeObject.NumberWrapper ->
                RuntimeObject.NumberWrapper(calculateNumber(left.number, right.number, lexeme.operation))
            lexeme.operation == Operation.PLUS && left is RuntimeObject.StringWrapper && right is RuntimeObject.StringWrapper ->
                RuntimeObject.StringWrapper(left.string + right.string)
            else -> throw KoteRuntimeException("Cannot apply ${lexeme.operation.stringValue} to $left and $right")
        }
        return EvalResult(result)
    }
}
