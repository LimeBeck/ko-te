package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject

object FunctionCallEvaluator : Evaluator<AstLexeme.FunctionCall, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.FunctionCall, context: RuntimeContext): EvalResult<RuntimeObject> {
        val possibleFunction = when (lexeme.identifier) {
            is AstLexeme.Variable -> context.get(lexeme.identifier.name)
            else -> CoreEvaluator.eval(lexeme.identifier, context).result
        }
        if (possibleFunction !is RuntimeObject.CallableWrapper)
            throw KoteRuntimeException("<95ee753e> $lexeme is not a callable")
        val args = evaluateArguments(lexeme.args, possibleFunction.parameterNames, context)
        return EvalResult(possibleFunction.block(args, context))
    }

    private suspend fun evaluateArguments(
        arguments: List<AstLexeme.FunctionArgument>,
        parameterNames: List<String>?,
        context: RuntimeContext
    ): List<RuntimeObject> {
        if (parameterNames == null) {
            if (arguments.any { it.name != null }) {
                throw KoteRuntimeException("This callable does not declare parameter names")
            }
            return arguments.map { CoreEvaluator.eval(it.value, context).result }
        }
        if (arguments.size != parameterNames.size) {
            throw KoteRuntimeException("Expected ${parameterNames.size} arguments, got ${arguments.size}")
        }

        val parameterIndices = parameterNames.withIndex().associate { (index, name) -> name to index }
        val assigned = BooleanArray(parameterNames.size)
        var sawNamedArgument = false
        val order = arguments.mapIndexed { index, argument ->
            val name = argument.name
            val slot = if (name == null) {
                if (sawNamedArgument) throw KoteRuntimeException("Positional arguments must precede named arguments")
                index
            } else {
                sawNamedArgument = true
                parameterIndices[name] ?: throw KoteRuntimeException("Unknown parameter '$name'")
            }
            if (assigned[slot]) throw KoteRuntimeException("Parameter '${parameterNames[slot]}' is already bound")
            assigned[slot] = true
            slot
        }

        val values = MutableList<RuntimeObject>(arguments.size) { RuntimeObject.Nothing }
        for ((index, argument) in arguments.withIndex()) {
            values[order[index]] = CoreEvaluator.eval(argument.value, context).result
        }
        return values
    }
}
