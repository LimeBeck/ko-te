package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object IndexAccessEvaluator : Evaluator<AstLexeme.IndexAccess, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.IndexAccess, context: RuntimeContext): EvalResult<RuntimeObject> {
        val value = CoreEvaluator.eval(lexeme.array, context)
        val index = lexeme.index
        when (value.result) {
            is RuntimeObject.CollectionWrapper -> return EvalResult(
                value.result.collection.getOrNull(index) ?: RuntimeObject.Null
            )

            else -> throw KoteRuntimeException("<30632f8f> ${value.result} is not an object")
        }

    }
}