package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object TemplateEvaluator: Evaluator<AstLexeme.TemplateSource, RuntimeObject.StringWrapper> {
    override suspend fun eval(lexeme: AstLexeme.TemplateSource, context: RuntimeContext): EvalResult<RuntimeObject.StringWrapper> {
        return EvalResult(RuntimeObject.StringWrapper(lexeme.text))
    }
}