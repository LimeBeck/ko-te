package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object FunctionCallEvaluator : Evaluator<AstLexeme.FunctionCall, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.FunctionCall, context: RuntimeContext): EvalResult<RuntimeObject> {
        val possibleFunction = when (lexeme.identifier) {
            is AstLexeme.Variable -> context.get(lexeme.identifier.name)
            else -> CoreEvaluator.eval(lexeme.identifier, context).result
        }
        if (possibleFunction !is RuntimeObject.CallableWrapper)
            throw RuntimeException("<95ee753e> $lexeme is not a callable")
        val args = lexeme.args.map {
            CoreEvaluator.eval(it.value, context).result
        }
        return EvalResult(possibleFunction.block(args, context))
    }
}