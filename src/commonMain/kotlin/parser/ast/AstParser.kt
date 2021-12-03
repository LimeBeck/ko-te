package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.InputStream
import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.peekOrNull
import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.LexerError

interface AstParser {
    fun parse(tokens: Sequence<LanguageToken>): AstRoot
}

fun InputStream<LanguageToken>.throwErrorOnValue(expected: String): Nothing {
    val next = peekOrNull()
    val isEof = !hasNext()
    throw LexerError(
        position = next?.startPosition ?: currentPosition,
        message = "<5665e901> Expected ${expected}, but got ${if (isEof) "EOF" else "$next"}"
    )
}

private fun parseAstTree(stream: RewindableInputStream<LanguageToken>): List<AstLexeme> {

    val astSequence = mutableListOf<AstLexeme>()

    while (stream.hasNext()) {
        astSequence.add(CoreAstParser.parse(stream))
        if (stream.hasNext())
            stream.next()
    }

    return astSequence
}


class KoTeAstParser : AstParser {
    override fun parse(tokens: Sequence<LanguageToken>): AstRoot {
        val stream = tokens.toList().toStream()
        val astSequence = parseAstTree(stream)
        return AstRoot(body = astSequence)
    }
}