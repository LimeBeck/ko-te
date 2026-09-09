# Changelog

## 0.3.0

Release preparation; publication is pending.

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
