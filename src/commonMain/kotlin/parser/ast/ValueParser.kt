package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken

interface AstLexemeValueParser : AstLexemeParser<AstLexeme.Value> {
    fun parseNext(stream: RewindableInputStream<LanguageToken>, prevValue: AstLexeme.Value): AstLexeme.Value
    fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean
}

object ValueParser : AstLexemeParser<AstLexeme.Value> {
    private val valueParsers = listOf<AstLexemeParser<AstLexeme.Value>>(
        LiteralParser,
        FunctionCallParser as AstLexemeParser<AstLexeme.Value>,
        KeyAccessParser as AstLexemeParser<AstLexeme.Value>,
        IndexAccessParser as AstLexemeParser<AstLexeme.Value>,
        IdentifierParser as AstLexemeParser<AstLexeme.Value>,
    )

    private val partialParsers = valueParsers.filterIsInstance<AstLexemeValueParser>()

    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return valueParsers.any { it.canParse(stream) }
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Value {
        var value = valueParsers.find { it.canParse(stream) }?.parse(stream)
            ?: stream.throwErrorOnValue("value")

        while (stream.hasNext()) {
            val rewindPoint = stream.currentPosition.absolutePosition
            stream.next()
            value = partialParsers.find { it.canParseNext(stream) }?.parseNext(stream, value)
                .also { it ?: stream.seek(rewindPoint) } ?: break
        }

        return value
    }
}