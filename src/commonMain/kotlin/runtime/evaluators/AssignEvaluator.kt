package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.wrap

object AssignEvaluator : Evaluator<AstLexeme.Assign, RuntimeObject> {
    override fun eval(lexeme: AstLexeme.Assign, context: RuntimeContext): EvalResult<RuntimeObject> {
        val value = CoreEvaluator.eval(lexeme.right, context).result.wrap()
        when (lexeme.left) {
            is AstLexeme.Variable -> context.set(lexeme.left.name, value)
            is AstLexeme.IndexAccess -> TODO("<4026481c>")
            is AstLexeme.KeyAccess -> TODO("<cc32040f>")
        }
        return EvalResult(RuntimeObject.Nothing)
    }
}