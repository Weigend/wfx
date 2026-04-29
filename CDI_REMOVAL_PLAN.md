# CDI Removal Plan

This plan describes the cleanup path for removing Weld/CDI from WFX after the
Avaje Inject migration is stable. The goal is not to replace every dynamic
framework lookup with DI. The target architecture keeps DI for long-lived
infrastructure and keeps views/controllers explicit where that is simpler.

## Target State

- Avaje Inject is the only DI container used by WFX.
- `Lookup` remains as a framework boundary for dynamic module/view/plugin
  access.
- `ServiceLoader` may remain as a lightweight non-DI fallback if still useful.
- `jakarta.inject` and `jakarta.annotation.Priority` may remain.
- `jakarta.enterprise.*`, Weld, Arquillian CDI, `beans.xml`, and CDI extensions
  are removed from production code.
- `FXMLView` instances stay manually created runtime objects, not DI beans.
- FXML controllers are only Avaje-managed when explicitly annotated. Otherwise
  they are created by `FXMLLoader` using the existing fallback path.

## Phase 1: Remove CDI Runtime Modules

Remove the modules that exist only for Weld/CDI:

- `wfx-modules/lookup-cdi`
- `wfx-modules/extensions/cdi-contexts`

Update:

- root `pom.xml`
- `wfx-modules/extensions/pom.xml`
- `wfx-all/pom.xml`
- any dependency references to `lookup-cdi` or `cdi-contexts`

Expected follow-up:

- The reactor should compile without those modules.
- `wfx-all` should no longer bundle CDI/Weld modules.
- Any failing references identify remaining CDI coupling that needs to move to
  Avaje or be deleted.

## Phase 2: Remove CDI Launchers

Delete CDI startup classes and examples:

- `wfx-modules/platform-runner/src/main/java/io/softwareecg/wfx/main/CDIMain.java`
- `wfx-modules/example-gui/src/main/java/io/softwareecg/wfx/examplegui/ExampleCDIMain.java`

Delete or rewrite tests:

- `wfx-modules/platform-runner/src/test/java/io/softwareecg/wfx/main/CDIMainTest.java`
- tests under `wfx-modules/lookup-cdi`
- tests under `wfx-modules/extensions/cdi-contexts`

Keep:

- `Main` for ServiceLoader fallback, if still desired.
- `AvajeMain` as the DI startup path.
- `ExampleAvajeMain` as the default example launcher.

## Phase 3: Remove CDI Dependencies

Remove from dependency management and module POMs:

- `org.jboss.weld.se:weld-se-core`
- `jakarta.enterprise:jakarta.enterprise.cdi-api`
- `org.jboss.arquillian.container:arquillian-weld-embedded`
- Arquillian CDI test dependencies if no longer used elsewhere

Search targets:

```bash
rg -n "weld|arquillian|jakarta\.enterprise|javax\.enterprise|lookup-cdi|cdi-contexts" .
```

At the end of this phase, production code should have no `jakarta.enterprise.*`
or Weld imports.

## Phase 4: Remove CDI Resources

Delete all CDI discovery resources:

- `META-INF/beans.xml`
- `META-INF/services/jakarta.enterprise.inject.spi.Extension`

Search target:

```bash
find . -path '*/META-INF/beans.xml' -o -path '*/META-INF/services/jakarta.enterprise.inject.spi.Extension'
```

Keep Avaje-generated and Avaje source resources:

- `META-INF/services/io.avaje.inject.spi.InjectExtension`

## Phase 5: Replace CDI TypeLiteral

`Lookup` currently uses `jakarta.enterprise.util.TypeLiteral`, which keeps a
hard CDI API dependency even after Weld is removed. Replace it with a WFX-owned
type reference.

Candidate API:

```java
public abstract class TypeRef<T> {
    public Type getType() {
        // capture parameterized superclass type
    }
}
```

Update:

- `Lookup`
- `LookupStrategy`
- `ServiceLoaderLookupStrategy`
- `AvajeLookupStrategy`
- related tests in `lookup` and `lookup-avaje`

Compatibility option:

- Add a temporary adapter overload from CDI `TypeLiteral` only if downstream
  users need a migration window. Otherwise remove it completely.

Completion criterion:

- `rg -n "jakarta\.enterprise\.util\.TypeLiteral"` returns no production hits.

## Phase 6: Simplify EventBus Wiring

Current migration code keeps CDI and Avaje producers returning the same static
instance. After CDI removal this can be simplified.

Remove:

- `EventBusProducer`
- CDI comments in `EventBusFactory`
- `@Vetoed` from `SimpleEventBus`

Then choose one final design:

1. Direct singleton bean:

   - Annotate `SimpleEventBus` as `@Singleton`.
   - Make it the single `EventBus` implementation.
   - Remove `SimpleEventBusHolder` and possibly `EventBusFactory`.

2. Explicit Avaje bean provider:

   - Keep a renamed factory such as `EventBusBeans`.
   - Return one `SimpleEventBus` instance.
   - Use this only if raw `EventBus` and typed `EventBus<EventObject>` both
     need distinct Avaje registrations.

Preferred target:

- Direct `@Singleton SimpleEventBus` unless Avaje generic lookup requires the
  explicit factory.

## Phase 7: Simplify FXML Loader Wiring

Keep the current pragmatic behavior:

- First ask `Lookup` for a managed controller.
- If none exists, instantiate via default constructor.
- Inject annotated fields only where Avaje can resolve them.

Remove CDI migration wording:

- references to `FXMLLoaderProducer`
- references to CDI `@Dependent`
- comments that describe coexistence with CDI

Do not force every controller into DI. Controllers should become Avaje beans
only when they have real dependencies or lifecycle requirements.

## Phase 8: Documentation Cleanup

Update:

- `README.md`
- `docs/TECHNICAL_DETAILS.md`

Remove or rewrite:

- CDI feature claims
- `CDIMain` instructions
- `lookup-cdi` module description
- `cdi-contexts` module description
- Weld/Arquillian requirements
- `beans.xml` instructions

Add:

- `AvajeMain` example
- current rule of thumb: DI for infrastructure, explicit objects for views
- explanation of `Lookup` as a WFX boundary, not general-purpose application DI

## Phase 9: Build and Test Checklist

Run after each major phase:

```bash
mvn install -DskipTests
```

Run after code deletion and TypeRef replacement:

```bash
mvn test
```

Run the example application:

```bash
mvn -pl wfx-modules/example-gui exec:java
```

Manual verification:

- Avaje logs list the expected WFX modules.
- The example preloader opens.
- Main window opens.
- Example views register.
- Adding a test tab still works.
- View overview still opens.
- Shutdown closes the Avaje `BeanScope`.

## Risks and Decisions

- `TypeLiteral` replacement is the main API-breaking part. Decide whether WFX
  can break that API now or needs a compatibility bridge.
- Removing `cdi-contexts` drops `@ViewScoped`. If no current WFX code depends
  on view-scoped CDI beans, delete it. If users depend on it, document that
  controller state should be explicit or add a small non-CDI view-scope concept
  later.
- EventBus simplification should preserve the single shared bus invariant.
- Keep `ServiceLoader` only if WFX still wants a no-DI startup path. If Avaje is
  mandatory, `Main` can eventually be simplified too.

## Done Criteria

- No `lookup-cdi` or `cdi-contexts` modules in the reactor.
- No Weld dependencies.
- No Arquillian CDI dependencies.
- No `META-INF/beans.xml`.
- No `jakarta.enterprise.*` imports in production code.
- Example app starts through `ExampleAvajeMain`.
- Reactor tests pass or known GUI-only tests are documented separately.
