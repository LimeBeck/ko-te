# Ko-te roadmap

Updated 2026-09-09. This plan describes the next steps after the current unreleased
correctness and build changes. Milestones are ordered by priority, not committed dates.
Compatibility changes belong in [CHANGELOG.md](CHANGELOG.md); supported behavior is
documented in [README.MD](README.MD).

## Completed in the current changes

- Update the toolchain and dependencies, require Java 11 for the library and JDK 21 for development.
- Correct expression precedence, associativity, parentheses and nested function calls.
- Preserve nullable data and define checked Long/Double arithmetic across platforms.
- Handle quoted delimiters and escapes, produce valid JSON and improve token positions.
- Guard import cycles and depth, isolate render contexts and document error propagation.
- Separate CI from tagged releases and configure Central Publisher Portal publication.
- Add regression coverage: 52 passing tests on each of JVM, JS/Node and Linux Native.

Release configuration has been checked with a local dry-run. Remote CI and actual publication
still need end-to-end verification.

## 1. Verify the release path

- [ ] Run CI from a clean checkout on GitHub and confirm PRs cannot trigger publication.
- [ ] Configure Central Portal credentials, verify the namespace and validate signing.
- [ ] Publish an explicitly approved prerelease through the tag workflow.
- [ ] Resolve the published artifacts in small JVM, JS and Linux Native consumer projects.
- [ ] Run a consumer smoke test on Java 11, in addition to the JDK 21 build tests.

Acceptance: the prerelease is installable from the documented repositories, metadata selects
the correct platform artifacts, and a Java 11 consumer renders a template successfully.

## 2. Strengthen diagnostics and execution limits

- [ ] Include resource names, source ranges and import context in parser/runtime diagnostics.
- [ ] Cover malformed input systematically so public entry points report intentional errors.
- [ ] Add configurable limits for parsing depth, execution steps and output size.
- [ ] Check coroutine cancellation during long engine-controlled operations.
- [ ] Add generative tests for nested expressions, string scanning and malformed templates.

Acceptance: invalid or excessive input fails predictably with useful source information;
cancellation propagates and failed renders do not affect subsequent calls. Host functions
remain application-controlled code; execution limits do not provide a sandbox.

## 3. Complete core template use cases

- [ ] Define and implement output-producing loops, including empty collections and nested loops.
- [ ] Decide whether iterable and index positions should accept arbitrary expressions.
- [ ] Define null-literal and unary-operator semantics before extending the expression grammar.
- [ ] Provide an explicit HTML escaping API with documented output-context limitations.
- [ ] Turn text and HTML examples into executable end-to-end tests.

Acceptance: list/table generation works through the public API, examples match the supported
grammar, and each semantic change has migration notes and cross-platform tests.

## 4. Measure and improve repeated rendering

- [ ] Establish benchmarks for parsing, single rendering, repeated rendering and imports.
- [ ] Introduce a compile/render API with immutable compiled templates and per-render state.
- [ ] Add an optional bounded cache with explicit invalidation and resource-loader semantics.
- [ ] Verify concurrency, cancellation and memory bounds when compiled templates are reused.

Acceptance: benchmarks report the effect of each optimization, cache limits are tested, and
reusing compiled templates does not share mutable render contexts.

## 5. Define the stable support contract

- [ ] Choose the supported Native targets, including a decision on macOS ARM64 and Windows.
- [ ] Add build, test and publication coverage for every officially supported target.
- [ ] Add public API compatibility checks and document supported Kotlin/toolchain versions.
- [ ] Revisit the legacy Result/exception API for a versioned migration if needed.
- [ ] Publish migration guidance and validate a release candidate in consumer projects.

Acceptance: platform claims match CI and published artifacts; consumers can identify breaking
changes and upgrade using tested instructions.

Template inheritance, macros and a broad built-in filter library are deferred until the core
scenarios and compatibility contract are stable. Performance work should follow measurements.
