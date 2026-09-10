package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.Operation
import dev.limebeck.templateEngine.parser.ast.UnaryOperation
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object UnaryOperationEvaluator : Evaluator<AstLexeme.PrefixOperation, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.PrefixOperation, context: RuntimeContext): EvalResult<RuntimeObject> {
        val operand = CoreEvaluator.eval(lexeme.operand, context).result
        val result = when (lexeme.operation) {
            UnaryOperation.NOT -> {
                val boolean = operand as? RuntimeObject.BooleanWrapper ?: invalidOperand(lexeme, "Boolean")
                RuntimeObject.BooleanWrapper(!boolean.value)
            }
            UnaryOperation.PLUS, UnaryOperation.MINUS -> {
                val number = operand as? RuntimeObject.NumberWrapper ?: invalidOperand(lexeme, "Number")
                val operation = if (lexeme.operation == UnaryOperation.PLUS) Operation.PLUS else Operation.MINUS
                RuntimeObject.NumberWrapper(calculateNumber(0L, number.number, operation))
            }
        }
        return EvalResult(result)
    }

    private fun invalidOperand(lexeme: AstLexeme.PrefixOperation, expected: String): Nothing =
        throw KoteRuntimeException("Unary ${lexeme.operation.symbol} requires $expected at ${lexeme.streamPosition}")
}
