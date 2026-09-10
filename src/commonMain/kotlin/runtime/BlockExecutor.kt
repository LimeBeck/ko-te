package dev.limebeck.templateEngine.runtime

import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.evaluators.CoreEvaluator

/** Block output is text, distinct from collections rendered as JSON. */
internal object BlockExecutor {
    suspend fun render(nodes: List<AstLexeme>, context: RuntimeContext): String {
        val output = StringBuilder()
        execute(nodes, context, output)
        return output.toString()
    }

    suspend fun execute(nodes: List<AstLexeme>, context: RuntimeContext, output: StringBuilder) {
        for (node in nodes) {
            when (node) {
                is AstLexeme.TemplateSource -> output.append(node.text)
                is AstLexeme.Conditional -> {
                    val condition = CoreEvaluator.eval(node.condition, context).result
                    if (condition !is RuntimeObject.BooleanWrapper) {
                        throw KoteRuntimeException("Condition returned not a boolean value at $node")
                    }
                    val branch = if (condition.value) node.then else node.another.orEmpty()
                    execute(branch, context, output)
                }
                is AstLexeme.Iterator -> executeLoop(node, context, output)
                is AstLexeme.Import -> {
                    val renderer = context.renderer
                    if (renderer is BlockRenderer) {
                        renderer.renderInto(node.path.value, context, output)
                    } else {
                        output.append(render(CoreEvaluator.eval(node, context).result))
                    }
                }
                else -> output.append(render(CoreEvaluator.eval(node, context).result))
            }
        }
    }

    private suspend fun executeLoop(node: AstLexeme.Iterator, context: RuntimeContext, output: StringBuilder) {
        if (node.item.name == "loop") {
            throw KoteRuntimeException("Loop item name 'loop' is reserved for loop metadata")
        }
        val iterable = context.get(node.iterable.name)
        if (iterable !is RuntimeObject.CollectionWrapper) {
            throw KoteRuntimeException("Iterable must be a collection at $node")
        }
        val previousItem = context.getOrNull(node.item.name)
        val previousLoop = context.getOrNull("loop")
        try {
            iterable.collection.forEachIndexed { index, item ->
                context.set(node.item.name, item)
                context.set("loop", mapOf(
                    "index" to index,
                    "number" to index + 1,
                    "first" to (index == 0),
                    "last" to (index == iterable.collection.lastIndex),
                    "length" to iterable.collection.size
                ).wrap())
                execute(node.body, context, output)
            }
        } finally {
            context.set(node.item.name, previousItem)
            context.set("loop", previousLoop)
        }
    }

    private fun RuntimeContext.getOrNull(key: String): RuntimeObject? =
        try {
            get(key)
        } catch (_: KoteRuntimeException) {
            null
        }
}

/** Internal fast path; custom public Renderers continue to return their own strings. */
internal interface BlockRenderer : Renderer {
    suspend fun renderInto(templateName: String, context: RuntimeContext, output: StringBuilder)
}
