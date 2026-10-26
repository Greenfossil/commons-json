# Changelog

## 1.5.0 — 2026-10-26 (final)

Final release of the 1.5.0 line; it supersedes `1.5.0-RC2`, whose tag points at
a commit that failed clean compilation (the RC1 tag). Everything below is the
1.5.0 contract — no release-candidate dependency remains.

### Added

- Safe parsing helpers: `Json.parseTry` for `String`, `Array[Byte]` and
  `InputStream`, plus `Json.parseBase64URLTry` variants.
- Deterministic output helpers: `Json.canonical` and `Json.canonicalString`.
- JSONL helpers: `Json.parseLines` and `Json.stringifyLines`, with per-line
  `JsonLineError` diagnostics.
- Regression coverage for the Jackson 3 migration and the retained Jackson 2
  compatibility defaults.

### Removed

- The unreleased `JsonPointer` / `JsonAccessError` helper was removed as an
  unnecessary enhancement; use the existing `JsValue` path accessors and their
  `toOption` variants.

### Fixed

- Restored `JsonLines` / `JsonLineError` in a dedicated source file. They had
  been co-located inside `JsonPointer.scala` and were accidentally deleted with
  it, while `Json.parseLines` / `Json.stringifyLines` still referenced them;
  clean builds now compile again.

### Changed

- Migrated the internal JSON engine to Jackson **3.1.8** (`tools.jackson`).
  commons-json no longer needs to track Armeria's Jackson 2 version; both major
  versions can coexist on one classpath.
- Migrated `json-path` integration to its Jackson 3 providers.
- Updated README, `llms.txt`, and Scaladoc for the new APIs and migration.
- Added `docs/jackson3-migration.md` with dependency, compatibility, and
  consumer upgrade guidance.
- Set the local build version to `1.5.0-RC2`.

### Compatibility notes

- The public `JsValue` and `Json` APIs are unchanged.
- `JsValue._jsonNode` and `jsonNodeToJsValue` are internal helpers and now use
  `tools.jackson.databind.JsonNode`. Code that reaches into these helpers must
  update its Jackson imports; this is documented as an internal API change.
- Jackson 2 parsing defaults are retained through
  `JsonMapper.builderWithJackson2Defaults()`, with exact BigDecimal values
  explicitly preserved.
- `jackson-annotations` remains on the Jackson 2.x coordinate/package.
- No Maven publication was performed for `1.5.0-RC1`; its tag remains at the
  failed commit and must not be reused.
