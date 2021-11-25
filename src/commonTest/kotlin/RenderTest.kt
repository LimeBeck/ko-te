import dev.limebeck.templateEngine.Renderer
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderTest {
    @Test
    fun renderSimpleStringTemplate() {
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
    fun renderTemplateWithFunction() {
        val renderer = Renderer()

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
}