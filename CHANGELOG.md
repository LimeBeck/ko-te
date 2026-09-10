# Changelog

## 0.5.0

- Add the null literal in expressions, assignments and function arguments. Explicit null
  values now compare equal; null remains distinct from all other value types.
- Reject missing object fields and out-of-range indices with KoteRuntimeException, matching
  missing variable lookup. Explicit null fields/elements remain valid. This changes the
  previous behavior that silently substituted null for missing fields/indices.
- Allow nullable return values in CallableWrapper.from. Host RuntimeContext.set(key, null)
  still removes a binding; use RuntimeObject.Null to store null explicitly.
- Add AstLexeme.Null; exhaustive visitors over primitive AST nodes must handle it.

Migration: provide expected optional fields explicitly as null or normalize input data before
rendering. Comparing missing data with null now fails instead of hiding the missing access.
See [the null specification](docs/NULL_VALUES.md).

## 0.4.0

- Render complete conditional branches and loop bodies in source order using a shared text
  buffer, including nested blocks and imports. Empty block bodies are now accepted.
- Provide `loop.index`, `number`, `first`, `last` and `length`. Item and metadata bindings
  are restored after iteration, exceptions and cancellation; other assignments still persist.
- Recognize imports consistently when checking whether a block body can continue.

### Compatibility

- Text and expression results inside loops now appear in the output. Conditions emit all
  selected branch results, rather than only the last one. Review templates that intentionally
  relied on discarded output; assignments remain silent.
- The loop item name `loop` is reserved for metadata; rename such items when upgrading.
  An existing outer variable named `loop` is restored when the loop finishes.
- Public renderer and evaluator signatures are retained. Low-level conditional and iterable
  evaluators now return a StringWrapper containing their full output (including an empty
  string for empty output), rather than the last value or Nothing respectively.

## 0.3.0

Available from Maven Central and GitHub Packages.

### Changes

- Upgrade Kotlin, Gradle, coroutines, JUnit and CI actions; require Java 11 for the JVM
  library and JDK 21 for development.
- Correct arithmetic precedence, left associativity and parentheses, including nested
  function arguments. Expressions that relied on the old incorrect grouping change result.
- Preserve null elements and accept nullable top-level data. Null output changes from `NULL`
  to JSON-compatible `null`.
- Replace Int/Float arithmetic narrowing with checked Long/Double arithmetic. Fractional
  AST literals now use Double. Overflow, non-finite values and unsafe mixed conversions fail
  explicitly. Integral-valued numbers use integer arithmetic consistently across platforms.
- Honor quotes, escapes and raw multiline strings when scanning template delimiters.
  Language tokens carry their actual source positions.
- Escape JSON keys and string values recursively. Callable/Nothing and non-finite values
  cannot be serialized as JSON; top-level Nothing still emits an empty string.
- Bound recursive imports and isolate their stacks between top-level renders.
- Document the existing throwing error contract, supported platforms and for-loop limitations.
- Move releases to a tag-only workflow and migrate Maven Central uploads from OSSRH to
  Central Portal. New Portal-token and in-memory-signing secrets are required.

### Upgrading from 0.2.5

- Run JVM consumers on Java 11 or newer. Building the project and running its JVM tests
  requires JDK 21.
- Recheck expressions that depended on the previous grouping: `10 - 3 - 2` now produces `5`,
  and `2 * (3 + 1)` produces `8`.
- Update snapshots expecting uppercase `NULL` or lists with null elements removed. Null now
  renders as `null`, and null list elements keep their original indices.
- Update code inspecting fractional AST literals to expect Double rather than Float.
  Numeric values without a fractional part use integer arithmetic, including `5.0 / 2.0 == 2`.
  Handle `KoteRuntimeException` for overflow, non-finite values and unsafe mixed conversions.
- Use the documented string escapes; malformed strings now fail explicitly. JSON output
  escapes keys and values and rejects functions and other unsupported JSON values.
- Imports reject cycles and nesting beyond 64 resources. Hosts requiring unbounded recursive
  imports must restructure their templates.

Rendering still returns Success on success and throws on failure; coroutine cancellation
propagates. Loops still discard their body output, and strings are not automatically HTML-escaped.
