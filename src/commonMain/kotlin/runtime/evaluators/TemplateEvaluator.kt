package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext

object TemplateEvaluator: Evaluator<AstLexeme.TemplateSource, String> {
    override fun eval(lexeme: AstLexeme.TemplateSource, context: RuntimeContext): EvalResult<String> {
        return EvalResult(lexeme.text)
    }
}