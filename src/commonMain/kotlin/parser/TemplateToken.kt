package dev.limebeck.templateEngine.parser

sealed interface TemplateToken {
    data class TemplateSource(
        val text: String
    ) : TemplateToken

    data class LanguagePart(
        val text: String
    ) : TemplateToken
}