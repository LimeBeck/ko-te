package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object CoreEvaluator : Evaluator<AstLexeme, RuntimeObject> {
    override fun eval(lexeme: AstLexeme, context: RuntimeContext): EvalResult<RuntimeObject> {
        return when (lexeme) {
            is AstLexeme.TemplateSource -> TemplateEvaluator.eval(lexeme, context)
            is AstLexeme.Variable -> VariableEvaluator.eval(lexeme, context)
            is AstLexeme.FunctionCall -> FunctionEvaluator.eval(lexeme, context)
            is AstLexeme.KeyAccess -> KeyAccessEvaluator.eval(lexeme, context)
            is AstLexeme.IndexAccess -> IndexAccessEvaluator.eval(lexeme, context)
            is AstLexeme.Primitive -> ValueEvaluator.eval(lexeme, context)
            is AstLexeme.Conditional -> ConditionalEvaluator.eval(lexeme, context)
            else -> throw RuntimeException("<2a2090f2> Can`t evaluate expression $lexeme")
        } as  EvalResult<RuntimeObject>
    }
}