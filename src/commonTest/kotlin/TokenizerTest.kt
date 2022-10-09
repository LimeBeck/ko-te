import dev.limebeck.templateEngine.inputStream.toStream
import dev.limebeck.templateEngine.parser.LanguageToken
import dev.limebeck.templateEngine.parser.MustacheLikeLanguageParser
import dev.limebeck.templateEngine.parser.MustacheLikeTemplateTokenizer
import dev.limebeck.templateEngine.parser.TemplateToken
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe

class TokenizerTest : FunSpec({
    test("Tokenize simple template") {
        val simpleTextTemplate = """
            Hello, {{ name }}!
            Object value: "{{ object.value }}"
            {{ obj }}{{ another }}
        """.trimIndent()
        val stream = simpleTextTemplate.toStream()

        val tokenizer = MustacheLikeTemplateTokenizer()
        val tokens = tokenizer.analyze(stream)

        tokens.size shouldBeExactly 7
        (tokens[0] as? TemplateToken.TemplateSource)?.text shouldBe "Hello, "
        (tokens[1] as? TemplateToken.LanguagePart)?.text shouldBe "name"
        (tokens[2] as? TemplateToken.TemplateSource)?.text shouldBe "!\nObject value: \""
        (tokens[3] as? TemplateToken.LanguagePart)?.text shouldBe "object.value"
        (tokens[4] as? TemplateToken.TemplateSource)?.text shouldBe "\"\n"
        (tokens[5] as? TemplateToken.LanguagePart)?.text shouldBe "obj"
        (tokens[6] as? TemplateToken.LanguagePart)?.text shouldBe "another"
    }

    test("Parse language tokens from template") {
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

        languageTokens.size shouldBeExactly 32
    }

    test("Parse keywords from tempalate") {
        val simpleTextTemplate = """
            {{ true }}{{ false }}
        """.trimIndent()
        val stream = simpleTextTemplate.toStream()

        val tokenizer = MustacheLikeTemplateTokenizer()
        val tokens = tokenizer.analyze(stream)

        val languageParser = MustacheLikeLanguageParser()
        val languageTokens = languageParser.parse(tokens.asSequence()).toList()

        languageTokens.size shouldBeExactly 2
        languageTokens.map { (it as? LanguageToken.Keyword)?.name } shouldBe listOf("true", "false")
    }
})