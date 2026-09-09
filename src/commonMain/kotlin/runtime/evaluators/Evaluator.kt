package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

interface Evaluator<in T : AstLexeme, out Result : RuntimeObject> {
    suspend fun eval(lexeme: T, context: RuntimeContext): EvalResult<Result>
}

data class EvalResult<out R>(
    val result: R
)
