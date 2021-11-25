package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.*

interface LanguageParser {
    fun parse(tokens: List<TemplateToken>): List<LanguageToken>
}

data class LanguageError(
    override val message: String,
    override val position: InputStream.Position
) : ParserError, Throwable(message)

class MustacheLikeLanguageParser : LanguageParser {
    companion object {
        val KEYWORDS = listOf("if", "else", "for", "in", "true", "false", "null", "endif", "let")
        val PUNCTUATION = ",.;[]{}".toList()
        val OPERATIONS = "+-*/%!<>=&".toList()
        val ALLOWED_STRING_DEFINITIONS = listOf("\"", "\"\"\"", "'")
    }

    private fun Char.isPunctuation(): Boolean = this in PUNCTUATION
    private fun Char.isOperation(): Boolean = this in OPERATIONS
    private fun Char.isStringDef(): Boolean = this in listOf('"', '\'')

    private fun String.isString(): Boolean {
        return ALLOWED_STRING_DEFINITIONS.any { startsWith(it) }
    }

    private fun String.isIdentifier(): Boolean {
        if (length < 1) return false
        if (!this[0].isLetter()) return false
        return true
    }

    private fun parseLanguageToken(token: TemplateToken.LanguagePart): List<LanguageToken> {
        val stream = token.text.toStream()

        stream.skipEmpty()

        val tokens = mutableListOf<LanguageToken>()
        while (stream.hasNext()) {
            val nextChar = stream.peek()
            when {
                nextChar.isOperation() -> {
                    tokens.add(LanguageToken.Operation(nextChar.toString()))
                    stream.next()
                }
                nextChar.isPunctuation() -> {
                    tokens.add(LanguageToken.Punctuation(nextChar.toString()))
                    stream.next()
                }
                KEYWORDS.any { stream.isNextSequenceEquals(it.toList()) } -> {
                    val keyword = stream.readUntil { !it.isWhitespace() }.joinToString("")
                    tokens.add(LanguageToken.Keyword(keyword))
                }
                ALLOWED_STRING_DEFINITIONS.any { stream.isNextSequenceEquals(it.toList()) } -> {
                    stream.readUntil { it.isStringDef() }
                    val string = stream.readUntil { !it.isStringDef() }.joinToString("")
                    tokens.add(LanguageToken.StringValue(string))
                }
                nextChar.isDigit() -> {
                    var hasPointInside = false
                    val digit = stream.readUntil {
                        if (it == '.') {
                            if (!hasPointInside) {
                                hasPointInside = true
                                return@readUntil true
                            } else {
                                throw LanguageError(
                                    position = token.position,
                                    message = "<1ac7d0b6> Unexpected point inside number ${token.text}"
                                )
                            }
                        }
                        it.isDigit()
                    }.joinToString("")
                    tokens.add(
                        LanguageToken.NumericValue(
                            value = if (hasPointInside) digit.toFloat() else digit.toInt()
                        )
                    )
                }
                nextChar.isLetter() -> {
                    val identifier = stream.readUntil {
                        !it.isWhitespace()
                                && !it.isPunctuation()
                                && !it.isOperation()
                                && !it.isStringDef()
                    }
                    stream.debug()
                    tokens.add(LanguageToken.Identifier(identifier.joinToString("")))
                }
                else -> {
                    throw LanguageError(
                        position = token.position,
                        message = "<4bf66c83> Unexpected token ${token.text}"
                    )
                }
            }

            stream.skipEmpty()
        }
        return tokens
    }

    override fun parse(tokens: List<TemplateToken>): List<LanguageToken> {
        return tokens.flatMap { token ->
            when (token) {
                is TemplateToken.TemplateSource -> listOf(LanguageToken.TemplateSource(token.text))
                is TemplateToken.LanguagePart -> parseLanguageToken(token)
            }
        }
    }
}