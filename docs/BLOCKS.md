# Block output specification

This describes unreleased behavior after 0.3.0. The public render result remains a string
wrapped in Success; failures and coroutine cancellation continue to propagate as exceptions.

## Execution

A template executes nodes sequentially. Literal template text is copied unchanged, expressions
are formatted once, and assignments produce no output. Conditions require Boolean values
and execute only the selected branch. A block emits all its nodes, including nested blocks.
Imports share the current context and emit into the same render buffer. Repeated imports
execute repeatedly; the existing cycle and depth guards remain in force.

The engine owns a separate StringBuilder per top-level render. Its internal block executor
handles conditions, loops and imports directly; expression evaluators continue to return
RuntimeObject values. A list value still renders as JSON and is never used as a container
for block output. There is no public streaming API or partial string result on failure.
Host callback side effects are not rolled back.

Public RuntimeEngine, Renderer and Evaluator signatures remain available. Direct calls to
conditional/iterable evaluators capture block output as a StringWrapper. Custom Renderers
continue to supply a string, which is appended once at the import position.

## Examples

| Template | Data | Output |
|---|---|---|
| `A{{ if (flag) }}B{{ value }}C{{ else }}D{{ endif }}E` | `flag=true, value="x"` | `ABxCE` |
| `{{ for item in items }}[{{ item }}]{{ endfor }}` | `items=["a", null, "b"]` | `[a][null][b]` |
| `A{{ for item in items }}{{ missing }}{{ endfor }}B` | `items=[]` | `AB` |
| `{{ if (true) }}{{ endif }}` | none | empty string |
| `{{ if (true) }}{{ values }}{{ endif }}` | `values=[1, null]` | `[1,null]` |

## Loop scope

The source variable is read once before iteration. Reassigning it inside the body does not
change the current traversal. Only collection values are accepted; arbitrary iterable
expressions remain outside this change.

Each iteration binds the item and a `loop` object with these fields:

| Field | Meaning |
|---|---|
| `index` | Zero-based index |
| `number` | One-based position |
| `first` | True for the first item |
| `last` | True for the final item |
| `length` | Number of items in the source list |

`loop` cannot itself be the item name. Existing item and `loop` bindings are restored after
execution, including explicit null values; previously absent names become absent again.
Restoration runs in finally blocks when a callback, imported resource or nested body fails.
Nested loops restore outer metadata before the next outer-body node executes. Other `let`
assignments persist in the shared context, preserving the existing accumulator use case.

## Compatibility and verification

Compared with 0.3.0, loops begin emitting output and conditions retain the whole selected
branch. Templates relying on discarded expression results must be revised explicitly.
Low-level callers inspecting block evaluator values must now expect a StringWrapper.

Common tests cover text/value ordering, callbacks called exactly once, nested scope, empty
bodies, null items, JSON formatting, imported blocks, resource entry points, legacy engine
integration, invalid closing keywords and cleanup after exceptions and cancellation.

This change does not introduce compilation caching, HTML escaping, general execution budgets,
asynchronous host functions or new expression operators.
