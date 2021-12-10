package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.readUntil
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.inputStream.toStream

object ConditionalBlockParser : AstLexemeParser<AstLexeme> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return stream.hasNext()
                && stream.peek() is LanguageToken.Keyword
                && (stream.peek() as LanguageToken.Keyword).name == "if"
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme {
        val nextToken = stream.peek()
        if (!canParse(stream))
            stream.throwErrorOnValue("conditional block")
        if (!(nextToken is LanguageToken.Keyword && nextToken.name == "if"))
            stream.throwErrorOnValue("'if' keyword")
        stream.skipNext(1)

        val possibleBracket = stream.peek()
        val hasOpenBracket = possibleBracket is LanguageToken.Punctuation && possibleBracket.value == "("
        if (!hasOpenBracket)
            stream.throwErrorOnValue("punctuation '('")

        stream.next()

        val condition = ValueParser.parse(stream)

        stream.next()

        val closedBracket = stream.peek()
        val hasClosedBracket = closedBracket is LanguageToken.Punctuation && closedBracket.value == ")"
        if (!hasClosedBracket)
            stream.throwErrorOnValue("punctuation ')'")
        stream.next()

        val thenValue = CoreAstParser.parse(stream)

        if (stream.hasNext()) {
            stream.next()
            val possibleElseOrEndif = stream.peek()
            if (possibleElseOrEndif !is LanguageToken.Keyword || possibleElseOrEndif.name !in listOf("endif", "else")) {
                stream.throwErrorOnValue("else or endif")
            }
            if (possibleElseOrEndif.name == "else") {
                stream.next()
                val elseValue = CoreAstParser.parse(stream)
                stream.skipNext(1)
                return AstLexeme.Conditional(
                    condition = condition,
                    then = listOf(thenValue),
                    another = listOf(elseValue)
                )
            }
            return AstLexeme.Conditional(
                condition = condition,
                then = listOf(thenValue),
                another = null
            )
        } else {
            stream.throwErrorOnValue("else or endif")
        }
    }
}