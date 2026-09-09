# Changelog

## Unreleased

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
