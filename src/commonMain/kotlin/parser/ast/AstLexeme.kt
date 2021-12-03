package dev.limebeck.templateEngine.parser.ast

sealed interface AstLexeme {

    data class TemplateSource(val text: kotlin.String) : AstLexeme

    interface Value : AstLexeme
    interface WritableValue : Value

    data class Number(val value: kotlin.Number) : Value

    data class String(val value: kotlin.String) : Value

    data class Boolean(val value: kotlin.Boolean) : Value

    data class FunctionCall(
        val name: kotlin.String,
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

    enum class BinaryOperations(val stringValue: kotlin.String) {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        PERCENT("%"),
        EQUALS("==")
    }

    data class BinaryOperation(val left: AstLexeme, val right: AstLexeme, val operation: BinaryOperations) : Value

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