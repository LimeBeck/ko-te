import dev.limebeck.templateEngine.KoTeRenderer
import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import dev.limebeck.templateEngine.runtime.RuntimeObject
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NamedArgumentsTest {
    private val calls = mutableListOf<String>()
    private val renderer = KoTeRenderer {
        mapOf(
            "join" to RuntimeObject.CallableWrapper.from(listOf("left", "middle", "right")) { args, _ ->
                args.joinToString("|", transform = ::render)
            },
            "record" to RuntimeObject.CallableWrapper { args, _ ->
                args.single().also { calls.add(render(it)) }
            }
        )
    }

    @Test
    fun bindArgumentsByNameAndEvaluateThemInTemplateOrder() = runTest {
        val template = "{{ join('a', right = record('c'), middle = record('b')) }}"
        assertEquals("a|b|c", renderer.render(template).getValueOrNull())
        assertEquals(listOf("c", "b"), calls)
        assertEquals("true|b|c", renderer.render("{{ join(a == a, 'b', 'c') }}", mapOf("a" to 1)).getValueOrNull())
    }

    @Test
    fun rejectInvalidBindingsBeforeEvaluatingArguments() = runTest {
        for (template in listOf(
            "{{ join('a', other = record('b'), right = 'c') }}",
            "{{ join('a', left = record('b'), right = 'c') }}",
            "{{ join(middle = record('b'), 'a', 'c') }}",
            "{{ join(left = record('a')) }}",
            "{{ record(value = 'a') }}"
        )) {
            assertFailsWith<KoteRuntimeException>(template) { renderer.render(template) }
        }
        assertEquals(emptyList(), calls)
    }
}
