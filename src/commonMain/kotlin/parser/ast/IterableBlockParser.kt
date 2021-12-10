package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken

object IterableBlockParser : AstLexemeParser<AstLexeme> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return stream.hasNext()
                && stream.peek() is LanguageToken.Keyword
                && (stream.peek() as LanguageToken.Keyword).name == "for"
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme {
        val nextToken = stream.peek()
        if (!canParse(stream))
            stream.throwErrorOnValue("iterable block")
        if (!(nextToken is LanguageToken.Keyword && nextToken.name == "for"))
            stream.throwErrorOnValue("'for' keyword")

        stream.next()

        val possibleItem = stream.peek()
        if (possibleItem !is LanguageToken.Identifier)
            stream.throwErrorOnValue("identifier")
        val item = AstLexeme.Variable(possibleItem.name)

        stream.next()

        val possibleIn = stream.peek()
        if (possibleIn !is LanguageToken.Keyword || possibleIn.name != "in")
            stream.throwErrorOnValue("keyword 'in'")

        stream.next()

        val possibleIterable = stream.peek()
        if (possibleIterable !is LanguageToken.Identifier)
            stream.throwErrorOnValue("identifier")
        val iterable = AstLexeme.Variable(possibleIterable.name)

        stream.next()

        val body = mutableListOf<AstLexeme>(CoreAstParser.parse(stream))

        stream.next()

        while (stream.hasNext() && CoreAstParser.canParse(stream)) {
            val next = stream.peek()
            body.add(CoreAstParser.parse(stream))
            if(stream.hasNext())
                stream.next()
        }

        if (stream.hasNext()) {
            val possibleEndfor = stream.peek()
            if (possibleEndfor !is LanguageToken.Keyword || possibleEndfor.name != "endfor") {
                stream.throwErrorOnValue("endfor")
            }
            return AstLexeme.Iterator(
                item = item,
                iterable = iterable,
                body = body
            )
        } else {
            stream.throwErrorOnValue("endfor")
        }
    }
}