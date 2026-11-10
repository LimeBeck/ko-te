package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.AstLexemeParser
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object LiteralParser : AstLexemeParser<AstLexeme.Expression> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        if (!stream.hasNext()) return false
        return when (val token = stream.peek()) {
            is LanguageToken.NumericValue, is LanguageToken.StringValue -> true
            is LanguageToken.Keyword -> token.name.lowercase() in setOf("true", "false", "null")
            else -> false
        }
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Expression {
        if (!canParse(stream)) stream.throwErrorOnValue("literal")
        val token = stream.peek()
        val position = token.startPosition.copy()
        return when (token) {
            is LanguageToken.StringValue -> AstLexeme.String(position, token.value)
            is LanguageToken.NumericValue -> AstLexeme.Number(position, token.value)
            is LanguageToken.Keyword -> when (token.name.lowercase()) {
                "true" -> AstLexeme.Boolean(position, true)
                "false" -> AstLexeme.Boolean(position, false)
                "null" -> AstLexeme.Null(position)
                else -> stream.throwErrorOnValue("literal")
            }
            else -> stream.throwErrorOnValue("literal")
        }
    }
}
