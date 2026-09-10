package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.valueParsers.ExpressionParser
import dev.limebeck.templateEngine.parser.ast.valueParsers.ImportParser

object CoreAstParser : AstLexemeParser<AstLexeme> {
    private val parsers = listOf(
        ImportParser,
        ConditionalBlockParser,
        IterableBlockParser,
        VariableAssignParser,
        ExpressionParser
    )

    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        if(!stream.hasNext()) return false
        val next = stream.peek()
        return parsers.any { it.canParse(stream) } || next is LanguageToken.TemplateSource
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme {
        val nextToken = stream.peek()
        if (nextToken is LanguageToken.TemplateSource) {
            return AstLexeme.TemplateSource(stream.currentPosition.copy(), nextToken.text)
        }
        return parsers.firstOrNull { it.canParse(stream) }?.parse(stream)
            ?: stream.throwErrorOnValue("language construction")
    }
}
