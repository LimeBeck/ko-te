package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.*

interface LanguageParser {
    suspend fun parse(tokens: Sequence<TemplateToken>): Sequence<LanguageToken>
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

    private fun parseLanguageToken(token: TemplateToken.LanguagePart): Sequence<LanguageToken> {
        val stream = token.text.toStream()

        stream.skipEmpty()

        return sequence {
            while (stream.hasNext()) {
                val nextChar = stream.peek()
                when {
                    nextChar.isOperation() -> {
                        yield(LanguageToken.Operation(nextChar.toString(), position = token.position))
                        stream.next()
                    }
                    nextChar.isPunctuation() -> {
                        yield(LanguageToken.Punctuation(nextChar.toString(), position = token.position))
                        stream.next()
                    }
                    KEYWORDS.any { stream.isNextSequenceEquals(it.toList() + ' ') } -> {
                        val keyword = stream.readUntil { !it.isWhitespace() }.joinToString("")
                        yield(LanguageToken.Keyword(keyword, position = token.position))
                    }
                    ALLOWED_STRING_DEFINITIONS.any { stream.isNextSequenceEquals(it.toList()) } -> {
                        stream.readUntil { it.isStringDef() }
                        val string = stream.readUntil { !it.isStringDef() }.joinToString("")
                        yield(LanguageToken.StringValue(string, position = token.position))
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
                        yield(
                            LanguageToken.NumericValue(
                                value = if (hasPointInside) digit.toFloat() else digit.toInt(),
                                position = token.position
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
                        yield(
                            LanguageToken.Identifier(
                                name = identifier.joinToString(""),
                                position = token.position
                            )
                        )
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
        }
    }

    override suspend fun parse(tokens: Sequence<TemplateToken>): Sequence<LanguageToken> {
        return sequence {
            tokens.forEach { token ->
                when (token) {
                    is TemplateToken.TemplateSource -> yield(
                        LanguageToken.TemplateSource(
                            token.text,
                            position = token.position
                        )
                    )
                    is TemplateToken.LanguagePart -> yieldAll(parseLanguageToken(token))
                }
            }
        }
    }
}