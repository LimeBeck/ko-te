# Unary operators

Available from 0.6.0:

| Operator | Operand | Result |
| --- | --- | --- |
| `+value` | Number | Numeric value, normalized under the engine's numeric rules |
| `-value` | Number | Negated numeric value |
| `!value` | Boolean | Logical negation |

There is no implicit conversion: `!0`, `+'1'` and `-null` fail with
`KoteRuntimeException`. Missing variables, fields and indices still fail on lookup.
Each operand is evaluated exactly once.

Precedence, highest first: calls/field/index access, unary operators, multiplication/
division/remainder, addition/subtraction, equality. Prefixes nest right-to-left;
binary operators remain left-associative. Whitespace between operators is optional.

```text
{{ -2 * 3 + 8 }}       → 2
{{ -(2 + 3) * 4 }}     → -20
{{ 1--2 }}            → 3
{{ !!true }}          → true
{{ !(1 == 2) }}       → true
{{ -data().items[0] }} → negate the first item returned by data()
```

Prefixes work in assignments, function arguments and conditions. Unary minus is
not a decrement or mutation operator: `--value` negates twice without changing a binding.
Index positions still require nonnegative integer literals.

Arithmetic retains the checked Long/Double rules documented in the README.
Negating Long.MIN_VALUE throws instead of wrapping; unary plus preserves it.
Non-finite inputs are rejected. The sign is a separate token, so integer literal
magnitudes must still fit in Long: to express Long.MIN_VALUE, pass it as host data
or write `-9223372036854775807 - 1`.

The public AST represents prefixes as `AstLexeme.PrefixOperation`, with an operand,
a `UnaryOperation` and the source position of the prefix character. Exhaustive
visitors over expressions must handle this new node.
