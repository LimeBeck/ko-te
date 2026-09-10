import dev.limebeck.templateEngine.*
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.*
import utils.parseAst
import utils.runTest
import kotlin.test.*

class UnaryOperatorsTest {
    private val renderer = KoTeRenderer()

    @Test
    fun precedenceAndAdjacentOperators() = runTest {
        for ((expression, expected) in mapOf(
            "-2 * 3 + 8" to "2", "-(2 + 3) * 4" to "-20", "1--2" to "3",
            "1+-2" to "-1", "2*-3" to "-6", "--2" to "2", "+-+2" to "-2",
            "!!true" to "true", "!true == false" to "true", "!(1 == 2)" to "true",
            "-1.25" to "-1.25", "+2.5" to "2.5", "6/-2" to "-3"
        )) assertEquals(expected, renderer.render("{{ $expression }}").getValueOrNull(), expression)
    }

    @Test
    fun suffixesBindBeforePrefixesAndOperandRunsOnce() = runTest {
        var calls = 0
        val engine = KoTeRenderer {
            mapOf("data" to RuntimeObject.CallableWrapper.from { _, _ ->
                calls++
                mapOf("values" to listOf(3))
            })
        }
        assertEquals("-3", engine.render("{{ -data().values[0] }}").getValueOrNull())
        assertEquals(1, calls)
    }

    @Test
    fun assignmentsArgumentsAndConditions() = runTest {
        val engine = KoTeRenderer {
            mapOf("identity" to RuntimeObject.CallableWrapper { args, _ -> args.single() })
        }
        assertEquals("-3", engine.render(
            "{{ let n = -3 }}{{ if (!false) }}{{ identity(+n) }}{{ endif }}"
        ).getValueOrNull())
    }

    @Test
    fun unaryOperatorsDoNotCoerceValues() = runTest {
        for (expression in listOf("!0", "!'true'", "!null", "-true", "+'1'", "-null", "+null")) {
            assertFailsWith<KoteRuntimeException>(expression) { renderer.render("{{ $expression }}") }
        }
        for (expression in listOf("-missing", "!obj.absent")) {
            assertFailsWith<KoteRuntimeException> { renderer.render("{{ $expression }}", mapOf("obj" to emptyMap<String, Any?>())) }
        }
    }

    @Test
    fun checkedNumericBoundaries() = runTest {
        assertEquals(Long.MIN_VALUE.toString(), renderer.render("{{ +n }}", mapOf("n" to Long.MIN_VALUE)).getValueOrNull())
        assertEquals((-Long.MAX_VALUE).toString(), renderer.render("{{ -n }}", mapOf("n" to Long.MAX_VALUE)).getValueOrNull())
        assertFailsWith<KoteRuntimeException> { renderer.render("{{ -n }}", mapOf("n" to Long.MIN_VALUE)) }
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            for (operator in listOf("+", "-")) {
                assertFailsWith<KoteRuntimeException> { renderer.render("{{ ${operator}n }}", mapOf("n" to value)) }
            }
        }
    }

    @Test
    fun prefixAstRetainsOperatorSourcePosition() = runTest {
        val source = "prefix{{ -2 }}"
        val node = assertIs<AstLexeme.PrefixOperation>(source.parseAst().body.last())
        assertEquals(source.indexOf('-'), node.streamPosition.absolutePosition)
    }

    @Test
    fun malformedExpressionsFail() = runTest {
        for (expression in listOf("-", "!", "1+", "1===2", "1!2", "2**3", "-( )")) {
            assertFails(expression) { renderer.render("{{ $expression }}") }
        }
    }
}
