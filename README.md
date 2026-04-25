# WFX - Window Framework for JavaFX

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE.txt)

WFX is a lightweight rich client platform for JavaFX applications. Inspired by the NetBeans RCP, it enables rapid development of JavaFX applications with the typical workbench features: tab views, splittable areas, and drag-and-drop docking.

## Features

- **Window Management** — Tab-based views with drag & drop, splittable areas, registration-order independent layouts
- **Module System** — Plugin-style modules discovered via Java `ServiceLoader` or CDI
- **Optional Priority** — Modules can declare a `@Priority` to control startup order
- **CDI Integration** — Full Jakarta CDI 4.0 support via Weld SE
- **ServiceLoader Support** — Lightweight alternative without a CDI container
- **FXML Integration** — `FXMLView.Builder` ties FXML files to controllers and registration metadata

## Architecture

![WFX Architecture](docs/wfx-architecture.png)

## Requirements

- **Java 21** or higher
- **JavaFX 21** or higher
- **Maven 3.8+** for building

## Quick Start

### Building the Project

```bash
mvn clean install -DskipTests
```

### Running the Example Application

```bash
mvn -pl wfx-modules/example-gui exec:java \
    -Dexec.mainClass=io.softwareecg.wfx.examplegui.ExampleServiceLoaderMain
```

Or with CDI as the lookup strategy:

```bash
mvn -pl wfx-modules/example-gui exec:java \
    -Dexec.mainClass=io.softwareecg.wfx.examplegui.ExampleCDIMain
```

## Project Structure

| Module | Description |
|--------|-------------|
| `wfx-modules/lookup` | Core lookup / service-locator API |
| `wfx-modules/lookup-cdi` | CDI-based lookup implementation (Weld SE) |
| `wfx-modules/platform-api` | Platform API interfaces (`Module`, `EventBus`, …) |
| `wfx-modules/platform-core` | Platform core implementation |
| `wfx-modules/platform-runner` | Application launcher and lifecycle (`Main`, `CDIMain`) |
| `wfx-modules/windowmanager-api` | Window-management API (`WindowManager`, `View`, `Position`) |
| `wfx-modules/windowmanager-core` | Window-management implementation (split areas, drag&drop) |
| `wfx-modules/extensions/cdi-contexts` | CDI scope extensions (`@ViewScoped`) |
| `wfx-modules/extensions/ui-utils` | UI utility classes (menu/toolbar helpers, system views) |
| `wfx-modules/example-gui` | Example application demonstrating the framework |
| `wfx-all` | All-in-One JAR with all modules bundled |

## Maven Dependency

The easiest way to use WFX is the `wfx-all` aggregator dependency, which bundles every WFX module and its required runtime dependencies:

```xml
<dependency>
    <groupId>io.softwareecg.wfx</groupId>
    <artifactId>wfx-all</artifactId>
    <version>7.0.0-SNAPSHOT</version>
</dependency>
```

This single dependency provides:
- All WFX modules
- CDI API and Weld SE implementation
- JavaFX FXML and Controls
- SLF4J logging API
- Logback as logging implementation
- Apache Commons Lang3

If you prefer to pick individual modules, you can also add them separately (e.g. `lookup`, `platform-runner`, `windowmanager-core`).

## Creating a Simple Application

### 1. Create a Main Class

```java
import io.softwareecg.wfx.main.Main;
import javafx.application.Application;

public class MyApp extends Main {
    public static void main(String[] args) {
        Application.launch(MyApp.class, args);
    }
}
```

For CDI-based dependency injection, extend [`CDIMain`](wfx-modules/platform-runner/src/main/java/io/softwareecg/wfx/main/CDIMain.java) instead.

### 2. Create a Module

```java
import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.platform.api.Module;
import io.softwareecg.wfx.platform.api.exceptions.PlatformException;
import io.softwareecg.wfx.windowmtg.api.FXMLView;
import io.softwareecg.wfx.windowmtg.api.Position;
import io.softwareecg.wfx.windowmtg.api.WindowManager;

import java.io.IOException;

public class MyModule implements Module {

    @Override
    public void preload() throws PlatformException {
        try {
            FXMLView<MyController> view = new FXMLView.Builder<MyController>()
                    .withId("my-view")
                    .withTitle("My View")
                    .withPos(Position.CENTER)
                    .withFile(getClass().getResource("my-view.fxml"))
                    .build();
            Lookup.lookup(WindowManager.class).register(view);
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    @Override
    public void start() { /* called on FX thread after main window is shown */ }

    @Override
    public void stop()  { /* called on application shutdown */ }
}
```

### 3. Register the Module (ServiceLoader)

Create `META-INF/services/io.softwareecg.wfx.platform.api.Module`:
```
com.example.MyModule
```

For CDI-based discovery, annotate the module with `@Singleton` and ensure a `META-INF/beans.xml` is present in the JAR. The bean is then picked up automatically when running with `CDIMain`.

### Optional: declare a startup order

Module discovery order is not specified. If your module must run before/after others (e.g. a sidebar that depends on the editor area being registered first), declare a `@Priority`:

```java
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

@Singleton
@Priority(1000)   // smaller value = earlier; default is Integer.MAX_VALUE (last)
public class MySidebarModule implements Module { … }
```

## CDI Support

The [`cdi-contexts`](wfx-modules/extensions/cdi-contexts/) extension adds JavaFX-aware CDI scopes — currently `@ViewScoped` (one bean instance per registered view). See its [README](wfx-modules/extensions/cdi-contexts/README.md) for details.

## Dependencies

The framework uses:
- **Weld SE 5.1.x** — CDI implementation (Jakarta EE 10)
- **SLF4J 2.0.x** + **Logback 1.5.x** — Logging
- **JavaFX 21** — UI framework
- **JUnit 4.13** + **Mockito 5.7** — Test stack

## License

This project is licensed under the Apache License 2.0 — see the [LICENSE.txt](LICENSE.txt) file for details.

## Contributing

Contributions are welcome. Please feel free to submit a Pull Request.

## History

This project was originally developed as "wfx" at Weigend AM and has been modernised to support Java 21 and Jakarta EE 10. Package namespaces were renamed from `de.weigend.*` to `io.softwareecg.*` during the rebrand.
