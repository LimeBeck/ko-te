# Null and missing values (0.5.0)

`null` is a literal and an ordinary value. Missing data is an access error, not an implicit
null. This specification describes the changes prepared for 0.5.0.

| Expression | Data | Result |
|---|---|---|
| `null` | none | `null` |
| `value == null` | `value = null` | `true` |
| `null == null` | none | `true` |
| `'null' == null` | none | `false` |
| `obj.value` | `obj = {value: null}` | `null` |
| `items[0]` | `items = [null]` | `null` |
| `value` | name absent | error |
| `obj.value` | `obj = {}` | error |
| `items[0]` | `items = []` | error |
| `value == null` | name absent | error |
| `null.value` or `null[0]` | none | type error |
| `if (null)` | none | type error; conditions require Boolean |

Equality is symmetric: null equals only null. No string, numeric, Boolean, collection or
object value is coerced to null. Arithmetic, invocation, iteration and field/index access
on null fail explicitly. The existing numeric equality model is unchanged.

The literal works in assignments, groups and function arguments. `let value = null` keeps
an explicit null binding. It does not delete the variable. A host callback can return null
through CallableWrapper.from; direct RuntimeObject callbacks use RuntimeObject.Null.

RuntimeContext.set(key, null) retains its host API meaning of removing a binding. Use
RuntimeContext.set(key, RuntimeObject.Null) to store explicit null. A missing map entry is
represented by Kotlin null during lookup; an existing null value is RuntimeObject.Null.
No new public Missing value or permissive access mode is introduced.

Rendering null continues to produce the text `null`. Null list elements retain their
positions, and JSON serialization preserves null fields. Unselected conditional branches
are not evaluated, so a null check can guard access to a value that may be explicitly null.
It cannot guard an absent name: applications must supply expected optional fields as null.

## Migration from 0.4.0

Missing object fields and out-of-range indices previously produced null. They now throw
KoteRuntimeException, matching absent variable lookup. Supply optional fields explicitly,
validate list bounds in host code, or normalize the input schema before rendering.
There is no safe-navigation or default-value operator in this change.

Comparisons between two explicit null values now return true instead of false. Public AST
consumers with exhaustive when expressions over AstLexeme.Primitive must handle AstLexeme.Null.
The new node preserves the literal's source position. This is a language/API change and is
versioned as 0.5.0, without a SNAPSHOT suffix.
