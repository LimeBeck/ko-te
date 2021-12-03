import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.TemplateToken
import utils.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenizerTest {
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
        val languageTokens = languageParser.parse(tokens.asSequence())
        assertEquals(33, languageTokens.toList().size)
    }
}