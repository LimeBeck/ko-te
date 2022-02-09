package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.AstLexemeParser
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object IdentifierParser : AstLexemeParser<AstLexeme.Variable> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return (stream.hasNext() && stream.peek() is LanguageToken.Identifier)
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Variable {
        if (!canParse(stream)) {
            stream.throwErrorOnValue("Identifier")
        }
        val next = stream.peek() as LanguageToken.Identifier

        return AstLexeme.Variable(
            name = next.name
        )
    }
}