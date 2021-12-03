package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.RewindableInputStream
import dev.limebeck.templateEngine.inputStream.recoverable
import dev.limebeck.templateEngine.parser.LanguageToken

object KeyAccessParser : AstLexemeParser<AstLexeme.KeyAccess> {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return recoverable(stream) {
            val canParseIdentifier = VariableParser.canParse(stream)
            if (canParseIdentifier && stream.hasNext()) {
                stream.next()
                val nextItem = stream.peek()
                val hasDot = nextItem is LanguageToken.Punctuation && nextItem.value == "."

                if (hasDot) {
                    stream.next()
                    val canParseKey = VariableParser.canParse(stream)
                    return@recoverable canParseKey
                }
            }
            return@recoverable false
        }
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.KeyAccess {
        TODO("<0c17ebef>")
//        if(!canParse(stream))
//            throw stream.throwErrorOnValue("key access")
//        val identifier =
    }
}