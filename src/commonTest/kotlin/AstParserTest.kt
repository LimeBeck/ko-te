import dev.limebeck.templateEngine.inputStream.SimplePosition
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.Operation
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import utils.parseAst

class AstParserTest : FunSpec({
    test("Simple templates: Variable in template") {
        val astLexemes = """
                Hello, {{ name }}!
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.TemplateSource(SimplePosition.MOCK, "Hello, "),
            AstLexeme.Variable(SimplePosition.MOCK, "name"),
            AstLexeme.TemplateSource(SimplePosition.MOCK, "!"),
        )
    }

    test("Simple templates: Single string value") {
        val astLexemes = """
                {{ "World" }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.String(SimplePosition.MOCK, "World")
        )
    }

    test("Simple templates: Numeric values") {
        val astLexemes = """
            {{ 123 }}{{ 123.456 }}
        """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.Number(SimplePosition.MOCK, 123),
            AstLexeme.Number(SimplePosition.MOCK, 123.456F)
        )
    }

    test("Operations: Single infix operation") {
        val astLexemes = """
                    {{ 1 + 2 }}
                """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.InfixOperation(
                streamPosition = SimplePosition.MOCK,
                left = AstLexeme.Number(SimplePosition.MOCK, 1),
                right = AstLexeme.Number(SimplePosition.MOCK, 2),
                operation = Operation.PLUS
            )
        )
    }

    test("Operations: Precedence infix operations") {
        val astLexemes = """
                    {{ 1 * 2 + 3 }}
                """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.InfixOperation(
                streamPosition = SimplePosition.MOCK,
                left = AstLexeme.InfixOperation(
                    streamPosition = SimplePosition.MOCK,
                    left = AstLexeme.Number(SimplePosition.MOCK, 1),
                    right = AstLexeme.Number(SimplePosition.MOCK, 2),
                    operation = Operation.MULTIPLY
                ),
                right = AstLexeme.Number(SimplePosition.MOCK, 3),
                operation = Operation.PLUS
            )
        )
    }

    test("Operations: Precedence changed infix operations") {
        val astLexemes = """
                    {{ 1 + 2 * 3 }}
                """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.InfixOperation(
                streamPosition = SimplePosition.MOCK,
                left = AstLexeme.Number(SimplePosition.MOCK, 1),
                right = AstLexeme.InfixOperation(
                    streamPosition = SimplePosition.MOCK,
                    left = AstLexeme.Number(SimplePosition.MOCK, 2),
                    right = AstLexeme.Number(SimplePosition.MOCK, 3),
                    operation = Operation.MULTIPLY
                ),
                operation = Operation.PLUS
            )
        )
    }

    test("Simple templates: Boolean value") {
        val astLexemes = """
                {{ true }}{{ false }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.Boolean(SimplePosition.MOCK, true),
            AstLexeme.Boolean(SimplePosition.MOCK, false)
        )
    }

    test("Simple templates: Assign string to variable") {
        val astLexemes = """
                {{ let name = "World" }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.Assign(
                SimplePosition.MOCK,
                left = AstLexeme.Variable(SimplePosition.MOCK, "name"),
                right = AstLexeme.String(SimplePosition.MOCK, "World")
            )
        )
    }

    test("Simple templates: Assign variable to another variable") {
        val astLexemes = """
                {{ let name = variable }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.Assign(
                SimplePosition.MOCK,
                left = AstLexeme.Variable(SimplePosition.MOCK, "name"),
                right = AstLexeme.Variable(SimplePosition.MOCK, "variable")
            )
        )
    }

    test("Simple templates: Function call") {
        val astLexemes = """
                {{ function("first", 2) }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
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
        )
    }

    test("Simple templates: Key access") {
        val astLexemes = """
                {{ object.key }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.KeyAccess(
                SimplePosition.MOCK,
                obj = AstLexeme.Variable(SimplePosition.MOCK, "object"),
                key = "key"
            )
        )
    }

    test("Simple templates: Nested key access") {
        val astLexemes = """
                {{ object.key1.key2 }}
            """.trimIndent().parseAst()
        val list = astLexemes.body.toList()

        list shouldBe listOf(
            AstLexeme.KeyAccess(
                SimplePosition.MOCK,
                obj = AstLexeme.KeyAccess(
                    SimplePosition.MOCK,
                    obj = AstLexeme.Variable(SimplePosition.MOCK, "object"),
                    key = "key1"
                ),
                key = "key2"
            )
        )
    }

    test("Simple templates: Index access") {
        val astLexemes = """
                {{ object[0] }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.IndexAccess(
                SimplePosition.MOCK,
                array = AstLexeme.Variable(SimplePosition.MOCK, "object"),
                index = 0
            )
        )
    }

    test("Simple templates: Nested index access") {
        val astLexemes = """
                {{ object[0][2] }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.IndexAccess(
                SimplePosition.MOCK,
                array = AstLexeme.IndexAccess(
                    SimplePosition.MOCK,
                    array = AstLexeme.Variable(SimplePosition.MOCK, "object"),
                    index = 0
                ),
                index = 2
            )
        )
    }

    test("More complex examples: Operations with changed precedence") {
        val astLexemes = """
                {{ (123 + 123.456) * 2 }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
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
    }

    test("More complex examples: Mixed access") {
        val astLexemes = """
                {{ object[0].key[0] }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
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
        )
    }

    test("More complex examples: Object method") {
        val astLexemes = """
                {{ object.key()(another.key) }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
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
        )
    }

    test("Flow control operators: If-Else full") {
        val astLexemes = """
                {{ if(variable()) }} true {{ else }} false {{ endif }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
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
        )
    }

    test("Flow control operators: If without else") {
        val astLexemes = """
                {{ if(variable) }} true {{ endif }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
            AstLexeme.Conditional(
                SimplePosition.MOCK,
                condition = AstLexeme.Variable(SimplePosition.MOCK, "variable"),
                then = listOf(AstLexeme.TemplateSource(SimplePosition.MOCK, " true ")),
                another = null
            )
        )
    }

    test("Flow control operators: Iterator (for _ in _)") {
        val astLexemes = """
                {{ for variable in array }}
                Value: {{ variable }}
                {{ endfor }}
            """.trimIndent().parseAst().body

        astLexemes shouldBe listOf(
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
        )
    }
})