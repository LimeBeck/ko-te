# Ko-te roadmap

Ko-te aims to become a predictable, embeddable Kotlin Multiplatform template engine for
text generation. Development focuses on useful template features, a clear Kotlin API and
efficient reuse of templates across supported platforms.

The stages below express development priorities, not release dates. Implemented changes
are recorded in [CHANGELOG.md](CHANGELOG.md); current capabilities are described in
[README.MD](README.MD).

## 1. Complete everyday template features

Make the engine suitable for generating documents, lists and tables without moving
presentation logic into host code.

- Output-producing loops with nested iteration, empty-collection handling and loop metadata.
- Expressions in iterable and index positions.
- Null literals, unary operators and a documented approach to missing values.
- An explicit HTML escaping API with output-context rules.
- Practical, executable examples for text documents and HTML templates.

Outcome: common presentation tasks can be expressed directly in templates, with consistent
semantics across platforms and migration guidance for changes to existing loop behavior.

## 2. Improve embedding and diagnostics

Give Kotlin applications a clear integration API and make template failures easy to locate.

- A consistent public error API covering parsing, evaluation and resource loading, with a
  migration path from the existing Result/exception contract.
- Diagnostics containing resource names, source ranges and import context.
- Configurable limits for parsing depth, execution steps and generated output.
- Cooperative cancellation during engine-controlled operations.
- Generative tests for malformed input and deeply nested templates.

Outcome: applications can handle failures predictably, show actionable diagnostics to template
authors and bound engine-controlled work. Host functions remain under application control.

## 3. Compile once and render repeatedly

Reduce the cost of using the same templates for multiple data sets.

- A compile/render API exposing immutable compiled templates with separate per-render state.
- An optional bounded cache with explicit invalidation and resource-loader semantics.
- Safe concurrent reuse of compiled templates.
- Benchmarks for parsing, rendering, imports, allocations and cache behavior.

Outcome: repeated rendering avoids unnecessary parsing; measured performance and memory
bounds guide optimization without sharing mutable render contexts.

## 4. Expand platform support and API stability

Make adoption and upgrades dependable for Kotlin Multiplatform consumers.

- A deliberate Native target matrix, prioritizing evaluation of macOS ARM64 and Windows support.
- Build and behavioral coverage for every officially supported target.
- Consumer examples for JVM, JS and Native integrations.
- Public API compatibility checks and a documented Kotlin/toolchain support policy.
- A versioning and deprecation policy with migration examples for incompatible changes.

Outcome: supported platforms have matching artifacts and tests, and applications can upgrade
against an explicit compatibility contract.

## 5. Add reusable template composition

After the core language and API stabilize, support larger template collections with less
duplication.

- Define template inheritance and overridable blocks.
- Explore macros or reusable fragments with explicit arguments and variable scope.
- Design an extensible filter/function registry; add built-ins only for demonstrated use cases.
- Specify how composition interacts with imports, diagnostics, escaping and compiled templates.

Outcome: applications can share layouts and presentation logic while retaining predictable
scope and useful error messages. Feature selection should follow real integration needs.
