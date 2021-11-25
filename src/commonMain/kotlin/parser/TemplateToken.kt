package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.InputStream

sealed interface TemplateToken {
    val position: InputStream.Position

    data class TemplateSource(
        val text: String,
        override val position: InputStream.Position
    ) : TemplateToken

    data class LanguagePart(
        val text: String,
        override val position: InputStream.Position
    ) : TemplateToken
}