package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.InputStream
import dev.limebeck.templateEngine.inputStream.peekOrNull
import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.LexerError
import dev.limebeck.templateEngine.parser.TemplateLanguageTokens
import dev.limebeck.templateEngine.parser.isLanguagePartEnd

interface AstParser {
    fun parse(tokens: Sequence<LanguageToken>): AstRoot
}

fun InputStream<LanguageToken>.throwErrorOnValue(expected: String): Nothing {
    val next = peekOrNull()
    val actual = when {
        next == null -> "EOF"
        next.isLanguagePartEnd -> "end of template block"
        else -> next.toString()
    }
    throw LexerError(
        position = next?.startPosition ?: currentPosition,
        message = "<5665e901> Expected $expected, but got $actual"
    )
}

class KoTeAstParser : AstParser {
    override fun parse(tokens: Sequence<LanguageToken>): AstRoot {
        val stream = ((tokens as? TemplateLanguageTokens)?.withBlockEnds ?: tokens).toList().toStream()
        val body = stream.parseBlockBody()
        if (stream.hasNext()) stream.throwErrorOnValue("language construction")
        return AstRoot(body)
    }
}
