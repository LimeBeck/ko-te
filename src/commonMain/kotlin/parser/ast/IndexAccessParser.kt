package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.parser.LanguageToken

object IndexAccessParser : AstLexemeValueParser {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return recoverable(stream) {
            val canParseIdentifier = VariableParser.canParse(stream)
            if (canParseIdentifier && stream.hasNext()) {
                stream.next()
                return@recoverable canParseNext(stream)
            }
            return@recoverable false
        }
    }

    fun Number.isInteger() = !this.toString().contains(".")

    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        val nextItem = stream.peek()
        val hasOpenBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "["

        if (hasOpenBracket) {
            stream.next()
            val index = stream.peek()
            if (index is LanguageToken.NumericValue && index.value.isInteger()) {
                stream.next()
                val closedBracket = stream.peek()
                val hasClosedBracket = closedBracket is LanguageToken.Punctuation && closedBracket.value == "]" 
                
                return@recoverable hasClosedBracket
            }
        }
        return@recoverable false
    }

    override fun parseNext(
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
        val closedBracket = stream.peek()
        val hasClosedBracket = closedBracket is LanguageToken.Punctuation && closedBracket.value == "]"
        if (!hasClosedBracket)
            stream.throwErrorOnValue("punctuation ']'")
        return AstLexeme.IndexAccess(prevValue, index.value.toInt())
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.IndexAccess {
        if (!canParse(stream))
            stream.throwErrorOnValue("index access")
        val rootIdentifier = VariableParser.parse(stream)
        stream.next()
        var value = rootIdentifier as AstLexeme.Value
        while (stream.hasNext()) {
            when {
                canParseNext(stream) -> value = parseNext(stream, value)
                KeyAccessParser.canParseNext(stream) ->  value = KeyAccessParser.parseNext(stream, value)
                else -> break
            }
           
            stream.next()
        }
        if (value == rootIdentifier)
            stream.throwErrorOnValue("index access")
        return value as AstLexeme.IndexAccess
    }
}