package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object CoreEvaluator : Evaluator<AstLexeme, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme, context: RuntimeContext): EvalResult<RuntimeObject> {
        return when (lexeme) {
            is AstLexeme.TemplateSource -> TemplateEvaluator.eval(lexeme, context)
            is AstLexeme.Variable -> VariableAccessEvaluator.eval(lexeme, context)
            is AstLexeme.Assign -> AssignEvaluator.eval(lexeme, context)
            is AstLexeme.FunctionCall -> FunctionCallEvaluator.eval(lexeme, context)
            is AstLexeme.KeyAccess -> KeyAccessEvaluator.eval(lexeme, context)
            is AstLexeme.IndexAccess -> IndexAccessEvaluator.eval(lexeme, context)
            is AstLexeme.Primitive -> ValueAccessEvaluator.eval(lexeme, context)
            is AstLexeme.Conditional -> ConditionalEvaluator.eval(lexeme, context)
            is AstLexeme.InfixOperation -> OperationEvaluator.eval(lexeme, context)
            is AstLexeme.Import -> ImportEvaluator.eval(lexeme, context)
            is AstLexeme.Iterator -> IterableEvaluator.eval(lexeme, context)
            else -> throw KoteRuntimeException("<2a2090f2> Can`t evaluate expression $lexeme")
        } as EvalResult<RuntimeObject>
    }
}