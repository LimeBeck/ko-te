package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.InputStream

sealed interface AstLexeme {
    val streamPosition: InputStream.Position

    interface Primitive : AstLexeme
    interface Expression : AstLexeme
    interface WritableExpression : Expression

    data class TemplateSource(
        override val streamPosition: InputStream.Position,
        val text: kotlin.String
    ) : Expression

    data class Number(
        override val streamPosition: InputStream.Position,
        val value: kotlin.Number
    ) : Expression, Primitive

    data class String(
        override val streamPosition: InputStream.Position,
        val value: kotlin.String
    ) : Expression, Primitive

    data class Boolean(
        override val streamPosition: InputStream.Position,
        val value: kotlin.Boolean
    ) : Expression, Primitive

    data class FunctionCall(
        override val streamPosition: InputStream.Position,
        val identifier: Expression,
        val args: List<FunctionArgument>
    ) : Expression

    data class FunctionArgument(
        override val streamPosition: InputStream.Position,
        val name: kotlin.String?,
        val value: AstLexeme
    ) : AstLexeme

    data class Variable(
        override val streamPosition: InputStream.Position,
        val name: kotlin.String
    ) : WritableExpression

    data class Assign(
        override val streamPosition: InputStream.Position,
        val left: WritableExpression,
        val right: AstLexeme
    ) : AstLexeme

    data class KeyAccess(
        override val streamPosition: InputStream.Position,
        val obj: Expression,
        val key: kotlin.String
    ) : WritableExpression

    data class IndexAccess(
        override val streamPosition: InputStream.Position,
        val array: Expression,
        val index: Int
    ) :
        WritableExpression

    data class InfixOperation(
        override val streamPosition: InputStream.Position,
        val left: AstLexeme,
        val right: AstLexeme,
        val operation: Operation
    ) : Expression

    data class Conditional(
        override val streamPosition: InputStream.Position,
        val condition: AstLexeme,
        val then: List<AstLexeme>,
        val another: List<AstLexeme>?
    ) : AstLexeme

    data class Iterator(
        override val streamPosition: InputStream.Position,
        val iterable: Variable,
        val item: Variable,
        val body: List<AstLexeme>
    ) : AstLexeme

    data class Import(
        override val streamPosition: InputStream.Position, val path: String
    ) : AstLexeme
}

data class AstRoot(
    val body: List<AstLexeme>
)

enum class Associativity {
    RIGHT, LEFT
}

enum class Operation(
    val stringValue: kotlin.String,
    val presence: Int,
    val associativity: Associativity
) {
    PLUS("+", 10, Associativity.RIGHT),
    MINUS("-", 10, Associativity.RIGHT),
    MULTIPLY("*", 20, Associativity.RIGHT),
    DIVIDE("/", 30, Associativity.RIGHT),
    PERCENT("%", 10, Associativity.RIGHT),
    EQUALS("==", 10, Associativity.RIGHT);

    companion object {
        fun find(value: String) = Operation.values().find { it.stringValue == value }
    }
}