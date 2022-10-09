import dev.limebeck.templateEngine.KoTeRenderer
import dev.limebeck.templateEngine.parser.LexerError
import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.Resource
import dev.limebeck.templateEngine.runtime.RuntimeObject
import dev.limebeck.templateEngine.runtime.StaticResourceLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RenderTest : FunSpec({
    val renderer = KoTeRenderer()

    test("Json-like map output") {
        fun String.normalize() = replace("\\s".toRegex(), "")

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

        renderer.render(template, data).getValueOrNull()?.normalize() shouldBe expectedOutput
    }

    test("Value access") {
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

        renderer.render(simpleTextTemplate, data).getValueOrNull() shouldBe expectedOutput
    }

    test("Variable assign") {
        val expectedOutput = """
            Hello, World!
        """.trimIndent().trim()

        renderer.render(
            template = """
                {{ let name = "World" }}
                Hello, {{ name }}!
            """.trimIndent(),
            data = mapOf()
        ).getValueOrNull()?.trim() shouldBe expectedOutput

        renderer.render(
            template = """
                {{ 
                    let name = "World" 
                    "Hello, " + name + "!"
                }}
            """.trimIndent(),
            data = mapOf()
        ).getValueOrNull()?.trim() shouldBe expectedOutput
    }

    test("Function call") {
        val renderer = KoTeRenderer {
            mapOf(
                "uppercase" to RuntimeObject.CallableWrapper.from { args, ctx ->
                    val value = args.first()
                    if (value is RuntimeObject.StringWrapper) {
                        value.string.uppercase()
                    } else {
                        throw KoteRuntimeException("<36c2048b> Value must be a string")
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

        renderer.render(template, data).getValueOrNull() shouldBe expectedOutput
    }

    test("Simple condition") {
        val template = """
            Hello, {{ if(condition) }}WORLD{{ else }}{{ username }}{{ endif }}!
        """.trimIndent()

        renderer.render(
            template, mapOf(
                "condition" to true
            )
        ).getValueOrNull() shouldBe """
            Hello, WORLD!
        """.trimIndent()

        renderer.render(
            template, mapOf(
                "condition" to false,
                "username" to "LimeBeck"
            )
        ).getValueOrNull() shouldBe """
            Hello, LimeBeck!
        """.trimIndent()
    }

    test("Iterable for _ in _") {
        val template = """
            Hello, {{ let some = "" }}{{ for value in arr }}{{
            if (some == "") let some = value
            else let some = (some + ', ' + value) endif
                }}{{ endfor }}{{ some }}!
        """.trimIndent()

        renderer.render(
            template,
            mapOf("arr" to listOf("WORLD", "USER"))
        ).getValueOrNull() shouldBe """
            Hello, WORLD, USER!
        """.trimIndent()
    }

    test("Nested condition") {
        val template = """
            Hello, {{ if(variable) }}{{ if(nestedCondition) }}WORLD{{ else }}МИР{{ endif }}{{ else }}{{ username }}{{ endif }}!
        """.trimIndent()

        renderer.render(
            template,
            mapOf(
                "variable" to true,
                "nestedCondition" to true
            )
        ).getValueOrNull() shouldBe """
            Hello, WORLD!
        """.trimIndent()

        renderer.render(
            template,
            mapOf(
                "variable" to true,
                "nestedCondition" to false
            )
        ).getValueOrNull() shouldBe """
            Hello, МИР!
        """.trimIndent()

        renderer.render(
            template,
            mapOf(
                "variable" to false,
                "username" to "LimeBeck"
            )
        ).getValueOrNull() shouldBe """
            Hello, LimeBeck!
        """.trimIndent()
    }

    test("Group expressions") {
        val templateWithChangedPrecedence = """{{ ((1 + 2) * 3) * 1 }}""".trimIndent()
        renderer.render(templateWithChangedPrecedence, mapOf()).getValueOrNull() shouldBe "9"

        listOf(
            "{{ (1 + ) }}",
            "{{ ( }}",
            "{{ ) }}",
            "{{ () }}",
        ).forEach {
            shouldThrow<LexerError> {
                renderer.render(it, mapOf())
            }
        }
    }

    test("Equals render") {
        listOf(
            """{{ 1 == 1 }}""",
            """{{ (1 == 1) == true }}"""
        ).forEach {
            renderer.render(it).getValueOrNull() shouldBe "true"
        }

        listOf(
            """{{ 1 == "1" }}""",
            """{{ 1 == "1" == 1 }}"""
        ).forEach {
            renderer.render(it).getValueOrNull() shouldBe "false"
        }
    }

    test("Binary operators") {
        val templateWithoutPrecedence = """{{ 1 + 2 * 3 }}""".trimIndent()
        renderer.render(templateWithoutPrecedence, mapOf()).getValueOrNull() shouldBe "7"

        val templateWithPrecedence = """{{ 2 * 3 + 1 }}""".trimIndent()
        renderer.render(templateWithPrecedence, mapOf()).getValueOrNull() shouldBe "7"

        val complexTemplate = """{{ obj.two * obj.three + obj.one }}""".trimIndent()
        renderer.render(
            template = complexTemplate,
            data = mapOf(
                "obj" to mapOf(
                    "one" to 1,
                    "two" to 2,
                    "three" to 3
                )
            )
        ).getValueOrNull() shouldBe "7"
    }

    test("Import template") {
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
        renderer.render(
            template = template,
            data = mapOf()
        ).getValueOrNull() shouldBe "Some template"
    }
})