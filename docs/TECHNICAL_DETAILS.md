# WFX — Technical Documentation

## Overview

WFX is a lightweight Rich Client Platform (RCP) for JavaFX applications. It provides a modular architecture with optional CDI-based dependency injection, a window manager with splittable areas and drag-and-drop docking, and a synchronous in-process event bus.

## Technology Stack

| Component  | Version       | Notes                                              |
|------------|---------------|----------------------------------------------------|
| Java       | 21            | LTS                                                |
| JavaFX     | 21.0.5        | LTS, modular via JPMS                              |
| Weld SE    | 5.1.2.Final   | CDI 4.0 implementation (Jakarta EE 10)             |
| Jakarta CDI| 4.0.1         | Contexts and Dependency Injection                  |
| SLF4J      | 2.0.13        | Logging facade                                     |
| Logback    | 1.5.20        | SLF4J implementation                               |
| JUnit      | 4.13.2        | Unit testing                                       |
| Mockito    | 5.7.0         | Mocking framework                                  |
| Arquillian | 1.8.0.Final   | CDI integration testing                            |

## Project Structure

```
wfx/
├── pom.xml                       # Single parent POM for the whole reactor
├── wfx-modules/
│   ├── platform-api/             # Public API (Module, EventBus, …)
│   ├── platform-core/            # Core implementation
│   ├── platform-runner/          # Application bootstrap (Main, CDIMain)
│   ├── lookup/                   # Service-lookup abstraction
│   ├── lookup-cdi/               # CDI-based lookup implementation
│   ├── windowmanager-api/        # Window-management API
│   ├── windowmanager-core/       # Window-management implementation
│   ├── extensions/
│   │   ├── cdi-contexts/         # JavaFX-aware CDI scope (@ViewScoped)
│   │   └── ui-utils/             # UI utilities (menus, system views)
│   └── example-gui/              # Example application
└── wfx-all/                      # Aggregator JAR with all modules bundled
```

> Until 2026-04 the reactor had two near-identical parent POMs at `wfx/pom.xml` and `wfx/wfx-modules/pom.xml`. They have been consolidated into a single parent at `wfx/pom.xml`; submodules under `wfx-modules/` declare an explicit `<relativePath>../../pom.xml</relativePath>`.

## Module System

### Module Interface

Modules implement [`io.softwareecg.wfx.platform.api.Module`](../wfx-modules/platform-api/src/main/java/io/softwareecg/wfx/platform/api/Module.java):

```java
public interface Module {
    default String getName()    { return getClass().getSimpleName(); }
    default String getVersion() { return getClass().getPackage().getImplementationVersion(); }

    void preload() throws PlatformException;  // background thread, splash visible
    void start();                              // FX thread, after main window
    void stop();                               // application shutdown
}
```

### Module Discovery

Two strategies, swappable at boot via `Main` vs. `CDIMain`:

1. **`ServiceLoader`** (default in `Main`)
   - Register fully-qualified module class names in
     `META-INF/services/io.softwareecg.wfx.platform.api.Module`
2. **CDI** (used by `CDIMain`)
   - Annotate the module with `@Singleton` and ship a `META-INF/beans.xml`.
     Discovery happens through Weld's bean scan.

### Startup Order

`Module.start()` is documented as **non-specific order** by the interface contract. After the QS round in 2026-04, `Main.init()` sorts modules by their `jakarta.annotation.@Priority` annotation (smaller value = earlier; unannotated modules default to `Integer.MAX_VALUE` and run last). Both `preload()` and `start()` then run in the resulting stable order. Modules without `@Priority` are unaffected.

## Lookup System

The `Lookup` abstraction provides a service-locator pattern. It is the same idea as the NetBeans `Lookup`, on a deliberately small surface.

### Lookup Strategies

1. **`ServiceLoaderLookupStrategy`** (default with `Main`)
   - Uses Java's `ServiceLoader`
   - No CDI container required
   - Services registered via `META-INF/services/`

