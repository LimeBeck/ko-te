package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser
import dev.limebeck.templateEngine.runtime.MapContext
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.SimpleRuntimeEngine

typealias JsonObject = Map<String, Any>

class Renderer(
    private val baseContext: MapContext = MapContext.EMPTY
//    contextBuilder: (RuntimeContext.() -> Unit)? = null
) {
    //    private val baseContext = MapContext.EMPTY.apply {
//        contextBuilder?.let { it() }
//    }
    private val runtimeEngine = SimpleRuntimeEngine
    private val tokenizer = MustacheLikeTemplateTokenizer()
    private val languageParser = MustacheLikeLanguageParser()
    private val astParser = KoTeAstParser()

    suspend fun render(
        template: String,
        resources: List<Resource<Any>>?,
        data: JsonObject
    ): Result<String, ParserError> {
        val templateStream = template.toStream()
        val tokens = tokenizer.analyze(templateStream)
        val languageTokens = languageParser.parse(tokens.asSequence())
        val ast = astParser.parse(languageTokens)
        return Result.ofSuccess(
            runtimeEngine.evaluateProgram(
                ast,
                baseContext + MapContext(data.map {
                    it.key to RuntimeObject.Value(it.value)
                }.toMap().toMutableMap())
            )
        ) as Result<String, ParserError>
    }
}

interface Resource<T> {
    val path: String
    val name: String
    val content: T
}



