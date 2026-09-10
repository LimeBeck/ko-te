package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken

/** Leaves the closing keyword for the owning block parser, including for empty bodies. */
internal fun RewindableInputStream<LanguageToken>.parseBlockBody(): List<AstLexeme> {
    val body = mutableListOf<AstLexeme>()
    while (hasNext() && CoreAstParser.canParse(this)) {
        body += CoreAstParser.parse(this)
        if (hasNext()) next()
    }
    return body
}
