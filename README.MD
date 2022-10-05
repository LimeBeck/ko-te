![GitHub Workflow Status](https://img.shields.io/github/workflow/status/limebeck/ko-te/CI)
![GitHub last commit](https://img.shields.io/github/last-commit/limebeck/ko-te)
![GitHub](https://img.shields.io/github/license/limebeck/ko-te)

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

```bnf
terminal TEMPLATE_SOURCE
terminal ANY
terminal CHARACTER -> A..Z | a..z
terminal DIGIT -> 0..9
terminal RB_OPEN -> '('
terminal RB_CLOSE -> ')'
terminal SB_OPEN -> '['
terminal SB_CLOSE -> ']'

STRING -> "ANY" | """ANY""" | 'ANY'
NATURAL_NUMBER -> DIGIT{DIGIT}+
NEGATIVE_NUMBER -> -NATURAL_NUMBER
NUMBER -> {NATURAL_NUMBER | NEGATIVE_NUMBER}[.NATURAL_NUMBER]
IDENTIFIER -> CHARACTER{CHARACTER | DIGIT | '_' }+

STATEMENT -> TEMPLATE_SOURCE | EXPRESSION

OPERATION_SYMBOL -> '/' | '*' | '-' | '+' | '%' | '&' | '|'
COMPARISON_OP_SYMBOL -> '==' | '=>' | '<=' | '!=' | '<' | '>'

INFIX_OPERATION -> EXPRESSION {OPERATION_SYMBOL | COMPARISON_OP_SYMBOL} EXPRESSION
PREFIX_OPERATION -> {'!' | '-'} EXPRESSION
KEY_ACCESS -> EXPRESSION.IDENIFIER
INDEX_ACCESS -> EXPRESSION SB_OPEN NATURAL_NUMBER SB_CLOSE
EXPRESSION -> RB_OPEN EXPRESSION RB_CLOSE 
   | IDENTIFIER 
   | KEY_ACCESS 
   | INDEX_ACCESS 
   | FUNCTION_CALL 
   | INFIX_OPERATION 
   | PREFIX_OPERATION
VARIABLE_ASSIGN -> let IDENTIFIER = EXPRESSION
FUNCTION -> IDENTIFIER RB_OPEN [EXPRESSION {, EXPRESSION}+] RB_CLOSE
FUNCTION_CALL -> IDENTIFIER RB_OPEN [EXPRESSION {, EXPRESSION}+] RB_CLOSE

CONDITIONAL -> if RB_OPEN EXPRESSION RB_CLOSE STATEMENT [else STATEMENT] endif
ITERATABLE -> for IDENITIFER in EXPRESSION STATEMENT endfor
IMPORT -> import STRING
```