package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.*

interface TemplateTokenizer {
    fun analyze(stream: RewindableInputStream<Char>): List<TemplateToken>
}

data class LexerError(
    override val position: InputStream.Position,
    val col: Int?,
    val row: Int?,
    override val message: String
) : ParserError, Throwable(message)


class MustacheLikeTemplateTokenizer(
    private val delimiterStartSymbol: String = "{{",
    private val delimiterEndSymbol: String = "}}"
) : TemplateTokenizer {
    private fun RewindableInputStream<Char>.readLanguageParts(): List<TemplateToken.LanguagePart> {
        skipNext(delimiterStartSymbol.asIterable().toList())
        skipEmpty()

        val languageConstructions = readUntil {
            !isNextSequenceEquals(delimiterEndSymbol.asIterable().toList())
        }.joinToString("")

        skipNext(delimiterEndSymbol.asIterable().toList())

        return listOf(
            TemplateToken.LanguagePart(
                text = languageConstructions.trimEnd(),
                position = currentPosition
            )
        )
    }

    private fun RewindableInputStream<Char>.readRawTemplate(): TemplateToken.TemplateSource? {
        val rawTemplate = readUntil {
            !isNextSequenceEquals(delimiterStartSymbol.asIterable().toList())
        }

        return if (rawTemplate.isNotEmpty())
            TemplateToken.TemplateSource(
                text = rawTemplate.joinToString(""),
                position = currentPosition
            )
        else
            null
    }

    override fun analyze(stream: RewindableInputStream<Char>): List<TemplateToken> {
        val tokens = mutableListOf<TemplateToken>()
        while (stream.hasNext()) {
            stream.readRawTemplate()?.let {
                tokens.add(it)
            }

//            (stream as? CharInputStream)?.debug()

            if (stream.hasNext()) {
                stream.readLanguageParts().let {
                    tokens.addAll(it)
                }
            }
        }
        return tokens
    }
}