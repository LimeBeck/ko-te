package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.inputStream.InputStream
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

interface Evaluator<in T : AstLexeme, out Result : RuntimeObject> {
    suspend fun eval(lexeme: T, context: RuntimeContext): EvalResult<Result>

    /** Consumes the next node and leaves the remaining nodes for later evaluation. */
    suspend fun eval(stream: InputStream<out T>, context: RuntimeContext): EvalResult<Result> {
        if (!stream.hasNext()) throw KoteRuntimeException("Expected an AST node", position = stream.currentPosition)
        return eval(stream.next(), context)
    }
}

data class EvalResult<out R>(
    val result: R
)
