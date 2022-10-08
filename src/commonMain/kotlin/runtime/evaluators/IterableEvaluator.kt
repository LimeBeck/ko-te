package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object IterableEvaluator : Evaluator<AstLexeme.Iterator, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.Iterator, context: RuntimeContext): EvalResult<RuntimeObject> {
        val (_, iterable, item, body) = lexeme
        val iterableValue = context.get(iterable.name)
        if (iterableValue !is RuntimeObject.CollectionWrapper) {
            throw KoteRuntimeException("<7c6066b6> Iterable must be a collection at $lexeme")
        }
        val oldItem = context.getOrNull(item.name)
        iterableValue.collection.forEach { iter ->
            context.set(item.name, iter)
            body.forEach {
                CoreEvaluator.eval(it, context)
            }
        }
        context.set(item.name, oldItem)
        return EvalResult(RuntimeObject.Nothing)
    }
}

private fun RuntimeContext.getOrNull(key: String): RuntimeObject? =
    try {
        get(key)
    } catch (e: KoteRuntimeException) {
        null
    }
