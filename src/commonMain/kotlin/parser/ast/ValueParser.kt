package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken

object ValueParser : AstLexemeParser<AstLexeme.Value> {
    private val valueParsers = listOf<AstLexemeParser<AstLexeme.Value>>(
        LiteralParser,
        KeyAccessParser as AstLexemeParser<AstLexeme.Value>,
        IndexAccessParser as AstLexemeParser<AstLexeme.Value>,
        VariableParser as AstLexemeParser<AstLexeme.Value>,
    )
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return valueParsers.any { it.canParse(stream) }
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Value {
        val parser = valueParsers.find { it.canParse(stream) }
        return parser?.parse(stream) ?: stream.throwErrorOnValue("value")
    }
}