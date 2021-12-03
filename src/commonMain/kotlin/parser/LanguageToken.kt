package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.InputStream


sealed interface LanguageToken {
    val position: InputStream.Position

    data class TemplateSource(
        val text: String,
        override val position: InputStream.Position
    ) : LanguageToken

    data class Identifier(
        val name: String,
        override val position: InputStream.Position
    ) : LanguageToken

    data class Keyword(
        val name: String,
        override val position: InputStream.Position
    ) : LanguageToken

    data class Punctuation(
        val value: String,
        override val position: InputStream.Position
    ) : LanguageToken

    data class Operation(
        val operation: String,
        override val position: InputStream.Position
    ) : LanguageToken

    data class StringValue(
        val value: String,
        override val position: InputStream.Position
    ) : LanguageToken

    data class NumericValue(
        val value: Number,
        override val position: InputStream.Position
    ) : LanguageToken

    data class Commentary(
        val comment: String,
        override val position: InputStream.Position
    ) : LanguageToken
}