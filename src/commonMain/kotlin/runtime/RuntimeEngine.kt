package dev.limebeck.templateEngine.runtime

import dev.limebeck.templateEngine.inputStream.InputStream
import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.AstRoot
import dev.limebeck.templateEngine.runtime.evaluators.CoreEvaluator

interface RuntimeEngine {
    suspend fun evaluateProgram(root: AstRoot, context: RuntimeContext): List<RuntimeObject>
}

object SimpleRuntimeEngine : RuntimeEngine {
    override suspend fun evaluateProgram(root: AstRoot, context: RuntimeContext): List<RuntimeObject> =
        evaluateProgram(root.body.toStream(), context)

    suspend fun evaluateProgram(stream: InputStream<out AstLexeme>, context: RuntimeContext): List<RuntimeObject> =
        buildList {
            while (stream.hasNext()) {
                add(CoreEvaluator.eval(stream, context).result)
            }
        }
}
