import dev.limebeck.templateEngine.*
import dev.limebeck.templateEngine.inputStream.CharInputStream
import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.*
import dev.limebeck.templateEngine.runtime.*
import kotlinx.serialization.json.*
import kotlinx.coroutines.CancellationException
import utils.runTest
import kotlin.test.*

class ReviewRegressionTest {
    private val renderer = KoTeRenderer()

    @Test
    fun arithmeticPrecedenceAndAssociativity() = runTest {
        val cases = mapOf(
            "10 - 3 - 2" to "5", "20 / 2 / 2" to "5", "10 % 3 * 2" to "2",
            "1 + 2 == 3" to "true", "2 * (3 + 1)" to "8",
            "(2 + 3) * (4 + 1)" to "25", "10 - (3 - 2)" to "9",
            "2 * ((3 + 1) * 2)" to "16", "18 / 3 * 2" to "12",
            "8 / (2 * 2)" to "2", "1 == 2 + 3" to "false",
            "1 + 2 * 3 - 4 / 2" to "5"
        )
        for ((expression, expected) in cases) {
            assertEquals(expected, renderer.render("{{ $expression }}").getValueOrNull(), expression)
        }
    }

    @Test
    fun parenthesesInFunctionArgumentsAndChainedCalls() = runTest {
        val functions = KoTeRenderer {
            mapOf(
                "identity" to RuntimeObject.CallableWrapper { args, _ -> args.single() },
                "sum" to RuntimeObject.CallableWrapper.from { args, _ ->
                    args.sumOf { (it as RuntimeObject.NumberWrapper).number.toLong() }
                }
            )
        }
        assertEquals("8", functions.render("{{ identity(2 * (3 + 1)) }}").getValueOrNull())
        assertEquals("3", functions.render("{{ identity(identity(1 + 2)) }}").getValueOrNull())
        assertEquals("6", functions.render("{{ sum(1, sum(2, 3)) }}").getValueOrNull())
    }

    @Test
    fun nullsPreserveCollectionIndicesAndTopLevelValues() = runTest {
        val data = mapOf("a" to listOf(null, "x", null, listOf(null, "y")), "n" to null)
        assertEquals("null|x|null|y|null", renderer.render(
            "{{ a[0] }}|{{ a[1] }}|{{ a[2] }}|{{ a[3][1] }}|{{ n }}", data
        ).getValueOrNull())
        assertEquals(Json.parseToJsonElement("[null,\"x\",null,[null,\"y\"]]"),
            Json.parseToJsonElement(renderer.render("{{ a }}", data).getValueOrNull()!!))
    }

    @Test
    fun longArithmeticAndMixedNumberEquality() = runTest {
        val data = mapOf("n" to 2147483648L, "d" to 1.2, "whole" to 1L)
        assertEquals("2147483649", renderer.render("{{ n + 1 }}", data).getValueOrNull())
        assertEquals("2147483649", renderer.render("{{ 2147483648 + 1 }}").getValueOrNull())
        assertEquals("true", renderer.render("{{ d == 1.2 }}", data).getValueOrNull())
        assertEquals("true", renderer.render("{{ whole == 1 }}", data).getValueOrNull())
        assertEquals("true", renderer.render("{{ 1.5 + 2.5 == 4 }}").getValueOrNull())
        assertEquals("true", renderer.render("{{ 5.0 / 2.0 == 2 }}").getValueOrNull())
        assertEquals("9223372036854775807", renderer.render("{{ n }}", mapOf("n" to Long.MAX_VALUE)).getValueOrNull())
        assertEquals("false", renderer.render("{{ a == b }}", mapOf(
            "a" to 9007199254740993L, "b" to 9007199254740992.0
        )).getValueOrNull())
    }

    @Test
    fun arithmeticRejectsOverflowAndNonFiniteResults() = runTest {
        val cases = listOf(
            "max + 1", "min - 1", "max * 2", "min * minusOne", "min / minusOne",
            "1 / 0", "1 % 0", "1.5 / 0", "max + 0.5", "huge * huge", "nan + 1"
        )
        val data = mapOf("max" to Long.MAX_VALUE, "min" to Long.MIN_VALUE,
            "minusOne" to -1, "huge" to Double.MAX_VALUE, "nan" to Double.NaN)
        for (expression in cases) assertFailsWith<KoteRuntimeException>(expression) {
            renderer.render("{{ $expression }}", data)
        }
        assertEquals("0", renderer.render("{{ min % minusOne }}", data).getValueOrNull())
        assertEquals("-9223372036854775808", renderer.render("{{ min * 1 }}", data).getValueOrNull())
    }

