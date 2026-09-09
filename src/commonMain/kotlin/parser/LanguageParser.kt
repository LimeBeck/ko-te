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
        val KEYWORDS = listOf("if", "else", "endif", "for", "in", "endfor", "true", "false", "null", "let", "import")
        val PUNCTUATION = ",.;[]{}()".toList()
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
                val local = stream.currentPosition
                val base = token.startPosition as? CharInputStream.StringPosition
                val position = if (base == null) token.startPosition else CharInputStream.StringPosition(
                    base.absolutePosition + local.absolutePosition,
                    base.line + local.line - 1,
                    if (local.line == 1) base.column + local.column else local.column
                )
                val nextChar = stream.peek()
                when {
                    nextChar.isOperation() -> {
                        yield(LanguageToken.Operation(nextChar.toString(), startPosition = position))
                        stream.next()
                    }
                    nextChar.isPunctuation() -> {
                        yield(LanguageToken.Punctuation(nextChar.toString(), startPosition = position))
                        stream.next()
                    }
                    nextChar.isStringDef() -> {
                        yield(LanguageToken.StringValue(stream.readStringLiteral().value, startPosition = position))
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
                                        position = position,
                                        message = "<1ac7d0b6> Unexpected point inside number ${token.text}"
                                    )
                                }
                            }
                            it.isDigit()
                        }.joinToString("")
                        yield(
                            LanguageToken.NumericValue(
                                value = if (hasPointInside) {
                                    digit.toDoubleOrNull()?.takeIf { it.isFinite() }
                                        ?: throw LanguageError("Invalid number: $digit", position)
                                } else {
                                    digit.toIntOrNull() ?: digit.toLongOrNull()
                                        ?: throw LanguageError("Integer is outside Long range: $digit", position)
                                },
                                startPosition = position
                            )
                        )
                    }
                    nextChar.isLetter() -> {
                        val identifier = stream.readUntil {
                            !it.isWhitespace()
                                    && !it.isPunctuation()
                                    && !it.isOperation()
                                    && !it.isStringDef()
                        }.joinToString("")

                        if (identifier in KEYWORDS) {
                            yield(LanguageToken.Keyword(identifier, startPosition = position))
                        } else {
                            yield(
                                LanguageToken.Identifier(
                                    name = identifier,
                                    startPosition = position
                                )
                            )
                        }
                    }
                    else -> {
                        throw LanguageError(
                            position = position,
                            message = "<4bf66c83> Unexpected token '${token.text}'"
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
                            startPosition = token.startPosition
                        )
                    )
                    is TemplateToken.LanguagePart -> yieldAll(parseLanguageToken(token))
                }
            }
        }
    }
}