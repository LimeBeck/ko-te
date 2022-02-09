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
            return@recoverable ValueParser.canParse(stream)
        }
        return@recoverable false
    }

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevValue: AstLexeme.Value
    ): AstLexeme.InfixOperation {
        val nextItem = stream.peek()
        val hasOperation = nextItem is LanguageToken.Operation
        if (!hasOperation)
            stream.throwErrorOnValue("operation")

        val operation = Operation.find((nextItem as LanguageToken.Operation).operation)
            ?: stream.throwErrorOnValue("operation")

        stream.next()

        val right = ValueParser.parse(stream)

        if (prevValue is AstLexeme.InfixOperation && prevValue.operation.presence < operation.presence) {
            return AstLexeme.InfixOperation(right, prevValue, operation)
        }

        return AstLexeme.InfixOperation(prevValue, right, operation)
    }
}