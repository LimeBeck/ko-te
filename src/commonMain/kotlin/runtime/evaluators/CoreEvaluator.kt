package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object CoreEvaluator : Evaluator<AstLexeme, Any> {
    override fun eval(lexeme: AstLexeme, context: RuntimeContext): EvalResult<Any> {
        return when (lexeme) {
            is AstLexeme.TemplateSource -> TemplateEvaluator.eval(lexeme, context) as EvalResult<Any>
            is AstLexeme.Variable -> VariableEvaluator.eval(lexeme, context)
            is AstLexeme.FunctionCall -> FunctionEvaluator.eval(lexeme, context)
            is AstLexeme.Value -> ValueEvaluator.eval(lexeme, context) as EvalResult<Any>
            else -> throw RuntimeException("<2a2090f2> Can`t evaluate expression $lexeme")
        }
    }
}

object FunctionEvaluator : Evaluator<AstLexeme.FunctionCall, Any> {
    override fun eval(lexeme: AstLexeme.FunctionCall, context: RuntimeContext): EvalResult<Any> {
        val possibleFunction = when (lexeme.identifier) {
            is AstLexeme.Variable -> context.get(lexeme.identifier.name)
            else -> CoreEvaluator.eval(lexeme.identifier, context).result
        }
        if (possibleFunction !is RuntimeObject.Callable)
            throw RuntimeException("<95ee753e> $lexeme is not a callable")
        val args = lexeme.args.map {
            CoreEvaluator.eval(it.value, context).result
        }
        return EvalResult(possibleFunction.block(args))
    }
}