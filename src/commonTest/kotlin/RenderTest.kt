import dev.limebeck.templateEngine.Renderer
import dev.limebeck.templateEngine.runtime.MapContext
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderTest {
    @Test
    fun renderSimpleStringTemplate() = runTest {
        val renderer = Renderer()

        val simpleTextTemplate = """
            Hello, {{ name }}!
            Object value: "{{ object.value }}"
        """.trimIndent()

        val data = mapOf(
            "name" to "World",
            "object" to mapOf(
                "value" to "Simple string"
            )
        )

        val expectedOutput = """
            Hello, World!
            Object value: "Simple string"
        """.trimIndent()

        assertEquals(expectedOutput, renderer.render(simpleTextTemplate, null, data).getValueOrNull())
    }

    @Test
    fun renderTemplateWithFunction() = runTest {
        val context = MapContext(mapOf(
            "uppercase" to RuntimeObject.CallableWrapper {
                val value = it.first()
                if (value is String) {
                    value.uppercase()
                } else {
                    throw RuntimeException("<36c2048b> Value must be a string")
                }
            }
        ).toMutableMap())
        val renderer = Renderer(context)

        val template = """
            Hello, {{ uppercase(name) }}!
        """.trimIndent()

        val data = mapOf(
            "name" to "World"
        )

        val expectedOutput = """
            Hello, WORLD!
        """.trimIndent()

        assertEquals(expectedOutput, renderer.render(template, null, data).getValueOrNull())
    }

    @Test
    fun languageReference() {
        val reference = """
            Variable access: {{ variable }}
            Key access: {{ object.value }}
            Index access: {{ array[0] }}
            Function call with round brackets syntax: {{ uppercase(variable) }}
            Function call with pipe syntax: {{ variable | uppercase }}
            Variable assign: {{ let newVariable = "value" | uppercase }}
            Multiline block: {{
                let first = 20
                let second = 30
                first + second
            }}
        """.trimIndent()
    }
}