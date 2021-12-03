package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.parser.LanguageToken

interface AstLexemeParser<T : AstLexeme> {
    fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean
    fun parse(stream: RewindableInputStream<LanguageToken>): T
}