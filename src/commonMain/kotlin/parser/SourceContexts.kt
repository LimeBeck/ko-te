package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.SourceFile

data class LexerContext(val file: SourceFile? = null)

data class ParserContext(val file: SourceFile? = null)
