package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.UnaryOperation
import dev.limebeck.templateEngine.parser.ast.AstLexemeParser
import dev.limebeck.templateEngine.parser.ast.FunctionCallParser
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

interface AstLexemeValueParser {
    fun parseNext(stream: RewindableInputStream<LanguageToken>, prevExpression: AstLexeme.Expression): AstLexeme.Expression
    fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean
}

interface ComplexParser : AstLexemeParser<AstLexeme.Expression>, AstLexemeValueParser

object ExpressionParser : AstLexemeParser<AstLexeme.Expression> {
    private val partialParsers = listOf(FunctionCallParser, KeyAccessParser, IndexAccessParser)

    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean =
        prefixOperation(stream) != null || LiteralParser.canParse(stream) || IdentifierParser.canParse(stream) || GroupExpressionParser.canParse(stream)

    private fun prefixOperation(stream: RewindableInputStream<LanguageToken>): UnaryOperation? =
        if (stream.hasNext()) (stream.peek() as? LanguageToken.Operation)?.operation?.let(UnaryOperation::find)
        else null

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Expression = parse(stream, 0)

    // Like the other AST parsers, leave the stream on the expression's last token.
    internal fun parse(stream: RewindableInputStream<LanguageToken>, minimumPrecedence: Int): AstLexeme.Expression {
        val prefix = prefixOperation(stream)
        var value = when {
            prefix != null -> {
                val position = stream.peek().startPosition.copy()
                stream.next()
                AstLexeme.PrefixOperation(position, parse(stream, 30), prefix)
            }
            LiteralParser.canParse(stream) -> LiteralParser.parse(stream)
            IdentifierParser.canParse(stream) -> IdentifierParser.parse(stream)
            GroupExpressionParser.canParse(stream) -> GroupExpressionParser.parse(stream)
            else -> stream.throwErrorOnValue("value")
        }
        while (stream.hasNext()) {
            val endPosition = stream.currentPosition.absolutePosition
            stream.next()
            val suffix = partialParsers.find { it.canParseNext(stream) }
            if (suffix != null) {
                value = suffix.parseNext(stream, value)
                continue
            }
            val operation = OperationParser.peekOperation(stream)
            if (operation == null || operation.presence < minimumPrecedence) {
                stream.seek(endPosition)
                break
            }
            value = OperationParser.parseNext(stream, value)
        }
        return value
    }
}
