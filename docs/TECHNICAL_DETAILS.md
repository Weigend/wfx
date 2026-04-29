# WFX Technical Documentation

## Overview

WFX is a lightweight Rich Client Platform (RCP) for JavaFX applications. It
provides a modular architecture, Avaje-based infrastructure wiring, a window
manager with splittable areas and drag-and-drop docking, and a synchronous
in-process event bus.

## Technology Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21 | LTS |
| JavaFX | 21.0.5 | UI toolkit |
| Avaje Inject | 11.4 | Compile-time DI |
| SLF4J | 2.0.13 | Logging facade |
| Logback | 1.5.20 | SLF4J implementation |
| JUnit | 4.13.2 | Unit testing |
| Mockito | 5.7.0 | Mocking framework |

## Project Structure

```text
wfx/
├── pom.xml                       # Single parent POM for the reactor
├── wfx-modules/
│   ├── platform-api/             # Public API (Module, EventBus, ...)
│   ├── platform-core/            # Core implementation
│   ├── platform-runner/          # Application bootstrap (Main, AvajeMain)
│   ├── lookup/                   # Service-lookup abstraction
│   ├── lookup-avaje/             # Avaje-backed lookup strategy
│   ├── windowmanager-api/        # Window-management API
│   ├── windowmanager-core/       # Window-management implementation
│   ├── extensions/
│   │   └── ui-utils/             # UI utilities (menus, system views)
│   └── example-gui/              # Example application
└── wfx-all/                      # Aggregator JAR with all modules bundled
```

## Module System

Modules implement `io.softwareecg.wfx.platform.api.Module`:

```java
public interface Module {
    default String getName()    { return getClass().getSimpleName(); }
    default String getVersion() { return getClass().getPackage().getImplementationVersion(); }

    void preload() throws PlatformException;  // background thread, preloader visible
    void start();                              // FX thread, after main window
    void stop();                               // application shutdown
}
```

## Module Discovery

WFX supports two startup styles:

1. **Avaje Inject** via `AvajeMain`
   - Modules are normal Avaje beans, typically annotated with `@Singleton`.
   - Compile-time generated Avaje modules are discovered through
     `META-INF/services/io.avaje.inject.spi.InjectExtension`.

2. **ServiceLoader** via `Main`
   - Register fully-qualified module class names in
     `META-INF/services/io.softwareecg.wfx.platform.api.Module`.
   - This is useful for small apps and tests that do not need DI.

## Startup Order

`Main.init()` sorts modules by `jakarta.annotation.Priority`:

- smaller value = earlier
- unannotated modules default to `Integer.MAX_VALUE`
- both `preload()` and `start()` use the same order

## Lookup System

`Lookup` is a small framework boundary inspired by NetBeans Lookup. It is used
for dynamic infrastructure access such as modules, windows, event bus, and FXML
controller fallback resolution.

```java
EventBus<ProgressEvent> bus = Lookup.lookup(EventBus.class);
List<Module> modules = Lookup.lookupAll(Module.class);
```

For parameterised types, WFX provides its own CDI-free type reference:

```java
EventBus<ProgressEvent> bus =
        Lookup.lookup(new TypeRef<EventBus<ProgressEvent>>() {});
```

## DI Boundary

WFX uses DI for long-lived infrastructure:

- platform services
- modules
- event bus
- window manager
- application windows
- factories

Concrete `FXMLView` instances are not DI beans. They are runtime registration
objects containing IDs, positions, FXML files, icons, and parent relationships.

FXML controllers are only DI-managed when explicitly annotated. Controllers
with per-view state should be `@Prototype` or created directly by `FXMLLoader`.

## Event Bus

`SimpleEventBus` is a thread-safe synchronous event bus:

- `ConcurrentHashMap` for subscriptions
- `CopyOnWriteArrayList` for listener lists
- superclass listeners receive subclass events
- exceptions in one listener are logged and do not stop other listeners

`EventBusFactory` exposes one shared `SimpleEventBus` instance for both raw
`EventBus` lookup and typed `EventBus<EventObject>` injection.

Built-in event types:

- `ProgressEvent` - message + progress fraction + source
- `StartupProgressEvent` - extends `ProgressEvent`, used by the startup preloader

## Window Management

The window manager owns one main `RootArea` and detachable `RootArea`s for
sub-windows. Views are registered through `WindowManager`:

```java
WindowManager wm = Lookup.lookup(WindowManager.class);
wm.register(view);
wm.register(view, parent);
wm.register(view, parent, false);
```

`Position` defines how a view is placed:

```java
public enum Position {
    TOP, LEFT, CENTER, RIGHT, BOTTOM
}
```

Side positions create or reuse split panes. `CENTER` places a view in the
current tab area.

## FXML Loading

`FXMLLoaderFactory` creates a fresh `FXMLLoader` per lookup. Its controller
factory uses a two-step strategy:

1. Ask `Lookup` for an Avaje-managed controller.
2. If no managed controller exists, create the controller with its default
   constructor and inject resolvable `@Inject` fields as a legacy fallback.

This keeps simple controllers simple while still supporting DI-managed
controllers where needed.

## Application Lifecycle

```text
1. Main.init()
   ├── initialize lookup strategy
   ├── discover modules
   ├── sort modules by @Priority
   └── get PlatformApplication

2. Main.start(primaryStage)
   ├── show preloader
   ├── run Module.preload() sequentially on a background thread
   ├── hide preloader
   ├── show main application window
   ├── initialize WindowManager
   └── run Module.start() sequentially on the FX thread

3. Main.stop()
   ├── run Module.stop()
   ├── close PlatformApplication
   └── AvajeMain closes the BeanScope
```

## Build System

Run from the `wfx/` directory:

```bash
mvn clean install
mvn clean install -DskipTests
```

`maven-surefire-plugin` is configured with JavaFX `--add-opens` flags required
by the JavaFX tests.

## Running the Example

```bash
mvn -pl wfx-modules/example-gui exec:java
```

The example starts through `ExampleAvajeMain`.

## Key Design Decisions

### 1. No OSGi

OSGi was removed during earlier refactoring because startup and classloader
behavior made JavaFX/FXML integration harder than necessary.

### 2. No CDI Runtime

WFX uses Avaje Inject instead of a runtime CDI container. This keeps startup
lighter and makes bean wiring a build-time concern.

### 3. Single Parent POM

The only parent is `wfx/pom.xml`; submodules under `wfx-modules/` reference it
with `<relativePath>../../pom.xml</relativePath>`.

### 4. Explicit UI Runtime Objects

Views are built explicitly. DI is reserved for infrastructure and stable
services.

## Testing

```bash
mvn test
```

Tests use JUnit 4 + Mockito 5.7. JavaFX-specific tests use helpers from
`windowmanager-api/src/test`.

## Known Limitations

1. **Event-bus hierarchy**: a listener registered for several event types in a
   class hierarchy may be called more than once for a subclass event.
2. **No async events**: the event bus is synchronous; use `Platform.runLater()`
   for UI updates from background threads.
3. **`ServiceLoader` caching**: discovered services are cached. Dynamic plugin
   loading at runtime is not supported.

## License

Apache License 2.0 - see [LICENSE.txt](../LICENSE.txt).
