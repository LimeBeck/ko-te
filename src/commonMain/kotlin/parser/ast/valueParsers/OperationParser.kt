package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.*
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.*

object OperationParser : AstLexemeValueParser {
    private fun readOperation(stream: RewindableInputStream<LanguageToken>): Operation? {
        val first = (if (stream.hasNext()) stream.peek() else null) as? LanguageToken.Operation ?: return null
        stream.next()
        if (first.operation != "=") return Operation.find(first.operation)
        val second = (if (stream.hasNext()) stream.peek() else null) as? LanguageToken.Operation
        if (second?.operation != "=") return null
        stream.next()
        return Operation.EQUALS
    }

    internal fun peekOperation(stream: RewindableInputStream<LanguageToken>): Operation? =
        recoverable(stream) { readOperation(stream) }

    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean =
        peekOperation(stream) != null

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevExpression: AstLexeme.Expression
    ): AstLexeme.InfixOperation {
        val operation = readOperation(stream) ?: stream.throwErrorOnValue("operation")
        val minimumPrecedence = operation.presence + if (operation.associativity == Associativity.LEFT) 1 else 0
        val right = ExpressionParser.parse(stream, minimumPrecedence)
        return AstLexeme.InfixOperation(stream.currentPosition.copy(), prevExpression, right, operation)
    }
}
