import dev.limebeck.templateEngine.*
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.runtime.*
import utils.parseAst
import utils.runTest
import kotlin.test.*

class NullValuesTest {
    private val renderer = KoTeRenderer()

    @Test
    fun nullAstRetainsSourcePosition() = runTest {
        val source = "prefix{{ null }}"
        val node = assertIs<AstLexeme.Null>(source.parseAst().body.last())
        assertEquals(source.indexOf("null"), node.streamPosition.absolutePosition)
    }

    @Test
    fun hostFunctionsCanReturnNullWithoutRemovingBindings() = runTest {
        val engine = KoTeRenderer {
            mapOf("optional" to RuntimeObject.CallableWrapper.from { _, _ -> null })
        }
        assertEquals("null|true", engine.render(
            "{{ let value = optional() }}{{ value }}|{{ value == null }}"
        ).getValueOrNull())
    }

    @Test
    fun literalWorksInAssignmentsGroupsAndArguments() = runTest {
        val identity = KoTeRenderer {
            mapOf("identity" to RuntimeObject.CallableWrapper { args, _ -> args.single() })
        }
        assertEquals("null|null|true", identity.render(
            "{{ let value = null }}{{ value }}|{{ identity((null)) }}|{{ value == null }}"
        ).getValueOrNull())
    }

    @Test
    fun nullEqualityIsSymmetricAndDoesNotCoerceOtherValues() = runTest {
        for (value in listOf(null, "null", "", false, 0, emptyList<String>(), emptyMap<String, Any?>())) {
            val expected = if (value == null) "true|true" else "false|false"
            assertEquals(expected, renderer.render("{{ value == null }}|{{ null == value }}", mapOf("value" to value)).getValueOrNull())
        }
        assertEquals("true", renderer.render("{{ null == null }}").getValueOrNull())
    }

    @Test
    fun explicitNullIsPreservedAtEveryAccessLevel() = runTest {
        assertEquals("null|null|null|true", renderer.render(
            "{{ value }}|{{ obj.value }}|{{ items[0] }}|{{ obj.value == items[0] }}",
            mapOf("value" to null, "obj" to mapOf("value" to null), "items" to listOf(null))
        ).getValueOrNull())
    }

    @Test
    fun missingNamesFieldsAndIndicesAreErrorsEvenInComparisons() = runTest {
        val data = mapOf("obj" to emptyMap<String, Any?>(), "items" to emptyList<String>())
        for (expression in listOf("missing", "obj.missing", "items[0]")) {
            for (suffix in listOf("", " == null")) {
                val error = assertFailsWith<KoteRuntimeException>(expression + suffix) {
                    renderer.render("{{ $expression$suffix }}", data)
                }
                assertTrue(error.reason.orEmpty().contains(if (expression == "items[0]") "0" else "missing"))
            }
        }
    }

    @Test
    fun nullIsNotAnObjectCollectionBooleanNumberOrCallable() = runTest {
        for (template in listOf(
            "{{ null.value }}", "{{ null[0] }}", "{{ null() }}", "{{ null + 1 }}",
            "{{ if (null) }}bad{{ endif }}", "{{ for item in value }}bad{{ endfor }}"
        )) assertFailsWith<KoteRuntimeException>(template) { renderer.render(template, mapOf("value" to null)) }
    }

    @Test
    fun nullableAssignmentsKeepBindingsAndLoopCleanupRestoresThem() = runTest {
        assertEquals("null|true", renderer.render(
            "{{ let item = null }}{{ for item in items }}{{ let other = null }}{{ endfor }}{{ item }}|{{ other == null }}",
            mapOf("items" to listOf(1))
        ).getValueOrNull())
    }

    @Test
    fun namesContainingNullAndQuotedNullRemainOrdinaryValues() = runTest {
        assertEquals("text|null|false", renderer.render(
            "{{ nullable }}|{{ 'null' }}|{{ 'null' == null }}", mapOf("nullable" to "text")
        ).getValueOrNull())
    }

    @Test
    fun unselectedBranchesDoNotResolveMissingData() = runTest {
        assertEquals("empty", renderer.render(
            "{{ if (value == null) }}empty{{ else }}{{ value.missing }}{{ endif }}", mapOf("value" to null)
        ).getValueOrNull())
    }
}
