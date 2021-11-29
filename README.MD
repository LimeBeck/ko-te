# Ko-te (Kotlin Template Engine)

Pure-kotlin template engine for all platforms

## Reference template

```
Variable access: {{ variable }}
Key access: {{ object.value }}
Index access: {{ array[0] }}
Function call with round brackets syntax: {{ uppercase(variable) }}
Function call with pipe syntax: {{ variable | uppercase }}
Variable assign: {{ let newVariable = "value" | uppercase }}
Multiline block: {{
    let first = 20
    let second = 30
    first + second
}}
```