package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser
import dev.limebeck.templateEngine.runtime.*

typealias JsonObject = Map<String, Any>

class Renderer(
    private val predefinedObjectsProvider: () -> Map<String, RuntimeObject> = { emptyMap() }
) {
    private val runtimeEngine = SimpleRuntimeEngine
    private val tokenizer = MustacheLikeTemplateTokenizer()
    private val languageParser = MustacheLikeLanguageParser()
    private val astParser = KoTeAstParser()

    suspend fun render(
        template: String,
        resources: List<Resource>?,
        data: JsonObject
    ): Result<String, ParserError> {
        val templateStream = template.toStream()
        val tokens = tokenizer.analyze(templateStream)
        val languageTokens = languageParser.parse(tokens.asSequence())
        val ast = astParser.parse(languageTokens)
        return Result.ofSuccess(
            runtimeEngine.evaluateProgram(
                ast,
                MapContext(
                    predefinedRuntimeObjects = data.wrapAll().toMutableMap(),
                    resources = resources ?: emptyList()
                ) + predefinedObjectsProvider()
            ).render()
        ) as Result<String, ParserError>
    }
}

interface Resource {
    val identifier: String
    val content: ByteArray
    val contentType: String
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
        RuntimeObject.Nothing -> ""
    }
}


