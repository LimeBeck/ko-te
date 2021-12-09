package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.parser.LanguageToken

object KeyAccessParser : AstLexemeParser<AstLexeme.KeyAccess> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return recoverable(stream) {
            val canParseIdentifier = VariableParser.canParse(stream)
            if (canParseIdentifier && stream.hasNext()) {
                stream.next()
                return@recoverable canParseDotAccess(stream)
            }
            return@recoverable false
        }
    }

    fun canParseDotAccess(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        val nextItem = stream.peek()
        val hasDot = nextItem is LanguageToken.Punctuation && nextItem.value == "."

        if (hasDot) {
            stream.next()
            val canParseKey = VariableParser.canParse(stream)
            return@recoverable canParseKey
        }
        return@recoverable false
    }

    fun parseNextDotAccess(
        stream: RewindableInputStream<LanguageToken>,
        prevValue: AstLexeme.Value
    ): AstLexeme.KeyAccess {
        val nextItem = stream.peek()
        val hasDot = nextItem is LanguageToken.Punctuation && nextItem.value == "."
        if (!hasDot)
            stream.throwErrorOnValue("punctuation '.'")
        stream.skipNext(1)
        val identifier = VariableParser.parse(stream)
        return AstLexeme.KeyAccess(prevValue, identifier.name)
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.KeyAccess {
        if(!canParse(stream))
            stream.throwErrorOnValue("key access")
        val rootIdentifier = VariableParser.parse(stream)
        stream.next()
        var value = rootIdentifier as AstLexeme.Value
        while (stream.hasNext() && canParseDotAccess(stream)){
            value = parseNextDotAccess(stream, value)
            stream.next()
        }
        if(value == rootIdentifier)
            stream.throwErrorOnValue("key access")
        return value as AstLexeme.KeyAccess
    }
}