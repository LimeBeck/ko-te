import dev.limebeck.templateEngine.Result
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.*
import utils.parseAst
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluatorTest {
    private val renderer = object : Renderer {
        override suspend fun render(templateName: String, context: RuntimeContext): Result<String, ParserError> {
            TODO("<cc8f3059> Not yet implemented")
        }
    }

    @Test
    fun evaluateSimpleTemplate() = runTest {
        val simpleTemplate = """Simple Template"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
                root = simpleTemplate.parseAst(),
                context = mapOf<String, RuntimeObject>().asContext(renderer)
            )
        assertEquals(simpleTemplate, result.render())
    }

    @Test
    fun evaluateTemplateWithVariable() = runTest {
        val template = """Value: {{ variable }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf("variable" to RuntimeObject.StringWrapper("value")).asContext(renderer)
        )
        assertEquals("Value: value", result.render())
    }

    @Test
    fun evaluateTemplateWithLiterals() = runTest {
        val template = """{{ "value" }}{{ 100 }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            context = mapOf<String, RuntimeObject>().asContext(renderer)
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
            ).asContext(renderer)
        )
        assertEquals("Hello, World", result.render())
    }

    @Test
    fun evaluateTemplateWithNestedFunction() = runTest {
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
            ).asContext(renderer)
        )
        assertEquals("Hello, World", result.render())
    }
}