package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object KeyAccessParser : ComplexParser {
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

    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        if(!stream.hasNext())
            return@recoverable false

        val nextItem = stream.peek()
        val hasDot = nextItem is LanguageToken.Punctuation && nextItem.value == "."

        if (hasDot) {
            stream.next()
            val canParseKey = IdentifierParser.canParse(stream)
            return@recoverable canParseKey
        }
        return@recoverable false
    }

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevValue: AstLexeme.Value
    ): AstLexeme.KeyAccess {
        val nextItem = stream.peek()
        val hasDot = nextItem is LanguageToken.Punctuation && nextItem.value == "."
        if (!hasDot)
            stream.throwErrorOnValue("punctuation '.'")
        stream.skipNext(1)
        val identifier = IdentifierParser.parse(stream)
        return AstLexeme.KeyAccess(prevValue, identifier.name)
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.KeyAccess {
        if (!canParse(stream))
            stream.throwErrorOnValue("key access")
        val rootIdentifier = IdentifierParser.parse(stream)
        stream.next()
        if(canParseNext(stream)) {
            return parseNext(stream, rootIdentifier)
        } else {
            stream.throwErrorOnValue("key access")
        }
    }
}