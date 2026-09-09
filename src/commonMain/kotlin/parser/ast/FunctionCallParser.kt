package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.*
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.valueParsers.ComplexParser
import dev.limebeck.templateEngine.parser.ast.valueParsers.IdentifierParser
import dev.limebeck.templateEngine.parser.ast.valueParsers.ExpressionParser

object FunctionCallParser : ComplexParser {
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

    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean =
        stream.hasNext() && (stream.peek() as? LanguageToken.Punctuation)?.value == "("

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevExpression: AstLexeme.Expression
    ): AstLexeme.FunctionCall {
        if (!canParseNext(stream)) stream.throwErrorOnValue("'('")
        stream.next()
        val arguments = mutableListOf<AstLexeme.FunctionArgument>()
        if ((stream.peekOrNull() as? LanguageToken.Punctuation)?.value != ")") {
            while (true) {
                val position = stream.currentPosition.copy()
                val value = ExpressionParser.parse(stream)
                arguments.add(AstLexeme.FunctionArgument(position, null, value))
                stream.next()
                val separator = (stream.peekOrNull() as? LanguageToken.Punctuation)?.value
                if (separator == ")") break
                if (separator != ",") stream.throwErrorOnValue("',' or ')'")
                stream.next()
            }
        }
        if ((stream.peekOrNull() as? LanguageToken.Punctuation)?.value != ")") stream.throwErrorOnValue("')'")
        return AstLexeme.FunctionCall(stream.currentPosition.copy(), prevExpression, arguments)
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.FunctionCall {
        if (!canParse(stream))
            stream.throwErrorOnValue("function call")
        val rootIdentifier = IdentifierParser.parse(stream)
        stream.next()
        if (canParseNext(stream)) {
            return parseNext(stream, rootIdentifier)
        } else {
            stream.throwErrorOnValue("function call")
        }
    }
}