    @Test
    fun delimitersAndEscapesInsideStrings() = runTest {
        val cases = mapOf(
            "{{ \"}}\" }}" to "}}", "{{ '{{' }}" to "{{",
            "{{ \"a\\\"}}b\" }}" to "a\"}}b",
            "{{ 'it\\'s }}' }}" to "it's }}",
            "{{ \"\\n\\t\\r\\b\\f\\u0041\\\\\" }}" to "\n\t\r\b\u000CA\\",
            "{{ \"\" }}" to "", "{{ '' }}" to "",
            "{{ \"\"\"first\n}}\\n\"last\"\"\" }}" to "first\n}}\\n\"last"
        )
        for ((template, expected) in cases) assertEquals(expected, renderer.render(template).getValueOrNull(), template)
    }

    @Test
    fun invalidStringsAndNumbersAreParserErrors() = runTest {
        for (template in listOf("{{ \"abc }}", "{{ \"\\q\" }}", "{{ \"\\uZZZZ\" }}",
            "{{ \"\"\"abc }}", "{{ 9223372036854775808 }}", "{{ 1.2.3 }}", "{{ a[2147483648] }}", "{{ 1")) {
            val error = assertFails { renderer.render(template) }
            assertTrue(error is ParserError, "$template: $error")
        }
    }

    @Test
    fun jsonEscapesKeysAndNestedValues() = runTest {
        val key = "quote\"\\\n"
        val value = "a\"b\nc\t\r\b\u000C\u0000\\😀"
        val data = mapOf("d" to mapOf(key to value, "n" to null, "a" to listOf(value, null)))
        val actual = Json.parseToJsonElement(renderer.render("{{ d }}", data).getValueOrNull()!!)
        assertEquals(buildJsonObject {
            put(key, value)
            put("n", JsonNull)
            put("a", buildJsonArray { add(value); add(JsonNull) })
        }, actual)
        assertEquals(value, renderer.render("{{ v }}", mapOf("v" to value)).getValueOrNull())
    }

    @Test
    fun jsonRejectsNonFiniteNumbersAndFunctions() = runTest {
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, RuntimeObject.CallableWrapper { _, _ -> RuntimeObject.Null })) {
            assertFailsWith<KoteRuntimeException> {
                renderer.render("{{ d }}", mapOf("d" to mapOf("value" to value)))
            }
        }
    }

    @Test
    fun tokenPositionsPointAtActualSource() = runTest {
        val source = "header\n{{\n  first + 2\n}}"
        val tokens = MustacheLikeLanguageParser().parse(MustacheLikeTemplateTokenizer().analyze(source.toStream()).asSequence()).toList()
        val first = tokens.filterIsInstance<LanguageToken.Identifier>().single()
        val position = first.startPosition as CharInputStream.StringPosition
        assertEquals(source.indexOf("first"), position.absolutePosition)
        assertEquals(3, position.line)
        assertEquals(2, position.column)
        assertEquals(source.indexOf('+'), tokens.filterIsInstance<LanguageToken.Operation>().single().startPosition.absolutePosition)
    }

    @Test
    fun importCyclesAndDepthAreBounded() = runTest {
        fun engine(templates: Map<String, String>) = KoTeRenderer(StaticResourceLoader(templates.map { (name, text) ->
            object : Resource {
                override val identifier = name
                override val content = text.encodeToByteArray()
                override val contentType = "text/kote"
            }
        }))
        val cyclic = engine(mapOf("a" to "{{ import 'b' }}", "b" to "{{ import 'a' }}", "good" to "ok"))
        assertTrue(assertFailsWith<KoteRuntimeException> {
            cyclic.renderFromResource("a", emptyMap())
        }.reason!!.contains("Cyclic"))
        assertEquals("okok", cyclic.render("{{ import 'good' }}{{ import 'good' }}").getValueOrNull())
        val deep = engine((0..65).associate { "r$it" to if (it == 65) "end" else "{{ import 'r${it + 1}' }}" })
        assertTrue(assertFailsWith<KoteRuntimeException> {
            deep.renderFromResource("r0", emptyMap())
        }.reason!!.contains("depth"))
    }

    @Test
    fun errorsAndCancellationPropagateAndRendersAreIsolated() = runTest {
        assertEquals("changed", renderer.render("{{ let name = 'changed' }}{{ name }}").getValueOrNull())
        assertFailsWith<KoteRuntimeException> { renderer.render("{{ name }}") }
        val cancelled = KoTeRenderer {
            mapOf("cancel" to RuntimeObject.CallableWrapper { _, _ -> throw CancellationException("cancelled") })
        }
        assertFailsWith<CancellationException> { cancelled.render("{{ cancel() }}") }
    }
}
