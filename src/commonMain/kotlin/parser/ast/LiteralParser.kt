package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken

object LiteralParser : AstLexemeParser<AstLexeme.Value> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return stream.hasNext() && (
                stream.peek() is LanguageToken.NumericValue
                        || stream.peek() is LanguageToken.StringValue
                        || (stream.peek() is LanguageToken.Keyword
                        && (stream.peek() as LanguageToken.Keyword).name.lowercase() in listOf("true", "false")
                        )
                )
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Value {
        if (!canParse(stream)) {
            stream.throwErrorOnValue("literal identifier")
        }
        return when (val next = stream.peek()) {
            is LanguageToken.StringValue -> AstLexeme.String(next.value)
            is LanguageToken.NumericValue -> AstLexeme.Number(next.value)
            is LanguageToken.Keyword -> {
                when (next.name.lowercase()) {
                    "true" -> AstLexeme.Boolean(true)
                    "false" -> AstLexeme.Boolean(false)
                    else -> stream.throwErrorOnValue("boolean value")
                }
            }
            else -> TODO()
        }
    }
}