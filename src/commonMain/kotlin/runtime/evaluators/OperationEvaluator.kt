package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.Operation
import dev.limebeck.templateEngine.parser.ast.valueParsers.IndexAccessParser.isInteger
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

operator fun Number.plus(other: Number): Number =
    if (this.isInteger() && other.isInteger()) {
        this.toInt() + other.toInt()
    } else {
        this.toFloat() + other.toFloat()
    }

operator fun Number.minus(other: Number): Number =
    if (this.isInteger() && other.isInteger()) {
        this.toInt() - other.toInt()
    } else {
        this.toFloat() - other.toFloat()
    }

operator fun Number.times(other: Number): Number =
    if (this.isInteger() && other.isInteger()) {
        this.toInt() * other.toInt()
    } else {
        this.toFloat() * other.toFloat()
    }

operator fun Number.rem(other: Number): Number =
    if (this.isInteger() && other.isInteger()) {
        this.toInt() % other.toInt()
    } else {
        this.toFloat() % other.toFloat()
    }

operator fun Number.div(other: Number): Number =
    if (this.isInteger() && other.isInteger()) {
        this.toInt() / other.toInt()
    } else {
        this.toFloat() / other.toFloat()
    }

object OperationEvaluator : Evaluator<AstLexeme.InfixOperation, RuntimeObject> {
    override fun eval(lexeme: AstLexeme.InfixOperation, context: RuntimeContext): EvalResult<RuntimeObject> {
        val left = CoreEvaluator.eval(lexeme.left, context)
        val right = CoreEvaluator.eval(lexeme.right, context)
        val result = when (lexeme.operation) {
            Operation.PLUS -> when {
                left.result is RuntimeObject.NumberWrapper &&
                        right.result is RuntimeObject.NumberWrapper -> RuntimeObject.NumberWrapper(left.result.number + right.result.number)
                left.result is RuntimeObject.StringWrapper &&
                        right.result is RuntimeObject.StringWrapper -> RuntimeObject.StringWrapper(left.result.string + right.result.string)
                else -> throw RuntimeException("<e3702fa1> $lexeme can`t be evaluated")
            }
            Operation.MULTIPLY -> when {
                left.result is RuntimeObject.NumberWrapper &&
                        right.result is RuntimeObject.NumberWrapper -> RuntimeObject.NumberWrapper(left.result.number * right.result.number)
                else -> throw RuntimeException("<015cca01> $lexeme can`t be evaluated")
            }
            Operation.MINUS -> when {
                left.result is RuntimeObject.NumberWrapper &&
                        right.result is RuntimeObject.NumberWrapper -> RuntimeObject.NumberWrapper(left.result.number - right.result.number)
                else -> throw RuntimeException("<a956ce5b> $lexeme can`t be evaluated")
            }
            Operation.DIVIDE ->  when {
                left.result is RuntimeObject.NumberWrapper &&
                        right.result is RuntimeObject.NumberWrapper -> RuntimeObject.NumberWrapper(left.result.number / right.result.number)
                else -> throw RuntimeException("<6da4d6d4> $lexeme can`t be evaluated")
            }
            Operation.PERCENT -> when {
                left.result is RuntimeObject.NumberWrapper &&
                        right.result is RuntimeObject.NumberWrapper -> RuntimeObject.NumberWrapper(left.result.number % right.result.number)
                else -> throw RuntimeException("<6da4d6d4> $lexeme can`t be evaluated")
            }
            Operation.EQUALS -> TODO()
        }
        return EvalResult(result)
//        val possibleFunction = when (lexeme.operation) {
//            is AstLexeme.Variable -> context.get(lexeme.identifier.name)
//            else -> CoreEvaluator.eval(lexeme.identifier, context).result
//        }
//        if (possibleFunction !is RuntimeObject.CallableWrapper)
//            throw RuntimeException("<95ee753e> $lexeme is not a callable")
//        val args = lexeme.args.map {
//            CoreEvaluator.eval(it.value, context).result
//        }
//        return EvalResult(possibleFunction.block(args))
    }
}