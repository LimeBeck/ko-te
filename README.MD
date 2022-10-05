![GitHub Workflow Status](https://img.shields.io/github/workflow/status/limebeck/ko-te/CI)
![GitHub last commit](https://img.shields.io/github/last-commit/limebeck/ko-te)
![GitHub](https://img.shields.io/github/license/limebeck/ko-te)

# Ko-te (Kotlin Template Engine)

Pure-kotlin template engine for all platforms

## Example

```kotlin
import dev.limebeck.templateEngine.KoTeRenderer
import kotlin.test.assertEquals

val renderer = KoTeRenderer()

val simpleTextTemplate = """
    Hello, {{ name }}!
    Object value: "{{ object.value[0] }}"
""".trimIndent()

val data = mapOf(
    "name" to "World",
    "object" to mapOf(
        "value" to listOf("Simple string")
    )
)

val expectedOutput = """
    Hello, World!
    Object value: "Simple string"
""".trimIndent()

assertEquals(expectedOutput, renderer.render(simpleTextTemplate, data).getValueOrNull())
```

## Reference template

```
Variable access: {{ variable }}
Key access: {{ object.value }}
Index access: {{ array[0] }}
Function call with round brackets syntax: {{ uppercase(variable) }}
Variable assign: {{ let newVariable = "value" }}
Multiline block: {{
    let first = 20
    let second = 30
    first + second
}}
Conditional template: {{if( 1 == 2 )}} true {{ else }} false {{ endif }}
Conditional value: {{
    if( 1 == 2 ) 
        true 
    else 
        false 
    endif 
}}
Conditional block: {{ if(value) }} Value is true {{ else }} Value is false {{ endif }}
```
