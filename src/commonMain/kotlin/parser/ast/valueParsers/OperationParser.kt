package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.readUntil
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.Operation
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue
import dev.limebeck.templateEngine.utils.tryOrNull

object OperationParser : AstLexemeValueParser {
    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        if (!stream.hasNext())
            return@recoverable false

        val nextItem = stream.peek()
        val hasOperation = nextItem is LanguageToken.Operation
        if (hasOperation) {
            stream.readUntil { it is LanguageToken.Operation }
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

        val operation = stream.readUntil { it is LanguageToken.Operation }
            .joinToString("") { (it as LanguageToken.Operation).operation }.let {
                tryOrNull { Operation.find(it) }
            } ?: stream.throwErrorOnValue("operation")

        val right = ExpressionParser.parse(stream)
        if (right is AstLexeme.InfixOperation && right.operation.presence < operation.presence) {
            stream.seek(right.left.streamPosition.absolutePosition)
            return AstLexeme.InfixOperation(
                streamPosition = stream.currentPosition.copy(),
                left = prevExpression,
                right = right.left,
                operation = operation
            )
        }

        return AstLexeme.InfixOperation(
            streamPosition = stream.currentPosition.copy(),
            left = prevExpression,
            right = right,
            operation = operation
        )
    }
}