package dev.limebeck.templateEngine.runtime

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.AstRoot
import dev.limebeck.templateEngine.runtime.evaluators.CoreEvaluator
import dev.limebeck.templateEngine.runtime.evaluators.TemplateEvaluator
import dev.limebeck.templateEngine.runtime.evaluators.ValueEvaluator
import dev.limebeck.templateEngine.runtime.evaluators.VariableEvaluator

interface RuntimeEngine {
    fun evaluateProgram(root: AstRoot, context: RuntimeContext): String
}

object SimpleRuntimeEngine : RuntimeEngine {
    override fun evaluateProgram(root: AstRoot, context: RuntimeContext): String {
        return root.body.fold("") { acc, lexeme ->
            acc + CoreEvaluator.eval(lexeme, context).result
        }
    }
}