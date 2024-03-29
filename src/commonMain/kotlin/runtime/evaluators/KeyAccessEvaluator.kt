package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object KeyAccessEvaluator : Evaluator<AstLexeme.KeyAccess, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.KeyAccess, context: RuntimeContext): EvalResult<RuntimeObject> {
        val value = CoreEvaluator.eval(lexeme.obj, context)
        val key = lexeme.key
        when (value.result) {
            is RuntimeObject.ObjectWrapper -> return EvalResult(value.result.objectMap[key] ?: RuntimeObject.Null)

            else -> throw KoteRuntimeException("<30632f8f> ${value.result} is not an object")
        }

    }
}