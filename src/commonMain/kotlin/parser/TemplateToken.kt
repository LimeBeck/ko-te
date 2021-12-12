package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.InputStream

sealed interface TemplateToken {
    val startPosition: InputStream.Position
    val endPosition: InputStream.Position

    data class TemplateSource(
        val text: String,
        override val startPosition: InputStream.Position,
        override val endPosition: InputStream.Position
    ) : TemplateToken

    data class LanguagePart(
        val text: String,
        override val startPosition: InputStream.Position,
        override val endPosition: InputStream.Position
    ) : TemplateToken
}