import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.MapContext
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.SimpleRuntimeEngine
import dev.limebeck.templateEngine.runtime.asContext
import utils.parseAst
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluatorTest {
    @Test
    fun evaluateSimpleTemplate() = runTest {
        val simpleTemplate = """Simple Template"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(simpleTemplate.parseAst(), MapContext.EMPTY)
        assertEquals(simpleTemplate, result.render())
    }

    @Test
    fun evaluateTemplateWithVariable() = runTest {
        val template = """Value: {{ variable }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf("variable" to RuntimeObject.StringWrapper("value")).asContext()
        )
        assertEquals("Value: value", result.render())
    }

    @Test
    fun evaluateTemplateWithLiterals() = runTest {
        val template = """{{ "value" }}{{ 100 }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            MapContext.EMPTY
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
            ).asContext()
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
            ).asContext()
        )
        assertEquals("Hello, World", result.render())
    }
}