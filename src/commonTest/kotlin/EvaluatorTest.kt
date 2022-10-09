import dev.limebeck.templateEngine.Result
import dev.limebeck.templateEngine.parser.ParserError
import dev.limebeck.templateEngine.render
import dev.limebeck.templateEngine.runtime.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import utils.parseAst

class EvaluatorTest : FunSpec({
    val emptyResourceLoader = StaticResourceLoader()
    val runtimeEngine = SimpleRuntimeEngine

    val renderer = object : Renderer {
        override suspend fun render(templateName: String, context: RuntimeContext): Result<String, ParserError> {
            TODO("<cc8f3059> Not yet implemented")
        }
    }

    test("Simple template") {
        val simpleTemplate = """Simple Template"""
        val result = runtimeEngine.evaluateProgram(
            root = simpleTemplate.parseAst(),
            context = mapOf<String, RuntimeObject>().asContext(renderer, emptyResourceLoader)
        )
        result.render() shouldBe simpleTemplate
    }

    test("Template with variable") {
        val template = """Value: {{ variable }}"""
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf("variable" to RuntimeObject.StringWrapper("value")).asContext(renderer, emptyResourceLoader)
        )
        result.render() shouldBe "Value: value"
    }

    test("Template with literals") {
        val template = """{{ "value" }}{{ 100 }}"""
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            context = mapOf<String, RuntimeObject>().asContext(renderer, emptyResourceLoader)
        )
        result.render() shouldBe "value100"
    }

    test("Template with function") {
        val template = """{{ greeting(name) }}"""
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf(
                "name" to RuntimeObject.StringWrapper("World"),
                "greeting" to RuntimeObject.CallableWrapper { args, ctx ->
                    val name = args.first() as RuntimeObject.StringWrapper
                    RuntimeObject.StringWrapper("Hello, ${name.string}")
                }
            ).asContext(renderer, emptyResourceLoader)
        )
        result.render() shouldBe "Hello, World"
    }

    test("Template with high order function") {
        val template = """{{ function("Hello")(variable) }}"""
        val result = runtimeEngine.evaluateProgram(
            template.parseAst(),
            mapOf(
                "variable" to RuntimeObject.StringWrapper("World"),
                "function" to RuntimeObject.CallableWrapper { args1, _ ->
                    RuntimeObject.CallableWrapper.from { args2, _ ->
                        "${(args1.first() as RuntimeObject.StringWrapper).string}, ${(args2.first() as RuntimeObject.StringWrapper).string}"
                    }
                }
            ).asContext(renderer, emptyResourceLoader)
        )
        result.render() shouldBe "Hello, World"
    }
})