package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.parser.LanguageToken

object IndexAccessParser : AstLexemeParser<AstLexeme.KeyAccess> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return recoverable(stream) {
            val canParseIdentifier = VariableParser.canParse(stream)
            if (canParseIdentifier && stream.hasNext()) {
                stream.next()
                return@recoverable canParseIndexAccess(stream)
            }
            return@recoverable false
        }
    }

    fun Number.isInteger() = this.toFloat() % this.toInt() == 0f

    fun canParseIndexAccess(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        val nextItem = stream.peek()
        val hasOpenBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "["

        if (hasOpenBracket) {
            stream.next()
            val next = stream.peek()
            if (next is LanguageToken.NumericValue && (next.value.isInteger())) {
                return@recoverable true
            }
        }
        return@recoverable false
    }

    fun parseNextIndexAccess(
        stream: RewindableInputStream<LanguageToken>,
        prevValue: AstLexeme.Value
    ): AstLexeme.IndexAccess {
        val nextItem = stream.peek()
        val hasOpenBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "["
        if (!hasOpenBracket)
            stream.throwErrorOnValue("punctuation '['")
        stream.skipNext(1)
        val index = stream.peek().also {
            if (it !is LanguageToken.NumericValue || !it.value.isInteger())
                stream.throwErrorOnValue("integer number")
        } as LanguageToken.NumericValue
        stream.next()
        val hasClosedBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "]"
        if (!hasClosedBracket)
            stream.throwErrorOnValue("punctuation ']'")
        return AstLexeme.IndexAccess(prevValue, index.value.toInt())
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.KeyAccess {
        if (!canParse(stream))
            stream.throwErrorOnValue("index access")
        val rootIdentifier = VariableParser.parse(stream)
        stream.next()
        var value = rootIdentifier as AstLexeme.Value
        while (stream.hasNext() && canParseIndexAccess(stream)) {
            value = parseNextIndexAccess(stream, value)
            stream.next()
        }
        if (value == rootIdentifier)
            stream.throwErrorOnValue("index access")
        return value as AstLexeme.KeyAccess
    }
}