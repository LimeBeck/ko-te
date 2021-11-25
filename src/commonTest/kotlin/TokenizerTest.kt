import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.TemplateToken
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
}