2. **`CDILookupStrategy`** (used with `CDIMain`)
   - Uses Weld SE container
   - Full CDI support: `@Inject`, `@Produces`, scopes
   - Requires `lookup-cdi` on the classpath

### Usage

```java
EventBus<ProgressEvent> bus = Lookup.lookup(EventBus.class);
List<Module>            mods = Lookup.lookupAll(Module.class);
```

## Event Bus

### `SimpleEventBus`

Thread-safe synchronous event bus:

- **Thread Safety**: `ConcurrentHashMap` for subscriptions, `CopyOnWriteArrayList` for listeners
- **Event Hierarchy**: Listeners on a superclass receive events of subclasses
- **Error Isolation**: An exception in one listener does not affect others (logged via SLF4J)
- **Consumed Semantics**: Listeners return `boolean` to indicate "still valid" (`true`) or "consumed" (`false`)

### `@EventSubscriber`

```java
@EventSubscriber(eventClass = ProgressEvent.class)
public boolean onProgress(ProgressEvent event) {
    // …
    return true;
}
```

### Built-in Event Types

- `ProgressEvent` — message + progress fraction + source
- `StartupProgressEvent` — extends `ProgressEvent`, used by the splash/preloader

## Window Management

### `WindowManager` API

The window manager owns one main `RootArea` and a list of detachable `RootArea`s for sub-windows. Views are registered through the `register(...)` overloads:

```java
WindowManager wm = Lookup.lookup(WindowManager.class);
wm.register(view);                  // shows the view at view.getDefaultPosition()
wm.register(view, parent);          // attaches relative to an existing parent view
wm.register(view, parent, false);   // adds without showing
```

### `Position` and Splittable Areas

`Position` is a richer enum than the usual cardinal directions: each direction carries the split orientation it requires and which slot of the split it occupies.

```java
public enum Position {
    TOP   (Orientation.VERTICAL,   true),    // first slot
    LEFT  (Orientation.HORIZONTAL, true),
    CENTER(null,                   false),
    RIGHT (Orientation.HORIZONTAL, false),
    BOTTOM(Orientation.VERTICAL,   false);

    public Orientation getSplitOrientation();
    public boolean     isFirstSlot();
}
```

This lets `ViewArea.add()` collapse all four side directions into a single shared parameterised path: if `this` is already split in the matching orientation, recurse into the matching slot; otherwise create a new split.

> Historical note (fixed 2026-04): the `LEFT` branch previously delegated to `getSecondChild()` when the area was already horizontally split — a copy-paste leftover from `RIGHT` that pushed every later `LEFT` registration into the existing right pane. The bug was masked by an `Platform.runLater()` workaround in client code (AI Chat sidebar). After the fix, registration order does not affect the final layout.

### `DragNDropManager`

Manages drag-and-drop of view tabs across panes and out into floating sub-windows. The currently dragged view is tracked via two static accessors on `DragNDropManagerImpl`:

```java
DragNDropManagerImpl.setDraggedViewStatus(viewStatus);
ViewStatus current = DragNDropManagerImpl.getDraggedViewStatus();
```

`ViewStatus` carries a `Status` enum (`VISIBLE` / `HIDDEN`) plus the `Position`, the parent `ViewStatus`, and the owning `TabArea`.

## CDI Integration

### `FXMLLoaderProducer`

Produces CDI-aware `FXMLLoader` instances:

```java
@Inject
FXMLLoader loader;
```

Controllers loaded through this `FXMLLoader` automatically support `@Inject` because the loader uses a `Callback<Class<?>, Object>` backed by the active CDI BeanManager.

### JavaFX Scope (cdi-contexts extension)

