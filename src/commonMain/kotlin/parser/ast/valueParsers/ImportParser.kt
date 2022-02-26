package dev.limebeck.templateEngine.parser.ast.valueParsers

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.AstLexemeParser
import dev.limebeck.templateEngine.parser.ast.throwErrorOnValue

object ImportParser : AstLexemeParser<AstLexeme.Import> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        if (!stream.hasNext()) {
            return false
        }
        val next = stream.peek()
        return (next is LanguageToken.Keyword && next.name == "import")
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.Import {
        if (!canParse(stream)) {
            stream.throwErrorOnValue("Import")
        }

        val next = stream.peek() as LanguageToken.Keyword
        if (next.name != "import") {
            stream.throwErrorOnValue("Import keyword")
        }

        stream.next()

        if (!stream.hasNext()) {
            stream.throwErrorOnValue("Import path")
        }

        val path = stream.peek()
        if (path !is LanguageToken.StringValue) {
            stream.throwErrorOnValue("Import path")
        }

        return AstLexeme.Import(
            path = AstLexeme.String(path.startPosition.copy(), path.value),
            streamPosition = stream.currentPosition.copy()
        )
    }
}