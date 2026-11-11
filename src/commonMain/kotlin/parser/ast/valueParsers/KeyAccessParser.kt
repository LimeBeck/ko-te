package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.peekOrNull
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object KeyAccessParser : ComplexParser {
    private val LanguageToken?.fieldName: String?
        get() = when (this) {
            is LanguageToken.Identifier -> name
            is LanguageToken.Keyword -> name
            else -> null
        }

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
            return@recoverable stream.peekOrNull().fieldName != null
        }
        return@recoverable false
    }

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevExpression: AstLexeme.Expression
    ): AstLexeme.KeyAccess {
        val nextItem = stream.peek()
        val hasDot = nextItem is LanguageToken.Punctuation && nextItem.value == "."
        if (!hasDot)
            stream.throwErrorOnValue("punctuation '.'")
        stream.skipNext(1)
        val fieldName = stream.peekOrNull().fieldName ?: stream.throwErrorOnValue("field name")
        return AstLexeme.KeyAccess(nextItem.startPosition.copy(), prevExpression, fieldName)
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
