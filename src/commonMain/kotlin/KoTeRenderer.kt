package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser
import dev.limebeck.templateEngine.runtime.*

typealias JsonObject = Map<String, Any?>

class KoTeRenderer(
    private val resourceLoader: ResourceLoader = StaticResourceLoader(),
    predefinedObjectsProvider: () -> Map<String, RuntimeObject> = { emptyMap() }
) {
    private val runtimeEngine = SimpleRuntimeEngine
    private val tokenizer = MustacheLikeTemplateTokenizer()
    private val languageParser = MustacheLikeLanguageParser()
    private val astParser = KoTeAstParser()

    private val predefinedObjects = predefinedObjectsProvider()

    private fun createContext(data: JsonObject): RuntimeContext {
        // The stack belongs to one render, so simultaneous renders cannot affect each other.
        val importStack = mutableListOf<String>()
        val renderer = object : Renderer {
            override suspend fun render(templateName: String, context: RuntimeContext): Result<String, ParserError> {
                if (templateName in importStack) {
                    throw KoteRuntimeException("Cyclic template import: ${(importStack + templateName).joinToString(" -> ")}")
                }
                if (importStack.size >= 64) throw KoteRuntimeException("Maximum import depth (64) exceeded")
                importStack.add(templateName)
                try {
                    return renderString(resourceLoader.loadTemplate(templateName), context)
                } finally {
                    importStack.removeAt(importStack.lastIndex)
                }
            }
        }
        return MapContext(predefinedObjects + data.wrapAll(), renderer, resourceLoader)
    }

    internal suspend fun renderString(template: String, context: RuntimeContext): Result<String, ParserError> {
        val templateStream = template.toStream()
        val tokens = tokenizer.analyze(templateStream)
        val languageTokens = languageParser.parse(tokens.asSequence())
        val ast = astParser.parse(languageTokens)
        return Result.ofSuccess(
            runtimeEngine.evaluateProgram(ast, context).render()
        )
    }

    /**
     * Returns a Success on completion. For compatibility, syntax and runtime errors are thrown,
     * not converted to Result.Error. Cancellation and failures from host callbacks propagate.
     */
    suspend fun render(
        template: String,
        data: JsonObject
    ): Result<String, ParserError> = renderString(
        template = template,
        context = createContext(data)
    )

    /** Uses the same throwing error contract as [render], including resource-loader failures. */
    suspend fun renderFromResource(
        templateResourceIdentifier: String,
        data: JsonObject
    ): Result<String, ParserError> {
        val context = createContext(data)
        return context.renderer.render(templateResourceIdentifier, context)
    }

    internal fun ResourceLoader.loadTemplate(
        templateName: String
    ): String {
        val template = loadResource(templateName)
        return template.content.decodeToString()
    }
}

suspend fun KoTeRenderer.render(template: String) = render(template, mapOf())