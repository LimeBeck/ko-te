package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.*

interface TemplateTokenizer {
    fun analyze(stream: RewindableInputStream<Char>): List<TemplateToken>
}

data class LexerError(
    val path: String,
    val col: Int?,
    val row: Int?,
    override val message: String
) : ParserError, Throwable(message)


class MustacheLikeTemplateTokenizer(
    private val delimiterStartSymbol: String = "{{",
    private val delimiterEndSymbol: String = "}}"
) : TemplateTokenizer {
    companion object {
        val KEYWORDS = listOf("if", "else", "for", "in", "true", "false", "null", "endif")
    }

    private fun RewindableInputStream<Char>.readLanguageParts(): List<TemplateToken.LanguagePart> {
        skipNext(delimiterStartSymbol.asIterable().toList())
        skipEmpty()

        val languageConstructions = readUntil {
            !isNextSequenceEquals(delimiterEndSymbol.asIterable().toList())
        }.joinToString("")

        skipNext(delimiterEndSymbol.asIterable().toList())

        return listOf(TemplateToken.LanguagePart(languageConstructions.trimEnd()))
    }

    private fun RewindableInputStream<Char>.readRawTemplate(): TemplateToken.TemplateSource? {
        val rawTemplate = readUntil {
            !isNextSequenceEquals(delimiterStartSymbol.asIterable().toList())
        }

        return if (rawTemplate.isNotEmpty())
            TemplateToken.TemplateSource(rawTemplate.joinToString(""))
        else
            null
    }

    override fun analyze(stream: RewindableInputStream<Char>): List<TemplateToken> {
        val tokens = mutableListOf<TemplateToken>()
        while (stream.hasNext()) {
            stream.readRawTemplate()?.let {
                tokens.add(it)
            }

            (stream as? CharInputStream)?.debug()

            if (stream.hasNext()) {
                stream.readLanguageParts().let {
                    tokens.addAll(it)
                }
            }
        }
        return tokens
    }
}