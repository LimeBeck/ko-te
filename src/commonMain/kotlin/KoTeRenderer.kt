package dev.limebeck.templateEngine

import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser
import dev.limebeck.templateEngine.runtime.*

typealias JsonObject = Map<String, Any>

class KoTeRenderer(
    private val resourceLoader: ResourceLoader = StaticResourceLoader(),
    predefinedObjectsProvider: () -> Map<String, RuntimeObject> = { emptyMap() }
) {
    private val runtimeEngine = SimpleRuntimeEngine
    private val tokenizer = MustacheLikeTemplateTokenizer()
    private val languageParser = MustacheLikeLanguageParser()
    private val astParser = KoTeAstParser()

    private val initialContext = MapContext(
        predefinedRuntimeObjects = predefinedObjectsProvider(),
        resourceLoader = resourceLoader,
        renderer = object : Renderer {
            override suspend fun render(templateName: String, context: RuntimeContext): Result<String, ParserError> {
                val template = this@KoTeRenderer.resourceLoader.loadTemplate(templateName)
                return this@KoTeRenderer.renderString(template, context)
            }
        }
    )

    internal suspend fun renderString(template: String, context: RuntimeContext): Result<String, ParserError> {
        val templateStream = template.toStream()
        val tokens = tokenizer.analyze(templateStream)
        val languageTokens = languageParser.parse(tokens.asSequence())
        val ast = astParser.parse(languageTokens)
        return Result.ofSuccess(
            runtimeEngine.evaluateProgram(ast, context).render()
        ) as Result<String, ParserError>
    }

    suspend fun render(
        template: String,
        data: JsonObject
    ): Result<String, ParserError> = renderString(
        template = template,
        context = initialContext + data.wrapAll()
    )

    suspend fun renderFromResource(
        templateResourceIdentifier: String,
        data: JsonObject
    ): Result<String, ParserError> = renderString(
        template = resourceLoader.loadTemplate(templateResourceIdentifier),
        context = initialContext + data.wrapAll()
    )

    internal fun ResourceLoader.loadTemplate(
        templateName: String
    ): String {
        val template = loadResource(templateName)
        return template.content.decodeToString()
    }
}
