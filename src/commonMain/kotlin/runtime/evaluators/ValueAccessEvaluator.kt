package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object ValueAccessEvaluator : Evaluator<AstLexeme.Primitive, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.Primitive, context: RuntimeContext): EvalResult<RuntimeObject> {
        val result = when (lexeme) {
            is AstLexeme.String -> RuntimeObject.StringWrapper(lexeme.value)
            is AstLexeme.Number -> RuntimeObject.NumberWrapper(lexeme.value)
            is AstLexeme.Boolean -> RuntimeObject.BooleanWrapper(lexeme.value)
        }
        return EvalResult(result)
    }
}