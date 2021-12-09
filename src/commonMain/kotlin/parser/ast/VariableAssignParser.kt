package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.skipNext
import dev.limebeck.templateEngine.parser.LanguageToken

object VariableAssignParser : AstLexemeParser<AstLexeme.Assign> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return stream.hasNext()
                && stream.peek() is LanguageToken.Keyword
                && (stream.peek() as LanguageToken.Keyword).name == "let"
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Assign {
        if (!canParse(stream)) {
            stream.throwErrorOnValue("'let' identifier")
        }

        stream.skipNext(1)
        val variable = IdentifierParser.parse(stream)
        stream.next()

        stream.skipNext(listOf("=")) { value, next ->
            next is LanguageToken.Operation && next.operation == value
        }

        val value = ValueParser.parse(stream)

        return AstLexeme.Assign(
            left = variable,
            right = value
        )
    }
}