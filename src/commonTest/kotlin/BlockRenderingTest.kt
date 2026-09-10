import dev.limebeck.templateEngine.*
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.runtime.*
import kotlinx.coroutines.CancellationException
import utils.parseAst
import utils.runTest
import kotlin.test.*

class BlockRenderingTest {
    @Test
    fun conditionalEmitsEveryNodeOfOnlyTheSelectedBranch() = runTest {
        val renderer = KoTeRenderer()
        val template = "A{{ if (flag) }}B{{ value }}{{ let x = 'X' }}{{ x }}C{{ else }}D{{ value }}E{{ endif }}F"
        assertEquals("ABVXCF", renderer.render(template, mapOf("flag" to true, "value" to "V")).getValueOrNull())
        assertEquals("ADVEF", renderer.render(template, mapOf("flag" to false, "value" to "V")).getValueOrNull())
        assertEquals("ok", renderer.render("{{ if (true) }}ok{{ else }}{{ missing }}{{ endif }}").getValueOrNull())
    }

    @Test
    fun loopsEmitTextAndValuesWithoutJsonWrapping() = runTest {
        assertEquals("[a][null][b]", KoTeRenderer().render(
            "{{ for item in items }}[{{ item }}]{{ endfor }}", mapOf("items" to listOf("a", null, "b"))
        ).getValueOrNull())
    }

    @Test
    fun emptyBodiesAndCollectionsEmitNothing() = runTest {
        for (template in listOf(
            "{{ if (true) }}{{ endif }}", "{{ if (false) }}x{{ else }}{{ endif }}",
            "{{ if (true) }}{{ else }}x{{ endif }}", "{{ for item in items }}{{ endfor }}"
        )) assertEquals("", KoTeRenderer().render(template, mapOf("items" to listOf(1))).getValueOrNull(), template)
        assertEquals("AB", KoTeRenderer().render(
            "A{{ for item in items }}{{ missing }}{{ endfor }}B", mapOf("items" to emptyList<String>())
        ).getValueOrNull())
    }

    @Test
    fun nestedBlocksRestoreLoopMetadataAndItemBindings() = runTest {
        val template = "{{ for item in outer }}{{ item }}:{{ loop.number }}/{{ loop.length }}:" +
            "{{ for item in inner }}{{ item }}{{ loop.index }}{{ if (loop.first) }}F{{ endif }}" +
            "{{ if (loop.last) }}L{{ endif }}{{ endfor }}:{{ item }}{{ loop.index }};{{ endfor }}{{ item }}{{ loop }}"
        assertEquals("a:1/2:x0Fy1L:a0;b:2/2:x0Fy1L:b1;originalsaved", KoTeRenderer().render(
            template, mapOf("outer" to listOf("a", "b"), "inner" to listOf("x", "y"),
                "item" to "original", "loop" to "saved")
        ).getValueOrNull())
    }

    @Test
    fun assignmentsPersistAndIterableIsReadOnce() = runTest {
        assertEquals("ab|ab|", KoTeRenderer().render(
            "{{ let total = '' }}{{ for item in items }}{{ let total = total + item }}{{ item }}" +
                "{{ let items = empty }}{{ endfor }}|{{ total }}|{{ for item in items }}bad{{ endfor }}",
            mapOf("items" to listOf("a", "b"), "empty" to emptyList<String>())
        ).getValueOrNull())
    }

    @Test
    fun importsShareOutputAndAssignmentsInOrder() = runTest {
        val renderer = KoTeRenderer(loader(mapOf(
            "row" to "({{ item }}{{ import 'suffix' }}){{ let last = item }}",
            "suffix" to "{{ if (loop.last) }}!{{ else }}.{{ endif }}"
        )))
        assertEquals("A(a.)(b!)Bb", renderer.render(
            "A{{ for item in items }}{{ import 'row' }}{{ endfor }}B{{ last }}",
            mapOf("items" to listOf("a", "b"))
        ).getValueOrNull())
    }

    @Test
    fun callbacksRunOnceAndInOutputOrder() = runTest {
        val calls = mutableListOf<String>()
        val renderer = KoTeRenderer {
            mapOf("emit" to RuntimeObject.CallableWrapper { args, _ ->
                calls += render(args.single())
                args.single()
            })
        }
        assertEquals("A12B", renderer.render(
            "{{ emit('A') }}{{ if (true) }}{{ for item in items }}{{ emit(item) }}{{ endfor }}" +
                "{{ else }}{{ emit('wrong') }}{{ endif }}{{ emit('B') }}", mapOf("items" to listOf(1, 2))
        ).getValueOrNull())
        assertEquals(listOf("A", "1", "2", "B"), calls)
    }

