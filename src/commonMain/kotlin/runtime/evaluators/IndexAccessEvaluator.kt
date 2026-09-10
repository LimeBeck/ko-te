package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object IndexAccessEvaluator : Evaluator<AstLexeme.IndexAccess, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.IndexAccess, context: RuntimeContext): EvalResult<RuntimeObject> {
        val value = CoreEvaluator.eval(lexeme.array, context).result
        if (value !is RuntimeObject.CollectionWrapper) {
            throw KoteRuntimeException("Cannot read index ${lexeme.index} from a non-collection at ${lexeme.streamPosition}")
        }
        return EvalResult(value.collection.getOrNull(lexeme.index)
            ?: throw KoteRuntimeException("Index ${lexeme.index} is out of bounds for size ${value.collection.size} at ${lexeme.streamPosition}"))
    }
}