- `@ViewScoped` — one bean per registered view (lifecycle tied to the view's `ViewStatus`).

(Earlier drafts of this document mentioned `@StageScoped`, `@SceneScoped`, and `@FxApplicationScoped`. Those scopes are not implemented; only `@ViewScoped` exists today.)

## Application Lifecycle

```
1. Main.init()
   ├── Initialise Lookup strategy
   ├── Discover modules via ServiceLoader (or CDI in CDIMain)
   ├── Sort modules by @Priority
   └── Get PlatformApplication instance

2. Main.start(primaryStage)
   ├── Show preloader / splash screen
   ├── Module.preload() for each module — sequential, on a background
   │   thread, with progress events between each module
   ├── Hide preloader
   ├── Show main application window (first registered ApplicationWindow)
   ├── Initialise WindowManager
   └── Module.start() for each module — sequential, on the FX thread

3. Main.stop()
   ├── Module.stop() for each module
   └── PlatformApplication.stop() (closes the main stage)
```

The preload phase runs on a background `Thread "Background Startup"` so that the FX thread stays responsive for the splash screen. After preload, control hops back to the FX thread via `Platform.runLater(...)` for the `start()` sequence.

## Build System

### Maven

The only supported build system. Run from the `wfx/` directory:

```bash
mvn clean install              # build + tests
mvn clean install -DskipTests  # skip tests
```

### Surefire JVM flags

`maven-surefire-plugin` is configured (in `wfx/pom.xml`) with `--add-opens` flags for several JavaFX modules. They are required for `JavaFxTestUtils.mockReadOnlyProperty`, which reflectively patches private property fields in `Stage`/`Scene`/`Region`/`Tab` so that integration tests can inject mocked values without standing up real windows.

### Running the Example

```bash
mvn -pl wfx-modules/example-gui exec:java \
    -Dexec.mainClass=io.softwareecg.wfx.examplegui.ExampleServiceLoaderMain
```

For the CDI variant use `ExampleCDIMain`.

## Key Design Decisions

### 1. No OSGi

OSGi was removed during earlier refactoring because of:
- Complexity at JavaFX platform startup
- Classloader friction with FXML
- Simpler alternatives that fit the use case (`ServiceLoader`, CDI)

### 2. Jakarta EE 10 Migration

Migrated from `javax.*` to `jakarta.*`:
- `javax.inject` → `jakarta.inject`
- `javax.enterprise.context` → `jakarta.enterprise.context`
- Requires Weld 5.x and Arquillian 1.8.x

### 3. Single Parent POM

`wfx-modules/pom.xml` was a near-identical duplicate of `wfx/pom.xml` and used to be the actual parent picked up by submodules under `wfx-modules/` (because `relativePath` defaults to `../pom.xml`). It has been removed; the only parent now is `wfx/pom.xml`, referenced via an explicit `<relativePath>../../pom.xml</relativePath>` from each submodule.

### 4. Thread-Safe Collections

- `SimpleEventBus`: `ConcurrentHashMap` + `CopyOnWriteArrayList`
- Event bus designed for multi-threaded JavaFX applications

### 5. Lazy Initialisation

`PlatformApplicationImpl.getEventBus()` uses lazy initialisation to avoid startup-order issues.

## Testing

### Unit Tests

```bash
mvn test
```

Tests use JUnit 4 + Mockito 5.7. The `JavaFXThreadingRule` (in `windowmanager-api/src/test`) drives each test on the JavaFX application thread; `GuiTestHelper` brings up a hidden `TestFxApp` stage on first use.

### Integration Tests (CDI)

Uses Arquillian with Weld SE:

```java
@RunWith(Arquillian.class)
public class CDITest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(MyService.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject MyService service;
}
```

### GUI Tests

```java
GuiTestHelper.runInJavaFxThreadAndWait(() -> {
    // any operation that must run on the FX thread
});
```

## Known Limitations

1. **Event-bus hierarchy**: a listener registered for several event types in a class hierarchy may be called more than once for a subclass event.
2. **No async events**: the event bus is synchronous; use `Platform.runLater()` for UI updates from background threads.
3. **`ServiceLoader` caching**: discovered services are cached. Dynamic plugin loading at runtime is not supported.

## License

Apache License 2.0 — see [LICENSE.txt](../LICENSE.txt).
