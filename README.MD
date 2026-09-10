# Ko-te (Kotlin Template Engine)

A Kotlin Multiplatform engine for text templates, with no third-party production dependencies.
JVM (Java 11+), JavaScript/Node.js and Linux x64 Native are tested in CI. Host-specific
macOS x64 and Windows x64 targets are configured but are not covered by CI; ARM Native
artifacts are not currently configured. Kotlin has deprecated the macOS x64 target.

## Installation

Use Maven Central and the root multiplatform artifact:

```kotlin
repositories { mavenCentral() }
// In commonMain.dependencies (or dependencies for a JVM project):
implementation("dev.limebeck:ko-te:0.3.0")
```

`0.3.0` is available from Maven Central. This checkout builds `0.5.0`; its block-output and null/missing-value
changes require building locally until the corresponding releases are published. See [CHANGELOG.md](CHANGELOG.md)
for migration notes.

## Example

Rendering is suspending. Call it from a coroutine or a suspending function:

```kotlin
import dev.limebeck.templateEngine.KoTeRenderer

suspend fun greeting(): String {
    val renderer = KoTeRenderer()
    return renderer.render(
        "Hello, {{ name }}! Object value: {{ object.value[0] }}",
        mapOf("name" to "World", "object" to mapOf("value" to listOf("example")))
    ).getValueOrNull()!!
}
```

The result is `Hello, World! Object value: example`.

## Template syntax

```text
Variable: {{ variable }}
Object key: {{ object.value }}
List index (non-negative Int literal): {{ array[0] }}
Host function: {{ uppercase(variable) }}
Assignment: {{ let newVariable = "value" }}
Multiline code: {{
    let first = 20
    let second = 30
    first + second
}}
Conditional: {{ if(value) }}yes{{ else }}no{{ endif }}
Import: {{ import "resourceName" }}
```

Functions such as `uppercase` are provided by the host through `predefinedObjectsProvider`;
there is no implicit standard function set. Imports use the supplied `ResourceLoader` and
share the current render's variables. Import cycles and depth greater than 64 are rejected.
Each top-level render has its own context and import stack.

Pipe syntax and template inheritance are not implemented.

## Blocks (unreleased)

Conditions emit every node of the selected branch, in source order. Loops emit their body
once per item, and imports emit the imported template at the import position. Assignments
emit no text. Empty bodies and loops over empty lists produce no output.

```text
{{ for item in items }}[{{ loop.number }}: {{ item }}]{{ endfor }}
```

For `items = listOf("a", "b")`, the result is `[1: a][2: b]`.
The iterable must still be a variable containing a list; it is read once before iteration.
Each iteration provides `loop.index` (zero-based), `loop.number` (one-based), `loop.first`,
`loop.last` and `loop.length`. The name `loop` cannot be used as the item variable.

Nested loops temporarily shadow the outer item and loop metadata. These bindings are restored
after the loop, including when evaluation throws or is cancelled. Other assignments retain
their existing shared-context behavior, including assignments made by imported templates.
Values inside blocks retain their usual formatting: lists and maps are JSON, while the
combined block output is plain text. No additional HTML escaping is applied.

In **0.3.0**, loops discard their body output and conditions return only the last result of
the selected branch. The new behavior is an intentional compatibility change. See the
[block specification](docs/BLOCKS.md) for examples and compatibility details.

## Expressions and values

Operators are left-associative. From highest to lowest precedence: `* / %`, `+ -`, `==`.
Parentheses override precedence, including in function arguments. Division of integral values
truncates toward zero. There are no unary operators; negative values can be supplied as data
or computed with subtraction.

Numeric operations use checked Long arithmetic for integral values in the Long range and
Double arithmetic for fractional values. This includes integral-valued Float/Double input
(e.g. `5.0 / 2.0` is `2`), giving the same behavior on JS, where numeric JVM types are not
preserved. Fractional literals are parsed as Double. Overflow, division by zero and non-finite
results throw `KoteRuntimeException`. Mixing integers outside ±9,007,199,254,740,991 with
fractional values is rejected to avoid silent conversion loss. Float/Double inputs already
have their host language's floating-point precision; the engine cannot restore lost digits.

