package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext

interface Evaluator<T: AstLexeme, Result> {
    fun eval(lexeme: T, context: RuntimeContext): EvalResult<Result>
}

data class EvalResult<R>(
    val result: R
)