package dev.limebeck.templateEngine.parser

import dev.limebeck.templateEngine.SourceFile
import dev.limebeck.templateEngine.inputStream.*

interface TemplateTokenizer {
    fun analyze(stream: RewindableInputStream<Char>): List<TemplateToken>
}

data class LexerError(
    override val position: InputStream.Position,
    override val message: String,
    override val sourceFile: SourceFile? = position.sourceFile
) : ParserError, Throwable(message)


class MustacheLikeTemplateTokenizer(
    private val delimiterStartSymbol: String = "{{",
    private val delimiterEndSymbol: String = "}}"
) : TemplateTokenizer {
    init {
        require(delimiterStartSymbol.isNotEmpty() && delimiterEndSymbol.isNotEmpty())
    }

    private val delimiterStart = delimiterStartSymbol.toList()
    private val delimiterEnd = delimiterEndSymbol.toList()

    private fun RewindableInputStream<Char>.readLanguageParts(file: SourceFile?): List<TemplateToken.LanguagePart> {
        skipNext(delimiterStart)
        skipEmpty()
        val startPosition = currentPosition.copy().withFile(file)
        val languageConstructions = buildString {
            while (hasNext() && !isNextSequenceEquals(delimiterEnd)) {
                if (peek() == '"' || peek() == '\'') append(readStringLiteral().source)
                else append(next())
            }
        }

        if (!hasNext()) throw LexerError(currentPosition.copy(), "Unterminated template block: expected $delimiterEndSymbol")
        skipNext(delimiterEnd)

        return listOf(
            TemplateToken.LanguagePart(
                text = languageConstructions.trimEnd(),
                startPosition = startPosition,
                endPosition = currentPosition.withFile(file)
            )
        )
    }

    private fun RewindableInputStream<Char>.readRawTemplate(file: SourceFile?): TemplateToken.TemplateSource? {
        val startPosition = currentPosition.copy().withFile(file)

        val rawTemplate = readUntil {
            !isNextSequenceEquals(delimiterStart)
        }

        return if (rawTemplate.isNotEmpty())
            TemplateToken.TemplateSource(
                text = rawTemplate.joinToString(""),
                startPosition = startPosition,
                endPosition = currentPosition.withFile(file)
            )
        else
            null
    }

    override fun analyze(stream: RewindableInputStream<Char>): List<TemplateToken> = analyze(stream, LexerContext())

    fun analyze(stream: RewindableInputStream<Char>, context: LexerContext): List<TemplateToken> =
        withSourceFile(context.file) {
            val tokens = mutableListOf<TemplateToken>()
            while (stream.hasNext()) {
                stream.readRawTemplate(context.file)?.let {
                    tokens.add(it)
                }

                if (stream.hasNext()) {
                    tokens.addAll(stream.readLanguageParts(context.file))
                }
            }
            tokens
        }
}
