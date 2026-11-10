package utils

import dev.limebeck.templateEngine.inputStream.SimplePosition
import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.TemplateToken
import dev.limebeck.templateEngine.parser.ast.AstRoot
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser

suspend fun String.parseAst(mockPositions: Boolean = false): AstRoot {
    val tokenizer = MustacheLikeTemplateTokenizer()
    val languageParser = MustacheLikeLanguageParser()
    val astParser = KoTeAstParser()

    val stream = this.toStream()
    val tokens = tokenizer.analyze(stream)
    val positionedTokens = if (mockPositions) {
        val position = SimplePosition.MOCK
        tokens.map { token ->
            when (token) {
                is TemplateToken.TemplateSource -> token.copy(startPosition = position, endPosition = position)
                is TemplateToken.LanguagePart -> token.copy(startPosition = position, endPosition = position)
            }
        }
    } else {
        tokens
    }
    val languageTokens = languageParser.parse(positionedTokens.asSequence())
    return astParser.parse(languageTokens).also {
        println(it.body.toList())
    }
}
