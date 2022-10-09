package utils

import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.ast.AstRoot
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser

suspend fun String.parseAst(debug: Boolean = false): AstRoot {
    val tokenizer = MustacheLikeTemplateTokenizer()
    val languageParser = MustacheLikeLanguageParser()
    val astParser = KoTeAstParser()

    val stream = this.toStream()
    val tokens = tokenizer.analyze(stream)
    val languageTokens = languageParser.parse(tokens.asSequence())
    return astParser.parse(languageTokens).also {
        if (debug) println(it.body.toList())
    }
}