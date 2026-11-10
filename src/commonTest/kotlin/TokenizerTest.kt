import dev.limebeck.templateEngine.SourceFile
import dev.limebeck.templateEngine.inputStream.CharInputStream.StringPosition
import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.LexerContext
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.TemplateToken
import dev.limebeck.templateEngine.parser.ast.AstLexeme
import dev.limebeck.templateEngine.parser.ast.KoTeAstParser
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TokenizerTest {
    @Test
    fun tokensAndAstPositionsRetainTheirSourceFile() = runTest {
        val file = SourceFile("fragments/numbers.kote")
        val template = "heading\n{{ value +\n 2 }}"
        val parts = MustacheLikeTemplateTokenizer().analyze(template.toStream(), LexerContext(file))
        assertTrue(parts.all { it.startPosition.sourceFile == file && it.endPosition.sourceFile == file })

        val tokens = MustacheLikeLanguageParser().parse(parts.asSequence())
        assertTrue(tokens.all { it.startPosition.sourceFile == file })

        val operation = assertIs<AstLexeme.InfixOperation>(KoTeAstParser().parse(tokens).body.last())
        assertEquals(StringPosition(template.indexOf('+'), 2, 9, file), operation.streamPosition)
        assertEquals(StringPosition(template.indexOf('2'), 3, 1, file), operation.right.streamPosition)
    }

    @Test
    fun tokenizeSimpleTemplate() {
        val simpleTextTemplate = """
            Hello, {{ name }}!
            Object value: "{{ object.value }}"
            {{ obj }}{{ another }}
        """.trimIndent()
        val stream = simpleTextTemplate.toStream()

        val tokenizer = MustacheLikeTemplateTokenizer()
        val tokens = tokenizer.analyze(stream)
        assertEquals(7, tokens.size)
        assertEquals("Hello, ", (tokens[0] as? TemplateToken.TemplateSource)?.text)
        assertEquals("name", (tokens[1] as? TemplateToken.LanguagePart)?.text)
        assertEquals("!\nObject value: \"", (tokens[2] as? TemplateToken.TemplateSource)?.text)
        assertEquals("object.value", (tokens[3] as? TemplateToken.LanguagePart)?.text)
        assertEquals("\"\n", (tokens[4] as? TemplateToken.TemplateSource)?.text)
        assertEquals("obj", (tokens[5] as? TemplateToken.LanguagePart)?.text)
        assertEquals("another", (tokens[6] as? TemplateToken.LanguagePart)?.text)
    }

    @Test
    fun parseLanguageTokensFromTemplate() = runTest {
        val simpleTextTemplate = """
            Hello, {{ name }}!
            Object value: "{{ object.value }}"
            {{ obj }}{{ another }}
            {{
                object.value
            }} {{ let newValue="asdsad asdsad" }}
            {{ let newValue=131213.12312 }}{{ let data=asdasd+1+2 }}{{ letif}}
        """.trimIndent()
        val stream = simpleTextTemplate.toStream()

        val tokenizer = MustacheLikeTemplateTokenizer()
        val tokens = tokenizer.analyze(stream)

        val languageParser = MustacheLikeLanguageParser()
        val languageTokens = languageParser.parse(tokens.asSequence()).toList()
        assertEquals(32, languageTokens.size)
    }

    @Test
    fun parseKeywordFromTemplate() = runTest {
        val simpleTextTemplate = """
            {{ true }}{{ false }}
        """.trimIndent()
        val stream = simpleTextTemplate.toStream()

        val tokenizer = MustacheLikeTemplateTokenizer()
        val tokens = tokenizer.analyze(stream)

        val languageParser = MustacheLikeLanguageParser()
        val languageTokens = languageParser.parse(tokens.asSequence()).toList()
        assertEquals(2, languageTokens.size)
        assertContentEquals(
            listOf("true", "false"),
            languageTokens.map { (it as? LanguageToken.Keyword)?.name }
        )
    }
}
