package dev.limebeck.templateEngine.parser.ast

import dev.limebeck.templateEngine.inputStream.*
import dev.limebeck.templateEngine.parser.LanguageToken

object FunctionCallParser : AstLexemeValueParser {
    override fun canParse(stream: RewindableInputStream<LanguageToken>): Boolean {
        return recoverable(stream) {
            val canParseIdentifier = IdentifierParser.canParse(stream)
            if (canParseIdentifier && stream.hasNext()) {
                stream.next()
                return@recoverable canParseNext(stream)
            }
            return@recoverable false
        }
    }

    override fun canParseNext(stream: RewindableInputStream<LanguageToken>): Boolean = recoverable(stream) {
        if(!stream.hasNext())
            return@recoverable false

        val nextItem = stream.peek()
        val hasOpenBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "("

        if (hasOpenBracket) {
            stream.next()
            val innerValues = stream.readUntil { !(it is LanguageToken.Punctuation && it.value == ")") }
            val closedBracket = stream.peek()
            val hasClosedBracket = closedBracket is LanguageToken.Punctuation && closedBracket.value == ")"
            return@recoverable hasClosedBracket
        }
        return@recoverable false
    }

    override fun parseNext(
        stream: RewindableInputStream<LanguageToken>,
        prevValue: AstLexeme.Value
    ): AstLexeme.FunctionCall {
        val nextItem = stream.peek()
        val hasOpenBracket = nextItem is LanguageToken.Punctuation && nextItem.value == "("
        if (!hasOpenBracket)
            stream.throwErrorOnValue("punctuation '('")
        stream.skipNext(1)

        val innerValues = stream.readUntil { !(it is LanguageToken.Punctuation && it.value == ")") }
        val closedBracket = stream.peek()
        val hasClosedBracket = closedBracket is LanguageToken.Punctuation && closedBracket.value == ")"
        if (!hasClosedBracket)
            stream.throwErrorOnValue("punctuation ')'")

        val innerValueStreams = innerValues.fold(mutableListOf(mutableListOf<LanguageToken>())){ acc, languageToken ->
            if(languageToken is LanguageToken.Punctuation && languageToken.value == ","){
                if(acc.last().isEmpty()){
                    stream.throwErrorOnValue("value before ','")
                }
                acc.add(mutableListOf<LanguageToken>())
            } else {
                acc.last().add(languageToken)
            }
            acc
        }

        val arguments = innerValueStreams.map {
            AstLexeme.FunctionArgument(null,  ValueParser.parse(it.toStream()))
        }

        return AstLexeme.FunctionCall(prevValue, arguments)
    }

    override fun parse(stream: RewindableInputStream<LanguageToken>): AstLexeme.FunctionCall {
        if (!canParse(stream))
            stream.throwErrorOnValue("function call")
        val rootIdentifier = IdentifierParser.parse(stream)
        stream.next()
        if(canParseNext(stream)) {
            return parseNext(stream, rootIdentifier)
        } else {
            stream.throwErrorOnValue("function call")
        }
    }
}