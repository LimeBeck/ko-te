package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.peekOrNull
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.valueParsers.IdentifierParser

internal object FunctionDeclarationParser : AstLexemeParser<AstLexeme.FunctionDeclaration> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean =
        (stream.peekOrNull() as? LanguageToken.Keyword)?.name == "fun"

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.FunctionDeclaration {
        if (!canParse(stream)) stream.throwErrorOnValue("'fun' keyword")
        val position = stream.next().startPosition.copy()
        val name = IdentifierParser.parse(stream).name
        stream.next()
        if (stream.punctuation != "(") stream.throwErrorOnValue("'('")
        stream.next()

        val parameters = linkedSetOf<String>()
        if (stream.punctuation != ")") {
            while (true) {
                val parameterName = IdentifierParser.parse(stream).name
                if (!parameters.add(parameterName)) stream.throwErrorOnValue("a unique parameter name")
                stream.next()
                val separator = stream.punctuation
                if (separator == ")") break
                if (separator != ",") stream.throwErrorOnValue("',' or ')'")
                stream.next()
            }
        }

        stream.next()
        val body = stream.parseBlockBody()
        if ((stream.peekOrNull() as? LanguageToken.Keyword)?.name != "endfun") {
            stream.throwErrorOnValue("'endfun' keyword")
        }
        return AstLexeme.FunctionDeclaration(position, name, parameters.toList(), body)
    }

    private val RewindableInputStream<LanguageToken>.punctuation: String?
        get() = (peekOrNull() as? LanguageToken.Punctuation)?.value
}
