package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.Result
import dev.limebeck.templateEngine.parser.LexerError
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.RuntimeObject

object ImportEvaluator : Evaluator<AstLexeme.Import, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.Import, context: RuntimeContext): EvalResult<RuntimeObject> {
        return when (val result = context.renderer.render(lexeme.path.value, context)) {
            is Result.Success -> EvalResult(RuntimeObject.StringWrapper(result.value))
            is Result.Error -> {
                val error = result.error
                throw (error as? Throwable ?: LexerError(error.position, error.message, error.sourceFile))
            }
        }
    }
}
