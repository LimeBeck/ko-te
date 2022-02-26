package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object VariableAccessEvaluator : Evaluator<AstLexeme.Variable, RuntimeObject> {
    override fun eval(lexeme: AstLexeme.Variable, context: RuntimeContext): EvalResult<RuntimeObject> {
        val value = context.get(lexeme.name)
        return EvalResult(value)
    }
}