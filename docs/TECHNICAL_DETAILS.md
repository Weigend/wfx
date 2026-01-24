# WFX (stagediver.fx) - Technical Documentation

## Overview

WFX is a lightweight Rich Client Platform (RCP) for JavaFX applications. It provides a modular architecture with dependency injection, window management, and an event bus system.

## Technology Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 17 | LTS, required for pattern matching and sealed classes |
| JavaFX | 21.0.5 | Latest LTS, modular via JPMS |
| Weld SE | 5.1.2.Final | CDI 4.0 implementation (Jakarta EE 10) |
| Jakarta CDI | 4.0.1 | Contexts and Dependency Injection |
| SLF4J | 2.0.9 | Logging facade |
| Logback | 1.4.11 | SLF4J implementation |
| JUnit | 4.13.2 | Unit testing |
| Mockito | 1.10.19 | Mocking framework |
| Arquillian | 1.8.0.Final | CDI integration testing |

## Project Structure

```
stagediver.fx/
├── platform-api/          # Public API interfaces
├── platform-core/         # Core implementation
├── platform-runner/       # Application bootstrap
├── lookup/                # Service lookup abstraction
├── lookup-cdi/            # CDI-based lookup implementation
├── windowmanager-api/     # Window management API
├── windowmanager-core/    # Window management implementation
├── extensions/            # Optional extensions
│   ├── cdi-contexts/      # JavaFX-aware CDI scopes
│   ├── ui-utils/          # UI utilities
│   └── logger-console*/   # Console logging extensions
├── example-gui/           # Example application
└── itests/                # Integration tests
```

## Module System

### Module Interface

Modules implement `de.qaware.sdfx.platform.api.Module`:

```java
public interface Module {
    String getName();
    String getVersion();
    void preload();    // Called during splash screen
    void start();      // Called after main window shown
    void stop();       // Called during shutdown
}
```

### Module Discovery

Modules are discovered via Java ServiceLoader:
- Register in `META-INF/services/de.qaware.sdfx.platform.api.Module`

## Lookup System

The Lookup system provides a service locator pattern abstraction.

### Lookup Strategies

1. **ServiceLoaderLookupStrategy** (default)
   - Uses Java's `ServiceLoader`
   - No additional dependencies
   - Services registered via `META-INF/services/`

2. **CDILookupStrategy**
   - Uses Weld SE container
   - Full CDI support with `@Inject`, `@Produces`, scopes
   - Requires `lookup-cdi` module

### Usage

```java
// Single lookup
EventBus bus = Lookup.lookup(EventBus.class);

// Multiple implementations
List<Module> modules = Lookup.lookupAll(Module.class);
```

## Event Bus

### SimpleEventBus

Thread-safe event bus with the following features:

- **Thread Safety**: Uses `ConcurrentHashMap` for subscriptions and `CopyOnWriteArrayList` for listeners
- **Event Hierarchy**: Listeners for superclasses receive events of subclasses
- **Error Isolation**: Exceptions in one listener don't affect others (logged via SLF4J)
- **Consumed Semantics**: Listeners return `boolean` to indicate if event was consumed

### EventSubscriber Annotation

Methods can be annotated to auto-register with the event bus:

```java
@EventSubscriber(eventClass = ProgressEvent.class)
public boolean onProgress(ProgressEvent event) {
    // Handle event
    return true; // Event still valid
}
```

### Event Types

- `ProgressEvent` - Progress updates with message and percentage
- `StartupProgressEvent` - Startup-specific progress (extends ProgressEvent)

## Window Management

### DragNDropManager

Manages drag-and-drop operations for views and windows:

```java
DragNDropManager dnd = Lookup.lookup(DragNDropManager.class);
dnd.setDraggedViewStatus(ViewDragStatus.DRAGGING);
ViewDragStatus status = dnd.getDraggedViewStatus();
```

Note: Legacy methods `getDragedViewStatus()`/`setDragedViewStatus()` are deprecated but retained for backward compatibility.

## CDI Integration

### FXMLLoaderProducer

Produces CDI-aware `FXMLLoader` instances:

```java
@Inject
FXMLLoader loader;
```

Controllers loaded via FXML automatically support `@Inject`.

### JavaFX Scopes (cdi-contexts extension)

- `@StageScoped` - Bean per Stage
- `@SceneScoped` - Bean per Scene  
- `@FxApplicationScoped` - Singleton for JavaFX application

## Application Lifecycle

```
1. Main.init()
   ├── Initialize Lookup strategy
   ├── Discover modules via ServiceLoader
   └── Get PlatformApplication instance

2. Main.start(primaryStage)
   ├── Show preloader/splash screen
   ├── Module.preload() for all modules (parallel)
   ├── Hide preloader
   ├── Initialize WindowManager
   ├── Module.start() for all modules (sequential)
   └── Show main application window

3. Main.stop()
   ├── Module.stop() for all modules (reverse order)
   └── Shutdown WindowManager
```

## Build System

### Maven

Primary build system. Run with:

```bash
mvn clean install
```

### Gradle

Gradle wrappers available for some modules but Maven is the main build.

### Running the Example

```bash
cd example-gui
mvn javafx:run
```

Or with explicit main class:
```bash
mvn exec:java -Dexec.mainClass=de.qaware.sdfx.main.Main
```

## Key Design Decisions

### 1. No OSGi

OSGi was removed due to:
- Complexity in JavaFX platform startup
- Classloader issues with FXML
- Simpler alternatives available (ServiceLoader, CDI)

### 2. Jakarta EE 10 Migration

Migrated from `javax.*` to `jakarta.*` namespaces:
- `javax.inject` → `jakarta.inject`
- `javax.enterprise.context` → `jakarta.enterprise.context`
- Requires Weld 5.x, Arquillian 1.8.x

### 3. Thread-Safe Collections

- `SimpleEventBus`: `ConcurrentHashMap` + `CopyOnWriteArrayList`
- Event bus designed for multi-threaded JavaFX applications

### 4. Lazy Initialization

`PlatformApplicationImpl.getEventBus()` uses lazy initialization to avoid startup order issues.

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests (CDI)

Uses Arquillian with Weld SE:

```java
@RunWith(Arquillian.class)
public class CDITest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(...)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    @Inject
    MyService service;
}
```

### GUI Tests

Uses `GuiTestHelper` for JavaFX thread management:

```java
GuiTestHelper.runInJavaFxThreadAndWait(() -> {
    // JavaFX operations
});
```

## Dependencies

### Core Dependencies (platform-api)

```xml
<dependency>
    <groupId>jakarta.inject</groupId>
    <artifactId>jakarta.inject-api</artifactId>
    <version>2.0.1</version>
</dependency>
```

### CDI Dependencies (lookup-cdi)

```xml
<dependency>
    <groupId>org.jboss.weld.se</groupId>
    <artifactId>weld-se-core</artifactId>
    <version>5.1.2.Final</version>
</dependency>
```

### Removed Dependencies

- `commons-collections4` - Replaced with Java standard library
- `commons-lang3` (partially) - `Objects.hash()`, `Objects.equals()` used instead

## Known Limitations

1. **Event Bus Hierarchy**: When a listener registers for multiple event types in a hierarchy, it may be called multiple times for subclass events
2. **No Async Events**: Event bus is synchronous; use `Platform.runLater()` for UI updates from background threads
3. **ServiceLoader Caching**: Services are cached; dynamic module loading not supported

## Repository

- **GitHub**: https://github.com/jweigend/wfx
- **Original**: gitlab/qaware/stagediver.fx (archived)

## License

Apache License 2.0
