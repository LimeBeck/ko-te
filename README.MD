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

`0.3.0` is the release prepared by this checkout; repository availability depends on completion
of publication. See [CHANGELOG.md](CHANGELOG.md) for changes and migration notes from `0.2.5`.

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

The existing `for item in items ... endfor` construct executes assignments and host calls;
it intentionally discards the body's output. It currently accepts a variable as its iterable,
not an arbitrary expression. Pipe syntax and template inheritance are not implemented.

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
Null renders as `null`. The literal keyword `null` is reserved but is not yet an expression.

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
