import dev.limebeck.templateEngine.KoTeRenderer
import dev.limebeck.templateEngine.Resource
import dev.limebeck.templateEngine.runtime.RuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderTest {
    @Test
    fun jsonLikeMapOutput() = runTest {
        fun String.normalize() = replace("\\s".toRegex(), "")

        val renderer = KoTeRenderer()
        val template = "{{ data }}"
        //language=JSON
        val expectedOutput = """
            {
              "string": "value",
               "number": 1.2,
               "bool": true,
               "array": ["string", 1.2, false]
            }
        """.trimIndent().normalize()

        val data = mapOf(
            "data" to mapOf(
                "string" to "value",
                "number" to 1.2,
                "bool" to true,
                "array" to listOf("string", 1.2, false)
            )
        )

        assertEquals(expectedOutput, renderer.render(template, null, data).getValueOrNull()?.normalize())
    }

    @Test
    fun valueAccess() = runTest {
        val renderer = KoTeRenderer()

        val simpleTextTemplate = """
            Hello, {{ name }}!
            Object value: "{{ object.value[0] }}"
        """.trimIndent()

        val data = mapOf(
            "name" to "World",
            "object" to mapOf(
                "value" to listOf("Simple string")
            )
        )

        val expectedOutput = """
            Hello, World!
            Object value: "Simple string"
        """.trimIndent()

        assertEquals(expectedOutput, renderer.render(simpleTextTemplate, null, data).getValueOrNull())
    }

    @Test
    fun variableAssign() = runTest {
        val renderer = KoTeRenderer()

        val expectedOutput = """
            Hello, World!
        """.trimIndent().trim()

        assertEquals(
            expectedOutput,
            renderer.render(
                template = """
                    {{ let name = "World" }}
                    Hello, {{ name }}!
                """.trimIndent(),
                resources = null,
                data = mapOf()
            ).getValueOrNull()?.trim()
        )

        assertEquals(
            expectedOutput,
            renderer.render(
                template = """
                    {{ 
                        let name = "World" 
                        "Hello, " + name + "!"
                    }}
                """.trimIndent(),
                resources = null,
                data = mapOf()
            ).getValueOrNull()?.trim()
        )
    }

    @Test
    fun functionCall() = runTest {
        val renderer = KoTeRenderer {
            mapOf(
                "uppercase" to RuntimeObject.CallableWrapper.from { args, ctx ->
                    val value = args.first()
                    if (value is RuntimeObject.StringWrapper) {
                        value.string.uppercase()
                    } else {
                        throw RuntimeException("<36c2048b> Value must be a string")
                    }
                }
            ).toMutableMap()
        }

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
    fun simpleCondition() = runTest {
        val renderer = KoTeRenderer()

        val template = """
            Hello, {{ if(variable) }}WORLD{{ else }}{{ username }}{{ endif }}!
        """.trimIndent()

        val expectedOutputTrue = """
            Hello, WORLD!
        """.trimIndent()

        assertEquals(
            expectedOutputTrue, renderer.render(
                template, null, mapOf(
                    "variable" to true
                )
            ).getValueOrNull()
        )

        val username = "LimeBeck"
        val expectedOutputFalse = """
            Hello, LimeBeck!
        """.trimIndent()

        assertEquals(
            expectedOutputFalse, renderer.render(
                template, null, mapOf(
                    "variable" to false,
                    "username" to username
                )
            ).getValueOrNull()
        )
    }

    @Test
    fun nestedCondition() = runTest {
        val renderer = KoTeRenderer()

        val template = """
            Hello, {{ if(variable) }}{{ if(nestedCondition) }}WORLD{{ else }}МИР{{ endif }}{{ else }}{{ username }}{{ endif }}!
        """.trimIndent()

        val expectedOutputTrue = """
            Hello, WORLD!
        """.trimIndent()

        assertEquals(
            expectedOutputTrue, renderer.render(
                template, null, mapOf(
                    "variable" to true,
                    "nestedCondition" to true
                )
            ).getValueOrNull()
        )

        val expectedOutputNestedTrue = """
            Hello, МИР!
        """.trimIndent()

        assertEquals(
            expectedOutputNestedTrue, renderer.render(
                template, null, mapOf(
                    "variable" to true,
                    "nestedCondition" to false
                )
            ).getValueOrNull()
        )

        val username = "LimeBeck"
        val expectedOutputFalse = """
            Hello, LimeBeck!
        """.trimIndent()

        assertEquals(
            expectedOutputFalse, renderer.render(
                template, null, mapOf(
                    "variable" to false,
                    "username" to username
                )
            ).getValueOrNull()
        )
    }

    @Test
    fun binaryOperator() = runTest {
        val renderer = KoTeRenderer()

        val templateWithoutPrecedence = """{{ 1 + 2 * 3 }}""".trimIndent()

        assertEquals("7", renderer.render(templateWithoutPrecedence, null, mapOf()).getValueOrNull())

        val templateWithPrecedence = """{{ 2 * 3 + 1 }}""".trimIndent()

        assertEquals("7", renderer.render(templateWithPrecedence, null, mapOf()).getValueOrNull())

        val complexTemplate = """{{ obj.two * obj.three + obj.one }}""".trimIndent()
        assertEquals(
            "7",
            renderer.render(
                template = complexTemplate,
                resources = null,
                data = mapOf(
                    "obj" to mapOf(
                        "one" to 1,
                        "two" to 2,
                        "three" to 3
                    )
                )
            ).getValueOrNull()
        )
    }

    @Test
    fun importTemplate() = runTest {
        val renderer = KoTeRenderer()

        val template = """{{ import "resource" }}""".trimIndent()

        assertEquals(
            "Some template",
            renderer.render(
                template = template,
                resources = listOf(
                    object : Resource {
                        override val identifier: String = "resource"
                        override val content = "Some template".encodeToByteArray()
                        override val contentType: String = "text/kote"
                    }
                ),
                data = mapOf()
            ).getValueOrNull()
        )
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