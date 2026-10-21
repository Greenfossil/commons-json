# Jackson 3 migration (1.5.0-RC1)

Status: **unreleased**. No tag or Maven publication has been performed.

## Why

Jackson 2 is a transitive dependency of Armeria. Before this change,
commons-json had to pin the same `com.fasterxml.jackson.core:jackson-databind`
version as Armeria. Jackson 3 uses a different Maven group and Java package
(`tools.jackson`), so it can coexist with Armeria's Jackson 2 and upgrade on its
own schedule.

The target line is **Jackson 3.1.8**, the latest patch in the 3.1 LTS line.

## Dependency coordinates

commons-json now depends on:

```sbt
"tools.jackson.core" % "jackson-databind" % "3.1.8"
"com.jayway.jsonpath" % "json-path" % "3.0.0"
```

`json-path 3.0.0` contains both Jackson 2 and Jackson 3 providers. commons-json
uses:

- `com.jayway.jsonpath.spi.json.Jackson3JsonNodeJsonProvider`
- `com.jayway.jsonpath.spi.mapper.Jackson3MappingProvider`

`jackson-annotations` remains on the Jackson 2.x coordinate and package. In an
Armeria application, Maven/sbt resolves the annotations version supplied by the
dependency graph; Jackson 3 accepts the 2.x annotations API.

## Coexistence with Armeria

Armeria 1.42.0 continues to use Jackson 2.22.3. This is intentional:

| Component | Package | Version |
|---|---|---|
| Armeria | `com.fasterxml.jackson.*` | 2.22.3 |
| commons-json | `tools.jackson.*` | 3.1.8 |
| annotations | `com.fasterxml.jackson.annotation` | 2.x |

Because the Jackson 2 and Jackson 3 packages differ, there is no
`NoSuchMethodError`/duplicate-class conflict between the two core trees. Do not
exclude either Jackson tree solely because the other is present.

## Public API impact

The `Json` and `JsValue` surface remains unchanged. The following helpers are
**internal** and now expose Jackson 3 types:

```scala
JsValue._jsonNode: tools.jackson.databind.JsonNode
JsValue.jsonNodeToJsValue(node: tools.jackson.databind.JsonNode, ...)
```

If application code used `_jsonNode` or `jsonNodeToJsValue` directly, update its
Jackson imports and types. This is the only intentional source/binary
compatibility exception in 1.5.0-RC1 and is why these members are documented as
internal rather than supported public API.

All normal `Json`/`JsValue` operations remain source-compatible.

## Behavior compatibility

The mapper starts from `JsonMapper.builderWithJackson2Defaults()` to retain the
parsing behavior of the 1.x line, then explicitly:

- enables `USE_BIG_DECIMAL_FOR_FLOATS`
- enables `StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN`
- disables `JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES`

This preserves, among other things:

- exact BigDecimal scale (`0.0060` stays `0.0060`)
- Jackson 2 trailing-token behavior
- unknown-property and null-for-primitive behavior
- Jackson 2 enum/date defaults

Regression tests cover these expectations in `Jackson3MigrationSuite`.
`builderWithJackson2Defaults()` is a Jackson migration helper and is not a
promise that every Jackson 2 default is identical; the behavior tests are the
source of truth.

## Consumer upgrade

Update the dependency when the candidate is available:

```sbt
libraryDependencies += "com.greenfossil" %% "commons-json" % "1.5.0-RC1"
```

Applications that also use Armeria should keep Armeria's Jackson 2 dependency;
no exclusion is required. Applications that use `data-mapping`/Thorium should
verify that their dependency graph resolves a single
`com.greenfossil:commons-json_3` version.

## Verification matrix

- commons-json full suite, including JsonPath extraction and migration tests
- POM generated with `tools.jackson.core:jackson-databind:3.1.8` and no
  compile/runtime Jackson 2 databind dependency from commons-json (the
  coexistence test dependency is `test` scope)
- dependency-tree/eviction check
- Thorium source build (Jackson 2 + Jackson 3 coexistence)
- `data-mapping`, `htmx`, `agent-runtime`, `agent-web`, and `agent-workbench`
  source builds/tests
- Scaladoc generation

## Rollback

The migration is confined to the commons-json repository. Revert the source,
build, tests, and documentation changes together; no published artifact or tag
exists for this candidate.

## References

- [Why upgrade to Jackson 3](https://cowtowncoder.medium.com/why-upgrade-to-jackson-3-0-94c30e797bf2)
- [Jackson 3 migration guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
