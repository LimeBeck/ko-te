import dev.limebeck.templateEngine.parser.ast.AstLexeme
import utils.parseAst
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class AstParserTest {

    @Test
    fun parseVariableFromTemplate() = runTest {
        val astLexemes = """
            Hello, {{ name }}!
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(3, list.size)
    }

    @Test
    fun parseStringValueFromTemplate() = runTest {
        val astLexemes = """
            {{ "World" }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(listOf(AstLexeme.String("World")), list)
    }

    @Test
    fun parseNumericValueFromTemplate() = runTest {
        val astLexemes = """
            {{ 123 }}{{ 123.456 }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(2, list.size)
        val expected = listOf(
            AstLexeme.Number(123),
            AstLexeme.Number(123.456F)
        )
        assertContentEquals(expected, list)
    }

    @Test
    fun parseBooleanValueFromTemplate() = runTest {
        val astLexemes = """
            {{ true }}{{ false }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(2, list.size)
        assertEquals(
            listOf(
                AstLexeme.Boolean(true),
                AstLexeme.Boolean(false)
            ).toSet(), list.toSet()
        )
    }

    @Test
    fun parseAssignStringFromTemplate() = runTest {
        val astLexemes = """
            {{ let name = "World" }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.Assign(
                    left = AstLexeme.Variable("name"),
                    right = AstLexeme.String("World")
                )
            ),
            list
        )
    }

    @Test
    fun parseAssignVariableFromTemplate() = runTest {
        val astLexemes = """
            {{ let name = variable }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.Assign(
                    left = AstLexeme.Variable("name"),
                    right = AstLexeme.Variable("variable")
                )
            ),
            list
        )
    }

    @Test
    fun parseFunctionCallFromTemplate() = runTest {
        val astLexemes = """
            {{ function("first", 2) }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.FunctionCall(
                    identifier = AstLexeme.Variable("function"),
                    args = listOf(
                        AstLexeme.FunctionArgument(
                            name = null,
                            value = AstLexeme.String("first"),
                        ),
                        AstLexeme.FunctionArgument(
                            name = null,
                            value = AstLexeme.Number(2),
                        )
                    )
                )
            ),
            list
        )
    }

    @Test
    fun parseKeyAccessFromTemplate() = runTest {
        val astLexemes = """
            {{ object.key }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(listOf(AstLexeme.KeyAccess(obj = AstLexeme.Variable("object"), key = "key")), list)
    }

    @Test
    fun parseNestedKeyAccessFromTemplate() = runTest {
        val astLexemes = """
            {{ object.key1.key2 }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.KeyAccess(
                    obj = AstLexeme.KeyAccess(
                        obj = AstLexeme.Variable("object"),
                        key = "key1"
                    ),
                    key = "key2"
                )
            ), list
        )
    }

    @Test
    fun parseIndexAccessFromTemplate() = runTest {
        val astLexemes = """
            {{ object[0] }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.IndexAccess(
                    array = AstLexeme.Variable("object"),
                    index = 0
                )
            ), list
        )
    }

    @Test
    fun parseNestedIndexAccessFromTemplate() = runTest {
        val astLexemes = """
            {{ object[0][2] }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.IndexAccess(
                    array = AstLexeme.IndexAccess(
                        array = AstLexeme.Variable("object"),
                        index = 0
                    ),
                    index = 2
                )
            ), list
        )
    }

    @Test
    fun parseMixedAccessFromTemplate() = runTest {
        val astLexemes = """
            {{ object[0].key[0] }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.IndexAccess(
                    array = AstLexeme.KeyAccess(
                        obj = AstLexeme.IndexAccess(
                            array = AstLexeme.Variable("object"),
                            index = 0
                        ),
                        key = "key"
                    ),
                    index = 0
                )
            ), list
        )
    }

    @Test
    fun parseObjectMethodFromTemplate() = runTest {
        val astLexemes = """
            {{ object.key()(another.key) }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.FunctionCall(
                    identifier = AstLexeme.FunctionCall(
                        identifier = AstLexeme.KeyAccess(
                            obj = AstLexeme.Variable("object"),
                            key = "key"
                        ),
                        args = listOf()
                    ),
                    args = listOf(
                        AstLexeme.FunctionArgument(
                            name = null,
                            value = AstLexeme.KeyAccess(obj = AstLexeme.Variable(name = "another"), key = "key")
                        )
                    )
                )
            ), list
        )
    }

    @Test
    fun parseIfElseFromTemplate() = runTest {
        val astLexemes = """
            {{ if(variable()) }} true {{ else }} false {{ endif }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.Conditional(
                    condition = AstLexeme.FunctionCall(AstLexeme.Variable("variable"), listOf()),
                    then = listOf(AstLexeme.TemplateSource(" true ")),
                    another = listOf(AstLexeme.TemplateSource(" false "))
                )
            ), list
        )
    }

    @Test
    fun parseIfWithoutElseFromTemplate() = runTest {
        val astLexemes = """
            {{ if(variable) }} true {{ endif }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.Conditional(
                    condition = AstLexeme.Variable("variable"),
                    then = listOf(AstLexeme.TemplateSource(" true ")),
                    another = null
                )
            ), list
        )
    }

    @Test
    fun parseIteratorFromTemplate() = runTest {
        val astLexemes = """
            {{ for variable in array }}
            Value: {{ variable }}
            {{ endfor }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        assertEquals(
            listOf(
                AstLexeme.Iterator(
                    iterable = AstLexeme.Variable("array"),
                    item = AstLexeme.Variable("variable"),
                    body = listOf(
                        AstLexeme.TemplateSource("\nValue: "),
                        AstLexeme.Variable("variable"),
                        AstLexeme.TemplateSource("\n"),
                    )
                )
            ), list
        )
    }

    //TODO: Implement binary ops
//    @Test
//    fun parseSingleBinaryOperationFromTemplate() = runTest {
//        val astLexemes = """
//            {{ 1 + 2 }}
//        """.trimIndent().getAst()
//        val list = astLexemes.body.toList()
//
//        assertEquals(1, list.size)
//        assertEquals(
//            listOf(
//                AstLexeme.InfixOperation(
//                    operation = AstLexeme.Operation.PLUS,
//                    left = AstLexeme.Number(1),
//                    right = AstLexeme.Number(2)
//                )
//            ), list
//        )
//    }
//
//    @Test
//    fun parseBinaryOperationWithPresenceFromTemplate() = runTest {
//        val astLexemes = """
//            {{ 1 * 2 + 3 }}
//        """.trimIndent().getAst()
//        val list = astLexemes.body.toList()
//
//        assertEquals(1, list.size)
//        assertEquals(
//            listOf(
//                AstLexeme.InfixOperation(
//                    operation = AstLexeme.Operation.PLUS,
//                    left = AstLexeme.InfixOperation(
//                        operation = AstLexeme.Operation.MULTIPLY,
//                        left = AstLexeme.Number(1),
//                        right = AstLexeme.Number(2)
//                    ),
//                    right = AstLexeme.Number(3)
//                )
//            ), list
//        )
//    }
}