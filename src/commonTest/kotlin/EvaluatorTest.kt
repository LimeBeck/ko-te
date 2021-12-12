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
        assertEquals(simpleTemplate, result)
    }

    @Test
    fun evaluateTemplateWithVariable() = runTest {
        val template = """Value: {{ variable }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf("variable" to RuntimeObject.Value("value")).asContext()
        )
        assertEquals("Value: value", result)
    }

    @Test
    fun evaluateTemplateWithLiterals() = runTest {
        val template = """{{ "value" }}{{ 100 }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            MapContext.EMPTY
        )
        assertEquals("value100", result)
    }

    @Test
    fun evaluateTemplateWithFunction() = runTest {
        val template = """{{ function(variable) }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf(
                "variable" to RuntimeObject.Value("World"),
                "function" to RuntimeObject.CallableWrapper { args ->
                    val name = args.first()
                    "Hello, $name"
                }
            ).asContext()
        )
        assertEquals("Hello, World", result)
    }

    @Test
    fun evaluateTemplateWithNestedFunction() = runTest {
        val template = """{{ function("Hello")(variable) }}"""
        val runtimeEngine = SimpleRuntimeEngine
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf(
                "variable" to RuntimeObject.Value("World"),
                "function" to RuntimeObject.CallableWrapper { args1 ->
                    RuntimeObject.CallableWrapper { args2 ->
                        "${args1.first()}, ${args2.first()}"
                    }
                }
            ).asContext()
        )
        assertEquals("Hello, World", result)
    }
}