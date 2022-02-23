package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.Operation
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object OperationParser : AstLexemeValueParser {
    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        if (!stream.hasNext())
            return@recoverable false

        val nextItem = stream.peek()
        val hasOperation = nextItem is LanguageToken.Operation
        if (hasOperation) {
            stream.next()
            return@recoverable ExpressionParser.canParse(stream)
        }
        return@recoverable false
    }

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevExpression: AstLexeme.Expression
    ): AstLexeme.InfixOperation {
        val nextItem = stream.peek()
        val hasOperation = nextItem is LanguageToken.Operation
        if (!hasOperation)
            stream.throwErrorOnValue("operation")

        val operation = Operation.find((nextItem as LanguageToken.Operation).operation)
            ?: stream.throwErrorOnValue("operation")

        stream.next()
        val savepoint = stream.currentPosition.absolutePosition

        val right = ExpressionParser.parse(stream)

        if (right is AstLexeme.InfixOperation && right.operation.presence < operation.presence) {
            stream.seek(savepoint) //TODO: Take right.left position here
            return AstLexeme.InfixOperation(prevExpression, right.left, operation)
        }

        return AstLexeme.InfixOperation(prevExpression, right, operation)
    }
}