package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.*
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.AstLexemeParser
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object GroupExpressionParser : AstLexemeParser<AstLexeme.Expression> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        if (!stream.hasNext())
            return@recoverable false

        if (stream.hasOpenBracket) {
            stream.next()
            if (stream.hasClosedBracket) {
                return@recoverable false
            }
            return@recoverable ExpressionParser.canParse(stream)
        }

        false
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Expression {
        if (!canParse(stream)) {
            stream.throwErrorOnValue("open bracket '('")
        }
        stream.next()

        if (stream.hasClosedBracket) {
            stream.throwErrorOnValue("unexpected closed bracket ')'")
        }
        val newStream = stream
            .readUntil { stream.hasNext() && !stream.hasClosedBracket }
            .toStream()

        return ExpressionParser.parse(newStream)
    }

    private val InputStream<LanguageToken>.hasClosedBracket: Boolean
        get() {
            if (!hasNext()) {
                return false
            }
            val nextItem = peek()
            return nextItem is LanguageToken.Punctuation && nextItem.value == ")"
        }

    private val InputStream<LanguageToken>.hasOpenBracket: Boolean
        get() {
            if (!hasNext()) {
                return false
            }
            val nextItem = peek()
            return nextItem is LanguageToken.Punctuation && nextItem.value == "("
        }
}