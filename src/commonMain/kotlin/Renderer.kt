package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser
import dev.limebeck.templateEngine.runtime.*

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
                baseContext + MapContext(data.wrapAll().toMutableMap())
            ).render()
        ) as Result<String, ParserError>
    }
}

interface Resource<T> {
    val path: String
    val name: String
    val content: T
}

fun List<RuntimeObject>.render() = joinToString("") { render(it) }

fun renderObject(obj: RuntimeObject.ObjectWrapper): String {
    return """{ ${
        obj.obj.entries.joinToString(",") {
            val value = it.value
            """ "${it.key}": ${if (value is RuntimeObject.StringWrapper) "\"${value.string}\"" else render(value)}  """
        }
    } }"""
}

fun render(value: RuntimeObject): String {
    return when (value) {
        is RuntimeObject.StringWrapper -> value.string
        is RuntimeObject.Null -> "NULL"
        is RuntimeObject.NumberWrapper -> value.number.toString()
        is RuntimeObject.BooleanWrapper -> value.value.toString()
        is RuntimeObject.CollectionWrapper -> "[${value.collection.joinToString(",") { render(it) }}]"
        is RuntimeObject.ObjectWrapper -> renderObject(value)
        is RuntimeObject.CallableWrapper -> "TODO"
    }
}