Data can contain strings, booleans, supported numeric values, lists, string-keyed maps,
`RuntimeObject` values and null. Nulls retain their list positions; top-level null is accepted.
Null renders as `null`. The `null` literal works in expressions, assignments and function
arguments. `null == null` is true; null does not equal a string, number or Boolean.

Access is strict: absent variables, missing object fields and out-of-range list indices throw
`KoteRuntimeException`. An explicit null field or list element remains a value. Accessing a
field/index of null also fails; null is not implicitly converted to Boolean or another type.
Supply optional fields as null in the input data, then guard their use with `if (value == null)`.
See [null and missing-value semantics](docs/NULL_VALUES.md) for migration from 0.4.0.

Single- and double-quoted strings support `\\`, `\"`, `\'`, `\/`, `\n`, `\r`, `\t`, `\b`,
`\f` and `\uXXXX`. Triple-double-quoted strings are raw and can span multiple lines.
Template delimiters inside strings are literal text. Invalid or unterminated strings are errors.

Strings are emitted as plain text. Maps and lists are emitted as JSON with escaped keys and
values; unsupported JSON values and non-finite numbers are errors. HTML is **not escaped**:
when generating HTML, provide host escaping functions appropriate to the output context.

## Errors and execution limits

For compatibility, `render` and `renderFromResource` return `Result.Success` on success but
**throw** syntax and runtime errors; they do not return `Result.Error`. Syntax errors include
`LexerError` and `LanguageError` (both implement `ParserError`); malformed low-level input can
also throw `StreamError`. Runtime errors use `KoteRuntimeException`. Loader and host-function
exceptions, including coroutine cancellation, propagate. `getValueOrNull()` does not catch them.

Templates are intended for trusted input. The import guard is not a sandbox: there is no
budget for general AST recursion, execution steps or output size. Host functions have the
permissions of the application. Compilation caching and a compile/render API are future work.

## Development

Use JDK 21 to build and test. The published JVM library requires Java 11 or newer.
Gradle 9.7.0 and Kotlin 2.4.20 are pinned in the build.

Update the default library version for each new change set: use the next patch version for
fixes and the next minor version for features or pre-1.0 behavior changes. Use the final version directly, without a `-SNAPSHOT` suffix.

See [ROADMAP.md](ROADMAP.md) for planned milestones and their acceptance criteria.

```shell
./gradlew jvmTest jsNodeTest nativeTest
./gradlew build
./gradlew dependencyUpdates
```

Gradle downloads Node.js and Kotlin/Native toolchains as needed. Commit the JS dependency
lock file; after an intentional dependency update, run `./gradlew kotlinUpgradeYarnLock`.

## Publishing

PRs and pushes to `master` run CI without publishing credentials. A `v<version>` tag triggers
[the release workflow](.github/workflows/release.yml), which tests the tagged source, signs
artifacts and uploads them to Central Portal and GitHub Packages. The tag supplies the version
through `-PreleaseVersion`; do not reuse a published version.

Configure these repository secrets before releasing:

- `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`: a Central Portal user token for the
  verified `dev.limebeck` namespace, not the retired OSSRH credentials.
- `SIGNING_KEY`: the ASCII-armored private GPG key (not the former encrypted/base64 key file).
- `SIGNING_PASSWORD` and, if needed, `SIGNING_KEY_ID`: its passphrase and key ID.

The public signing key must be available to Central. The workflow uses an in-memory key and
leaves the Central deployment for manual release after validation in the Portal. Actual account,
namespace and credential validation requires an authorized release and is not part of local tests.
See [Central publishing setup](https://vanniktech.github.io/gradle-maven-publish-plugin/central/).
