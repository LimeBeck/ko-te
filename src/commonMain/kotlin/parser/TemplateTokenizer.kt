package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.inputStream.*

interface TemplateTokenizer {
    fun analyze(stream: RewindableInputStream<Char>): List<TemplateToken>
}

data class LexerError(
    override val position: InputStream.Position,
    override val message: String
) : ParserError, Throwable(message)


class MustacheLikeTemplateTokenizer(
    private val delimiterStartSymbol: String = "{{",
    private val delimiterEndSymbol: String = "}}"
) : TemplateTokenizer {
    private fun RewindableInputStream<Char>.readLanguageParts(): List<TemplateToken.LanguagePart> {
        val startPosition = currentPosition.copy()

        skipNext(delimiterStartSymbol.asIterable().toList())
        skipEmpty()

        val languageConstructions = readUntil {
            !isNextSequenceEquals(delimiterEndSymbol.asIterable().toList())
        }.joinToString("")

        skipNext(delimiterEndSymbol.asIterable().toList())

        return listOf(
            TemplateToken.LanguagePart(
                text = languageConstructions.trimEnd(),
                startPosition = startPosition,
                endPosition = currentPosition
            )
        )
    }

    private fun RewindableInputStream<Char>.readRawTemplate(): TemplateToken.TemplateSource? {
        val startPosition = currentPosition.copy()

        val rawTemplate = readUntil {
            !isNextSequenceEquals(delimiterStartSymbol.asIterable().toList())
        }

        return if (rawTemplate.isNotEmpty())
            TemplateToken.TemplateSource(
                text = rawTemplate.joinToString(""),
                startPosition = startPosition,
                endPosition = currentPosition
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