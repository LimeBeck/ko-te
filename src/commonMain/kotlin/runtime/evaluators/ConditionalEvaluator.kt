package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object ConditionalEvaluator : Evaluator<AstLexeme.Conditional, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.Conditional, context: RuntimeContext): EvalResult<RuntimeObject> {
        val conditionResult = CoreEvaluator.eval(lexeme.condition, context)

        if (conditionResult.result !is RuntimeObject.BooleanWrapper) {
            throw KoteRuntimeException("<67a4a09f> Condition returned not a boolean value at $lexeme")
        }

        val branch = if (conditionResult.result.value)
            lexeme.then.map { CoreEvaluator.eval(it, context) }
        else
            lexeme.another?.map { CoreEvaluator.eval(it, context) } ?: listOf()

        return EvalResult(branch.lastOrNull()?.result ?: RuntimeObject.Nothing)
    }
}