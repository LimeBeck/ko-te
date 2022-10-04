package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.AstLexemeParser
import dev.limebeck.templateEngine.parser.ast.FunctionCallParser
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

interface AstLexemeValueParser {
    fun parseNext(stream: RewindableInputStream<LanguageToken>, prevExpression: AstLexeme.Expression): AstLexeme.Expression
    fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean
}

interface ComplexParser : AstLexemeParser<AstLexeme.Expression>, AstLexemeValueParser

object ExpressionParser : AstLexemeParser<AstLexeme.Expression> {
    private val expressionParsers = listOf<AstLexemeParser<AstLexeme.Expression>>(
        LiteralParser,
        FunctionCallParser as AstLexemeParser<AstLexeme.Expression>,
        KeyAccessParser as AstLexemeParser<AstLexeme.Expression>,
        IndexAccessParser as AstLexemeParser<AstLexeme.Expression>,
        IdentifierParser as AstLexemeParser<AstLexeme.Expression>,
        GroupExpressionParser as AstLexemeParser<AstLexeme.Expression>
    )

    private val partialParsers = expressionParsers.filterIsInstance<AstLexemeValueParser>() + listOf(
        OperationParser as AstLexemeValueParser
    )

    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return expressionParsers.any { it.canParse(stream) }
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Expression {
        var value = expressionParsers.find { it.canParse(stream) }?.parse(stream)
            ?: stream.throwErrorOnValue("value")

        while (stream.hasNext()) {
            val rewindPoint = stream.currentPosition.absolutePosition
            stream.next()
            value = partialParsers
                .find { it.canParseNext(stream) }
                ?.parseNext(stream, value)
                .also { it ?: stream.seek(rewindPoint) }
                ?: break
        }

        return value
    }
}