    @Test
    fun errorsAndCancellationRestoreBindingsAndDoNotLeakOutput() = runTest {
        for (failure in listOf(IllegalStateException("host failure"), CancellationException("cancelled"))) {
            for (initial in listOf(emptyMap(), mapOf("item" to null, "loop" to "outer"))) {
                var captured: RuntimeContext? = null
                val renderer = KoTeRenderer(loader(mapOf("fail" to "prefix{{ fail() }}"))) {
                    mapOf("fail" to RuntimeObject.CallableWrapper { _, context ->
                        captured = context
                        throw failure
                    })
                }
                val actual = assertFails {
                    renderer.render("start{{ for item in items }}{{ import 'fail' }}{{ endfor }}",
                        initial + ("items" to listOf(1)))
                }
                assertSame(failure, actual)
                val context = assertNotNull(captured)
                if (initial.isEmpty()) {
                    assertFailsWith<KoteRuntimeException> { context.get("item") }
                    assertFailsWith<KoteRuntimeException> { context.get("loop") }
                } else {
                    assertSame(RuntimeObject.Null, context.get("item"))
                    assertEquals("outer", render(context.get("loop")))
                }
                assertEquals("ok", renderer.render("ok").getValueOrNull())
            }
        }
    }

    @Test
    fun legacyRuntimeEngineUsesTheSameBlockSemanticsAndCustomRenderer() = runTest {
        val calls = mutableListOf<String>()
        val renderer = object : Renderer {
            override suspend fun render(templateName: String, context: RuntimeContext): Result<String, ParserError> {
                calls += templateName
                return Result.ofSuccess("<${render(context.get("item"))}>")
            }
        }
        val context = mapOf("items" to listOf("a", "b")).wrapAll().asContext(renderer, StaticResourceLoader())
        val result = SimpleRuntimeEngine.evaluateProgram(
            "{{ if (true) }}A{{ for item in items }}{{ import 'row' }}{{ endfor }}B{{ endif }}".parseAst(), context)
        assertEquals("A<a><b>B", result.render())
        assertEquals(listOf("row", "row"), calls)
    }

    @Test
    fun jsonValuesInsideBlocksStillRenderAsJson() = runTest {
        assertEquals("[1,null]|{\"x\":\"y\"}", KoTeRenderer().render(
            "{{ if (true) }}{{ list }}|{{ obj }}{{ endif }}",
            mapOf("list" to listOf(1, null), "obj" to mapOf("x" to "y"))
        ).getValueOrNull())
    }

    @Test
    fun invalidBlocksFailExplicitly() = runTest {
        for (template in listOf(
            "{{ if (true) }}", "{{ if (true) }}{{ else }}", "{{ for item in items }}",
            "{{ if (true) }}{{ endfor }}", "{{ for item in items }}{{ endif }}"
        )) {
            val error = assertFails { KoTeRenderer().render(template, mapOf("items" to listOf(1))) }
            assertTrue(error is ParserError, "$template: $error")
        }
        assertFailsWith<KoteRuntimeException> {
            KoTeRenderer().render("{{ for loop in items }}{{ loop }}{{ endfor }}", mapOf("items" to listOf(1)))
        }
        assertFailsWith<KoteRuntimeException> {
            KoTeRenderer().render("{{ for item in items }}x{{ endfor }}", mapOf("items" to 1))
        }
    }

    @Test
    fun resourceEntryPointUsesTheSameOutputAndRepeatedImportsRemainIndependent() = runTest {
        val renderer = KoTeRenderer(loader(mapOf(
            "main" to "{{ if (true) }}A{{ import 'part' }}{{ import 'part' }}B{{ endif }}",
            "part" to "{{ for item in items }}[{{ item }}]{{ endfor }}"
        )))
        assertEquals("A[x][x]B", renderer.renderFromResource("main", mapOf("items" to listOf("x"))).getValueOrNull())
        assertEquals("A[y][y]B", renderer.renderFromResource("main", mapOf("items" to listOf("y"))).getValueOrNull())
    }

    private fun loader(templates: Map<String, String>) = StaticResourceLoader(templates.map { (name, source) ->
        object : Resource {
            override val identifier = name
            override val content = source.encodeToByteArray()
            override val contentType = "text/kote"
        }
    })
}
