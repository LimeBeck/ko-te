import dev.limebeck.templateEngine.inputStream.SimplePosition
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.Operation
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
        assertEquals(listOf(AstLexeme.String(SimplePosition.MOCK, "World")), list)
    }

    @Test
    fun parseNumericValueFromTemplate() = runTest {
        val astLexemes = """
            {{ 123 }}{{ 123.456 }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(2, list.size)
        val expected = listOf(
            AstLexeme.Number(SimplePosition.MOCK, 123),
            AstLexeme.Number(SimplePosition.MOCK, 123.456F)
        )
        assertContentEquals(expected, list)
    }

    @Test
    fun parseOperationsValueFromTemplate() = runTest {
        val astLexemes = """
            {{ (123 + 123.456) * 2 }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        val expected = listOf(
            AstLexeme.InfixOperation(
                streamPosition = SimplePosition.MOCK,
                left = AstLexeme.InfixOperation(
                    streamPosition = SimplePosition.MOCK,
                    left = AstLexeme.Number(SimplePosition.MOCK, 123),
                    right = AstLexeme.Number(SimplePosition.MOCK, 123.456F),
                    operation = Operation.PLUS
                ),
                right = AstLexeme.Number(SimplePosition.MOCK, 2),
                operation = Operation.MULTIPLY
            )
        )
        assertContentEquals(expected, list)
    }

    @Test
    fun parseNestedOperationsValueFromTemplate() = runTest {
        val astLexemes = """
            {{ ((123 + 123.456) * 2) }}
        """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        assertEquals(1, list.size)
        val expected = listOf(
            AstLexeme.InfixOperation(
                streamPosition = SimplePosition.MOCK,
                left = AstLexeme.InfixOperation(
                    streamPosition = SimplePosition.MOCK,
                    left = AstLexeme.Number(SimplePosition.MOCK, 123),
                    right = AstLexeme.Number(SimplePosition.MOCK, 123.456F),
                    operation = Operation.PLUS
                ),
                right = AstLexeme.Number(SimplePosition.MOCK, 2),
                operation = Operation.MULTIPLY
            )
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
        assertContentEquals(
            listOf(
                AstLexeme.Boolean(SimplePosition.MOCK, true),
                AstLexeme.Boolean(SimplePosition.MOCK, false)
            ),
            list
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
                    SimplePosition.MOCK,
                    left = AstLexeme.Variable(SimplePosition.MOCK, "name"),
                    right = AstLexeme.String(SimplePosition.MOCK, "World")
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
                    SimplePosition.MOCK,
                    left = AstLexeme.Variable(SimplePosition.MOCK, "name"),
                    right = AstLexeme.Variable(SimplePosition.MOCK, "variable")
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
                    streamPosition = SimplePosition.MOCK,
                    identifier = AstLexeme.Variable(SimplePosition.MOCK, "function"),
                    args = listOf(
                        AstLexeme.FunctionArgument(
                            SimplePosition.MOCK,
                            name = null,
                            value = AstLexeme.String(SimplePosition.MOCK, "first"),
                        ),
                        AstLexeme.FunctionArgument(
                            SimplePosition.MOCK,
                            name = null,
                            value = AstLexeme.Number(SimplePosition.MOCK, 2),
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
        assertEquals(
            listOf(
                AstLexeme.KeyAccess(
                    SimplePosition.MOCK,
                    obj = AstLexeme.Variable(SimplePosition.MOCK, "object"),
                    key = "key"
                )
            ), list
        )
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
                    SimplePosition.MOCK,
                    obj = AstLexeme.KeyAccess(
                        SimplePosition.MOCK,
                        obj = AstLexeme.Variable(SimplePosition.MOCK, "object"),
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
                    SimplePosition.MOCK,
                    array = AstLexeme.Variable(SimplePosition.MOCK, "object"),
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
                    SimplePosition.MOCK,
                    array = AstLexeme.IndexAccess(
                        SimplePosition.MOCK,
                        array = AstLexeme.Variable(SimplePosition.MOCK, "object"),
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
                    SimplePosition.MOCK,
                    array = AstLexeme.KeyAccess(
                        SimplePosition.MOCK,
                        obj = AstLexeme.IndexAccess(
                            SimplePosition.MOCK,
                            array = AstLexeme.Variable(SimplePosition.MOCK, "object"),
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
                    SimplePosition.MOCK,
                    identifier = AstLexeme.FunctionCall(
                        SimplePosition.MOCK,
                        identifier = AstLexeme.KeyAccess(
                            SimplePosition.MOCK,
                            obj = AstLexeme.Variable(SimplePosition.MOCK, "object"),
                            key = "key"
                        ),
                        args = listOf()
                    ),
                    args = listOf(
                        AstLexeme.FunctionArgument(
                            SimplePosition.MOCK,
                            name = null,
                            value = AstLexeme.KeyAccess(
                                SimplePosition.MOCK,
                                obj = AstLexeme.Variable(SimplePosition.MOCK, name = "another"),
                                key = "key"
                            )
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
                    streamPosition = SimplePosition.MOCK,
                    condition = AstLexeme.FunctionCall(
                        streamPosition = SimplePosition.MOCK,
                        identifier = AstLexeme.Variable(SimplePosition.MOCK, "variable"),
                        args = listOf()
                    ),
                    then = listOf(AstLexeme.TemplateSource(SimplePosition.MOCK, " true ")),
                    another = listOf(AstLexeme.TemplateSource(SimplePosition.MOCK, " false "))
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
                    SimplePosition.MOCK,
                    condition = AstLexeme.Variable(SimplePosition.MOCK, "variable"),
                    then = listOf(AstLexeme.TemplateSource(SimplePosition.MOCK, " true ")),
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
                    SimplePosition.MOCK,
                    iterable = AstLexeme.Variable(SimplePosition.MOCK, "array"),
                    item = AstLexeme.Variable(SimplePosition.MOCK, "variable"),
                    body = listOf(
                        AstLexeme.TemplateSource(SimplePosition.MOCK, "\nValue: "),
                        AstLexeme.Variable(SimplePosition.MOCK, "variable"),
                        AstLexeme.TemplateSource(SimplePosition.MOCK, "\n"),
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