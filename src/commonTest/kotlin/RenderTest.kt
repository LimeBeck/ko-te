import dev.limebeck.templateEngine.KoTeRenderer
import dev.limebeck.templateEngine.parser.LexerError
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.Resource
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.StaticResourceLoader
import dev.limebeck.templateEngine.runtime.standartLibrary.std
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

        assertEquals(expectedOutput, renderer.render(template, data).getValueOrNull()?.normalize())
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

        assertEquals(expectedOutput, renderer.render(simpleTextTemplate, data).getValueOrNull())
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
                data = mapOf()
            ).getValueOrNull()?.trim()
        )
    }

    @Test
    fun functionCall() = runTest {
        val renderer = KoTeRenderer {
            mapOf(
                *std
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

        assertEquals(expectedOutput, renderer.render(template, data).getValueOrNull())
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
                template, mapOf(
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
                template, mapOf(
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
                template, mapOf(
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
                template, mapOf(
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
                template, mapOf(
                    "variable" to false,
                    "username" to username
                )
            ).getValueOrNull()
        )
    }

    @Test
    fun groupExpressionParser() = runTest {
        val renderer = KoTeRenderer()

        val templateWithChangedPrecedence = """{{ ((1 + 2) * 3) * 1 }}""".trimIndent()
        assertEquals("9", renderer.render(templateWithChangedPrecedence, mapOf()).getValueOrNull())

        val errorCases = listOf(
            "{{ (1 + ) }}",
            "{{ ( }}",
            "{{ ) }}",
            "{{ () }}",
        )

        errorCases.forEach {
            assertFailsWith<LexerError> {
                renderer.render(it, mapOf())
            }
        }
    }

    @Test
    fun binaryOperator() = runTest {
        val renderer = KoTeRenderer()

        val templateWithoutPrecedence = """{{ 1 + 2 * 3 }}""".trimIndent()

        assertEquals("7", renderer.render(templateWithoutPrecedence, mapOf()).getValueOrNull())

        val templateWithChangedPrecedence = """{{ (1 + 2) * 3 }}""".trimIndent()

        assertEquals("9", renderer.render(templateWithChangedPrecedence, mapOf()).getValueOrNull())

        val templateWithPrecedence = """{{ 2 * 3 + 1 }}""".trimIndent()

        assertEquals("7", renderer.render(templateWithPrecedence, mapOf()).getValueOrNull())
        val complexTemplate = """{{ obj.two * obj.three + obj.one }}""".trimIndent()
        assertEquals(
            "7",
            renderer.render(
                template = complexTemplate,
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
        val renderer = KoTeRenderer(resourceLoader = StaticResourceLoader(
            resources = listOf(
                object : Resource {
                    override val identifier: String = "resource"
                    override val content = "Some template".encodeToByteArray()
                    override val contentType: String = "text/kote"
                }
            )
        ))

        val template = """{{ import "resource" }}""".trimIndent()

        assertEquals(
            "Some template",
            renderer.render(
                template = template,
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