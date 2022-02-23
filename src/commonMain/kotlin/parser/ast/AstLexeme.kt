package dev.limebeck.templateEngine.parser.ast

sealed interface AstLexeme {

    interface Primitive : AstLexeme
    interface Expression : AstLexeme
    interface WritableExpression : Expression

    data class TemplateSource(val text: kotlin.String) : Expression

    data class Number(val value: kotlin.Number) : Expression, Primitive

    data class String(val value: kotlin.String) : Expression, Primitive

    data class Boolean(val value: kotlin.Boolean) : Expression, Primitive

    data class FunctionCall(
        val identifier: Expression,
        val args: List<FunctionArgument>
    ) : Expression

    data class FunctionArgument(
        val name: kotlin.String?,
        val value: AstLexeme
    ) : AstLexeme

    data class Variable(val name: kotlin.String) : WritableExpression

    data class Assign(val left: WritableExpression, val right: AstLexeme) : AstLexeme

    data class KeyAccess(val obj: Expression, val key: kotlin.String) : WritableExpression

    data class IndexAccess(val array: Expression, val index: Int) : WritableExpression

    data class InfixOperation(val left: AstLexeme, val right: AstLexeme, val operation: Operation) : Expression

    data class Conditional(
        val condition: AstLexeme,
        val then: List<AstLexeme>,
        val another: List<AstLexeme>?
    ) : AstLexeme

    data class Iterator(
        val iterable: Variable,
        val item: Variable,
        val body: List<AstLexeme>
    ) : AstLexeme

    data class Import(
        val path: String
    ) : AstLexeme
}

data class AstRoot(
    val body: List<AstLexeme>
)

enum class Associativity {
    RIGHT,
    LEFT
}

enum class Operation(val stringValue: kotlin.String, val presence: Int, val associativity: Associativity) {
    PLUS("+", 10, Associativity.RIGHT),
    MINUS("-", 10, Associativity.RIGHT),
    MULTIPLY("*", 20, Associativity.RIGHT),
    DIVIDE("/", 30, Associativity.RIGHT),
    PERCENT("%", 10, Associativity.RIGHT),
    EQUALS("==", 10, Associativity.RIGHT);

    companion object {
        fun find(value: String) = Operation.values()
            .find { it.stringValue == value }
    }
}