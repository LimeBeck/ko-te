# Work in progress!
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

## Rules

### Operators

1. `|` - Pipe operator.
   Get left argument and make partial application for right function (put it as first function argument)

### BNF

```
terminal TEMPLATE_SOURCE
terminal ANY
terminal CHARACTER
terminal DIGIT
terminal ROUND_BRACKET_OPEN -> '('
terminal ROUND_BRACKET_CLOSE -> ')'

STRING -> "ANY" | """ANY""" | 'ANY'
NATURAL_NUMBER -> DIGIT{DIGIT}+
NEGATIVE_NUMBER -> -NATURAL_NUMBER
OPERATION_SYMBOL -> '/' | '*' | '-' | '+' | '%' | '&'
IDENTIFIER -> CHARACTER{CHARACTER | DIGIT | '_' }+
TEMPLATE_OUTPUT = TEMPLATE_SOURCE | VALUEABLE

INFIX_OPERATION = VALUEABLE OPERATION_SYMBOL VALUEABLE
PREFIX_OPERATION = OPERATION_SYMBOL VALUEABLE
KEY_ACCESS -> VALUEABLE.IDENIFIER
INDEX_ACCESS -> VALUEABLE[NATURAL_NUMBER]
VALUEABLE -> IDENTIFIER | KEY_ACCESS | INDEX_ACCESS | FUNCTION
FUNCTION -> IDENTIFIER ROUND_BRACKET_OPEN [VALUEABLE {, VALUEABLE}+] ROUND_BRACKET_CLOSE
VARIABLE_ASSIGN -> let IDENTIFIER = VALUEABLE

CONDITIONAL -> if ROUND_BRACKET_OPEN VALUEABLE ROUND_BRACKET_CLOSE TEMPLATE_OUTPUT else TEMPLATE_OUTPUT endif
ITERATABLE -> for IDENITIFER in IDENTIFIER TEMPLATE_OUTPUT endfor
IMPORT -> import STRING
```