package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object ImportEvaluator : Evaluator<AstLexeme.Import, RuntimeObject> {
    override fun eval(lexeme: AstLexeme.Import, context: RuntimeContext): EvalResult<RuntimeObject> {
        TODO("<9d4a510f> Import is not implemented")
    }
}