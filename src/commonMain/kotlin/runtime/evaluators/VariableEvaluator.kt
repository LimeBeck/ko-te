package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object VariableEvaluator : Evaluator<AstLexeme.Variable, Any> {
    override fun eval(lexeme: AstLexeme.Variable, context: RuntimeContext): EvalResult<Any> {
        val value = context.get(lexeme.name)
        return EvalResult(
            (value as? RuntimeObject.Value)?.value ?: throw RuntimeException("<381098f4> $value is not a value")
        )
    }
}