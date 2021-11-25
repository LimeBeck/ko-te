package dev.limebeck.templateEngine.parser


sealed interface LanguageToken {
    data class Identifier(
        val name: String
    ) : LanguageToken

    data class Keyword(
        val name: String
    ) : LanguageToken

    data class Operation(
        val operation: String
    ) : LanguageToken

    data class StringValue(
        val value: String
    ) : LanguageToken

    data class NumericValue(
        val value: Number
    ) : LanguageToken

    data class Commentary(
        val comment: String
    ) : LanguageToken
}