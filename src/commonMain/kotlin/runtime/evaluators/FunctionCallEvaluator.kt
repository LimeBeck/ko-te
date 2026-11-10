package dev.limebeck.templateEngine.runtime.evaluators

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.BlockExecutor
import dev.limebeck.templateEngine.runtime.RuntimeContext
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.childScope

object FunctionCallEvaluator : Evaluator<AstLexeme.FunctionCall, RuntimeObject> {
    override suspend fun eval(lexeme: AstLexeme.FunctionCall, context: RuntimeContext): EvalResult<RuntimeObject> {
        val possibleFunction = when (lexeme.identifier) {
            is AstLexeme.Variable -> context.get(lexeme.identifier.name)
            else -> CoreEvaluator.eval(lexeme.identifier, context).result
        }
        val result = when (possibleFunction) {
            is RuntimeObject.CallableWrapper -> {
                val args = evaluateArguments(lexeme.args, possibleFunction.parameterNames, context)
                possibleFunction.block(args, context)
            }
            is RuntimeObject.TemplateFunction -> {
                val parameters = possibleFunction.declaration.parameters
                val args = evaluateArguments(lexeme.args, parameters, context)
                val scope = possibleFunction.closure.childScope()
                for ((index, name) in parameters.withIndex()) {
                    scope.set(name, args[index])
                }
                RuntimeObject.StringWrapper(BlockExecutor.render(possibleFunction.declaration.body, scope))
            }
            else -> throw KoteRuntimeException("<95ee753e> $lexeme is not a callable")
        }
        return EvalResult(result)
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
