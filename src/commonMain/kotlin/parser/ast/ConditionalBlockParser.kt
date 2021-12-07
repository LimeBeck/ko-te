package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.inputStream.skipNext

object ConditionalBlockParser : AstLexemeParser<AstLexeme> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return stream.hasNext() 
                    && stream.peek() is LanguageToken.Keyword 
                    && (stream.peek() as LanguageToken.Keyword).name == "if"
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme {
        val nextToken = stream.peek()
        if(!canParse(stream))
            throw stream.throwErrorOnValue("conditional block")
        stream.skipNext(1)
        TODO("ConditionalBlockParser")
    }
}