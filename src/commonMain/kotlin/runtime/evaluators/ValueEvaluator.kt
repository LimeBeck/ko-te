package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object ValueEvaluator: Evaluator<AstLexeme.Value, String> {
    override fun eval(lexeme: AstLexeme.Value, context: RuntimeContext): EvalResult<String> {
        val result = when(lexeme) {
            is AstLexeme.String -> lexeme.value
            is AstLexeme.Number -> lexeme.value.toString()

            else -> "TODO"
        }
        return EvalResult(result)
    }
}