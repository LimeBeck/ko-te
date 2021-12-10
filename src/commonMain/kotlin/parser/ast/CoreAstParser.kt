package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken

object CoreAstParser : AstLexemeParser<AstLexeme> {
    private val parsers = listOf(
        ConditionalBlockParser,
        IterableBlockParser,
        VariableAssignParser,
        ValueParser
    )

    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        if(!stream.hasNext()) return false
        val next = stream.peek()
        return parsers.any { it.canParse(stream) } || next is LanguageToken.TemplateSource
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme {
        val nextToken = stream.peek()
        return when {
            nextToken is LanguageToken.TemplateSource -> {
                AstLexeme.TemplateSource(nextToken.text)
            }
            ConditionalBlockParser.canParse(stream) -> {
                ConditionalBlockParser.parse(stream)
            }
            IterableBlockParser.canParse(stream) -> {
                IterableBlockParser.parse(stream)
            }
            VariableAssignParser.canParse(stream) -> {
                VariableAssignParser.parse(stream)
            }
            ValueParser.canParse(stream) -> {
                ValueParser.parse(stream)
            }
            else -> stream.throwErrorOnValue("language construction")
        }
    }
}