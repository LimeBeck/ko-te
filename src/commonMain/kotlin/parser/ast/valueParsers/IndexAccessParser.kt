package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.peekOrNull
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object IndexAccessParser : ComplexParser {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return recoverable(stream) {
            val canParseIdentifier = IdentifierParser.canParse(stream)
            if (canParseIdentifier && stream.hasNext()) {
                stream.next()
                return@recoverable canParseNext(stream)
            }
            return@recoverable false
        }
    }

    fun Number.isInteger() = this is Long ||
        (this is Byte || this is Short || this is Int) && toDouble() % 1.0 == 0.0

    private fun LanguageToken.NumericValue.isListIndex(): Boolean =
        (isIntegerLiteral ?: value.isInteger()) && value.toLong() in 0L..Int.MAX_VALUE.toLong()

    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        if(!stream.hasNext())
            return@recoverable false

        val nextItem = stream.peek()
        val hasOpenBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "["

        if (hasOpenBracket) {
            stream.next()
            val index = stream.peekOrNull()
            if (index is LanguageToken.NumericValue && index.isListIndex()) {
                stream.next()
                return@recoverable (stream.peekOrNull() as? LanguageToken.Punctuation)?.value == "]"
            }
        }
        return@recoverable false
    }

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevExpression: AstLexeme.Expression
    ): AstLexeme.IndexAccess {
        val nextItem = stream.peek()
        val hasOpenBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "["
        if (!hasOpenBracket)
            stream.throwErrorOnValue("punctuation '['")
        stream.skipNext(1)
        val index = stream.peekOrNull() as? LanguageToken.NumericValue
        if (index == null || !index.isListIndex()) stream.throwErrorOnValue("non-negative Int literal")
        stream.next()
        val closedBracket = stream.peekOrNull()
        val hasClosedBracket = closedBracket is LanguageToken.Punctuation && closedBracket.value == "]"
        if (!hasClosedBracket)
            stream.throwErrorOnValue("punctuation ']'")
        return AstLexeme.IndexAccess(stream.currentPosition.copy(), prevExpression, index.value.toInt())
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.IndexAccess {
        if (!canParse(stream))
            stream.throwErrorOnValue("index access")
        val rootIdentifier = IdentifierParser.parse(stream)
        stream.next()
        if(canParseNext(stream)) {
            return parseNext(stream, rootIdentifier)
        } else {
            stream.throwErrorOnValue("index access")
        }
    }
}
