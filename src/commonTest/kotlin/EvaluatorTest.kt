import dev.limebeck.templateEngine.Result
import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.*
import dev.limebeck.templateEngine.runtime.evaluators.CoreEvaluator
import utils.parseAst
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class EvaluatorTest {
    private val emptyResourceLoader = StaticResourceLoader()

    private val renderer = object : Renderer {
        override suspend fun render(templateName: String, context: RuntimeContext): Result<String, ParserError> {
            TODO("<cc8f3059> Not yet implemented")
        }
    }

    @Test
    fun runtimeCanConsumeAstStreamsIncrementally() = runTest {
        val context = emptyMap<String, RuntimeObject>().asContext(renderer, emptyResourceLoader)
        val stream = "{{ 1 }}{{ missing }}{{ 3 }}".parseAst().body.toStream()

        assertEquals("1", render(CoreEvaluator.eval(stream, context).result))
        assertFailsWith<KoteRuntimeException> { SimpleRuntimeEngine.evaluateProgram(stream, context) }
        assertEquals(2, stream.currentPosition.absolutePosition)
        assertEquals("3", SimpleRuntimeEngine.evaluateProgram(stream, context).render())
        assertFalse(stream.hasNext())
        assertEquals(emptyList(), SimpleRuntimeEngine.evaluateProgram(stream, context))
    }

    @Test
    fun evaluateSimpleTemplate() = runTest {
        val simpleTemplate = """Simple Template"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
                root = simpleTemplate.parseAst(),
                context = mapOf<String, RuntimeObject>().asContext(renderer, emptyResourceLoader)
            )
        assertEquals(simpleTemplate, result.render())
    }

    @Test
    fun evaluateTemplateWithVariable() = runTest {
        val template = """Value: {{ variable }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf("variable" to RuntimeObject.StringWrapper("value")).asContext(renderer, emptyResourceLoader)
        )
        assertEquals("Value: value", result.render())
    }

    @Test
    fun evaluateTemplateWithLiterals() = runTest {
        val template = """{{ "value" }}{{ 100 }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            context = mapOf<String, RuntimeObject>().asContext(renderer, emptyResourceLoader)
        )
        assertEquals("value100", result.render())
    }

    @Test
    fun evaluateTemplateWithFunction() = runTest {
        val template = """{{ function(variable) }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf(
                "variable" to RuntimeObject.StringWrapper("World"),
                "function" to RuntimeObject.CallableWrapper { args, ctx ->
                    val name = args.first() as RuntimeObject.StringWrapper
                    RuntimeObject.StringWrapper("Hello, ${name.string}")
                }
            ).asContext(renderer, emptyResourceLoader)
        )
        assertEquals("Hello, World", result.render())
    }

    @Test
    fun evaluateTemplateWithHighOrderFunction() = runTest {
        val template = """{{ function("Hello")(variable) }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf(
                "variable" to RuntimeObject.StringWrapper("World"),
                "function" to RuntimeObject.CallableWrapper { args1, _ ->
                    RuntimeObject.CallableWrapper.from { args2, _ ->
                        "${(args1.first() as RuntimeObject.StringWrapper).string}, ${(args2.first() as RuntimeObject.StringWrapper).string}"
                    }
                }
            ).asContext(renderer, emptyResourceLoader)
        )
        assertEquals("Hello, World", result.render())
    }
}
