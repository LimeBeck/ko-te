package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.*

internal data class StringLiteral(val source: String, val value: String)

/** Shared by the template scanner and language lexer so quotes have identical boundaries. */
internal fun RewindableInputStream<Char>.readStringLiteral(): StringLiteral {
    val start = currentPosition.copy()
    fun invalid(message: String): Nothing = throw LanguageError(message, start)
    val source = StringBuilder()
    val value = StringBuilder()
    fun take(): Char {
        if (!hasNext()) invalid("Unterminated string literal")
        return next().also { source.append(it) }
    }
    val quote = take()
    val multiline = quote == '"' && isNextSequenceEquals(listOf('"', '"'))
    if (multiline) { take(); take() }
    while (hasNext()) {
        if (multiline && isNextSequenceEquals(listOf('"', '"', '"'))) {
            repeat(3) { take() }
            return StringLiteral(source.toString(), value.toString())
        }
        val char = take()
        if (!multiline && char == quote) return StringLiteral(source.toString(), value.toString())
        if (!multiline && (char == '\n' || char == '\r')) invalid("Use triple quotes for multiline strings")
        if (!multiline && char == '\\') {
            value.append(when (val escape = take()) {
                '\\', '\'', '"', '/' -> escape
                'n' -> '\n'
                'r' -> '\r'
                't' -> '\t'
                'b' -> '\b'
                'f' -> '\u000C'
                'u' -> {
                    val hex = buildString { repeat(4) { append(take()) } }
                    (hex.toIntOrNull(16) ?: invalid("Invalid Unicode escape")).toChar()
                }
                else -> invalid("Unknown string escape: $escape")
            })
        } else value.append(char)
    }
    invalid("Unterminated string literal")
}
