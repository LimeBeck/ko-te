package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.parser.LanguageToken

sealed interface AstLexeme {

    interface Value : AstLexeme
    interface WritableValue : Value

    data class TemplateSource(val text: kotlin.String) : Value

    data class Number(val value: kotlin.Number) : Value

    data class String(val value: kotlin.String) : Value

    data class Boolean(val value: kotlin.Boolean) : Value

    data class FunctionCall(
        val identifier: Value,
        val args: List<FunctionArgument>
    ) : Value

    data class FunctionArgument(
        val name: kotlin.String?,
        val value: AstLexeme
    ) : AstLexeme

    data class Variable(val name: kotlin.String) : WritableValue

    data class Assign(val left: WritableValue, val right: AstLexeme) : AstLexeme

    data class KeyAccess(val obj: Value, val key: kotlin.String) : WritableValue

    data class IndexAccess(val array: Value, val index: Int) : WritableValue

    data class InfixOperation(val left: AstLexeme, val right: AstLexeme, val operation: Operation) : Value

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
    MULTIPLY("*", 10, Associativity.RIGHT),
    DIVIDE("/", 10, Associativity.RIGHT),
    PERCENT("%", 10, Associativity.RIGHT),
    EQUALS("==", 10, Associativity.RIGHT)
}