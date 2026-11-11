package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.isLanguagePartEnd

/** Leaves the closing keyword for the owning block parser, including for empty bodies. */
internal fun RewindableInputStream<LanguageToken>.parseBlockBody(): List<AstLexeme> {
    val body = mutableListOf<AstLexeme>()
    while (hasNext()) {
        if (!peek().isLanguagePartEnd) {
            if (!CoreAstParser.canParse(this)) break
            body += CoreAstParser.parse(this)
        }
        if (hasNext()) next()
    }
    return body
}
