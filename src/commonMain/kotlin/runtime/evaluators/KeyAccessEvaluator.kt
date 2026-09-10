package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object KeyAccessEvaluator : Evaluator<AstLexeme.KeyAccess, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.KeyAccess, context: RuntimeContext): EvalResult<RuntimeObject> {
        val value = CoreEvaluator.eval(lexeme.obj, context).result
        if (value !is RuntimeObject.ObjectWrapper) {
            throw KoteRuntimeException("Cannot read field '${lexeme.key}' from a non-object at ${lexeme.streamPosition}")
        }
        return EvalResult(value.objectMap[lexeme.key]
            ?: throw KoteRuntimeException("Missing field '${lexeme.key}' at ${lexeme.streamPosition}"))
    }
}
