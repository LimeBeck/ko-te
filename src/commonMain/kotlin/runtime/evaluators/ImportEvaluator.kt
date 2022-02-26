package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.wrap

object ImportEvaluator : Evaluator<AstLexeme.Import, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.Import, context: RuntimeContext): EvalResult<RuntimeObject> {
        return EvalResult(context.renderer.render(lexeme.path.value, context).getValueOrNull().wrap())
    }
}