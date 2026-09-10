package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.BlockExecutor
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object ConditionalEvaluator : Evaluator<AstLexeme.Conditional, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.Conditional, context: RuntimeContext): EvalResult<RuntimeObject> =
        EvalResult(RuntimeObject.StringWrapper(BlockExecutor.render(listOf(lexeme), context)))
}
