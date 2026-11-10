import dev.limebeck.templateEngine.KoTeRenderer
import dev.limebeck.templateEngine.parser.LexerError
import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.KoteRuntimeException
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemplateFunctionsTest {
    private val renderer = KoTeRenderer()

    @Test
    fun functionsRenderTheirBodiesAndIsolateLocalAssignments() = runTest {
        val template = "{{ let name = 'outer' }}{{ let punctuation = '!' }}" +
            "{{ fun greet(name, prefix) }}{{ let local = prefix + name }}{{ local }}{{ punctuation }}{{ endfun }}" +
            "{{ greet(prefix = 'Hi ', name = 'Ada') }}|{{ greet('Lin', 'Hello ') }}|{{ name }}"
        assertEquals("Hi Ada!|Hello Lin!|outer", renderer.render(template).getValueOrNull())
        assertFailsWith<KoteRuntimeException> { renderer.render(template + "{{ local }}") }
    }

    @Test
    fun functionReadsFromItsDeclarationScope() = runTest {
        val template = "{{ let suffix = 'outer' }}{{ fun tag() }}{{ suffix }}{{ endfun }}" +
            "{{ fun caller(suffix) }}{{ tag() }}{{ endfun }}{{ caller('inner') }}"
        assertEquals("outer", renderer.render(template).getValueOrNull())
    }

    @Test
    fun invalidDeclarationsAndCallsFail() = runTest {
        assertFailsWith<LexerError> { renderer.render("{{ fun show(value, value) }}{{ endfun }}") }
        assertFailsWith<LexerError> { renderer.render("{{ fun show(value) }}{{ value }}") }
        assertFailsWith<LexerError> { renderer.render("{{ fun show(value }}{{ ) }}{{ endfun }}") }
        assertFailsWith<KoteRuntimeException> { renderer.render("{{ fun show(value) }}{{ value }}{{ endfun }}{{ show() }}") }
    }
}
