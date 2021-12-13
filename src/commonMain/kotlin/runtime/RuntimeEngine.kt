package dev.limebeck.templateEngine.runtime

import dev.limebeck.templateEngine.parser.ast.AstRoot
import dev.limebeck.templateEngine.runtime.evaluators.CoreEvaluator

interface RuntimeEngine {
    fun evaluateProgram(root: AstRoot, context: RuntimeContext): List<RuntimeObject>
}

object SimpleRuntimeEngine : RuntimeEngine {
    override fun evaluateProgram(root: AstRoot, context: RuntimeContext): List<RuntimeObject> {
        return root.body.map { lexeme ->
            CoreEvaluator.eval(lexeme, context).result
        }
    }
}