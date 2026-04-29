# WFX - Window Framework for JavaFX

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE.txt)

WFX is a lightweight rich client platform for JavaFX applications. Inspired by
the NetBeans RCP, it provides workbench-style features: tab views, splittable
areas, drag-and-drop docking, a startup preloader, module discovery, and a
synchronous in-process event bus.

## Features

- **Window Management** - tab-based views with drag & drop, splittable areas, and registration-order independent layouts
- **Module System** - modules discovered via Avaje Inject or Java `ServiceLoader`
- **Optional Priority** - modules can declare `@Priority` to control startup order
- **Preloader** - splash window shown while modules run their `preload()` work, with progress events on the platform event bus
- **Bootstrapping Support** - clear `init` → preloader → `preload()` → main window → `start()` lifecycle so expensive work happens at the right phase
- **Avaje Inject Integration** - compile-time DI for framework infrastructure
- **ServiceLoader Support** - lightweight non-DI fallback for simple applications and tests
- **FXML Integration** - `FXMLView.Builder` ties FXML files to controllers and registration metadata

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
mvn -pl wfx-modules/example-gui exec:java
```

This starts `io.softwareecg.wfx.examplegui.ExampleAvajeMain`.

## Project Structure

| Module | Description |
|--------|-------------|
| `wfx-modules/lookup` | Core lookup / service-locator API |
| `wfx-modules/lookup-avaje` | Avaje-backed lookup implementation |
| `wfx-modules/platform-api` | Platform API interfaces (`Module`, `EventBus`, ...) |
| `wfx-modules/platform-core` | Platform core implementation |
| `wfx-modules/platform-runner` | Application launcher and lifecycle (`Main`, `AvajeMain`) |
| `wfx-modules/windowmanager-api` | Window-management API (`WindowManager`, `View`, `Position`) |
| `wfx-modules/windowmanager-core` | Window-management implementation (split areas, drag & drop) |
| `wfx-modules/extensions/ui-utils` | UI utility classes (menu/toolbar helpers, system views) |
| `wfx-modules/example-gui` | Example application demonstrating the framework |
| `wfx-all` | All-in-One JAR with all WFX modules bundled |

## Maven Dependency

The easiest way to use WFX is the `wfx-all` aggregator dependency:

```xml
<dependency>
    <groupId>io.softwareecg.wfx</groupId>
    <artifactId>wfx-all</artifactId>
    <version>7.0.0-SNAPSHOT</version>
</dependency>
```

This dependency provides the WFX modules, JavaFX FXML/Controls, Avaje Inject,
SLF4J, Logback, and Apache Commons Lang3.

If you prefer to pick individual modules, add them separately, for example
`lookup`, `lookup-avaje`, `platform-runner`, and `windowmanager-core`.

## Creating a Simple Application

A minimal WFX application has three pieces: a launcher class, an
`ApplicationWindow` that hosts the menu/tool bars and the docking area, and
one or more `Module`s that register FXML views at a `Position`.

### 1. Create a Main Class

Use `AvajeMain` when the application uses Avaje-managed WFX infrastructure:

```java
import io.softwareecg.wfx.main.AvajeMain;
import javafx.application.Application;

public class MyApp extends AvajeMain {
    public static void main(String[] args) {
        Application.launch(MyApp.class, args);
    }
}
```

Use `Main` only for a pure `ServiceLoader` setup.

### 2. Define an Application Window

WFX picks the registered `ApplicationWindow` bean during startup and lets it
load its own scene. Subclass `DefaultApplicationWindow` and load an FXML that
declares the menu/tool/status bars plus the central docking area:

```java
import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.windowmtg.windows.DefaultApplicationWindow;
import jakarta.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

@Singleton
public class MyApplicationWindow extends DefaultApplicationWindow {

    @javafx.fxml.FXML
    private BorderPane root;

    @Override
    public void init() throws IOException {
        FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
        loader.setLocation(getClass().getResource("MyApplicationWindow.fxml"));
        loader.setController(this);

        Parent scene = loader.load();
        getStage().setScene(new Scene(scene));
        getStage().setMaximized(true);

        // Hand the WFX docking area into the layout.
        root.setCenter(getWindowManager().getRootPane());

        useSystemMenuBarIfPossible();
    }
}
```

The matching `MyApplicationWindow.fxml` only needs to wire the IDs that
`DefaultApplicationWindow` expects (`menuBar`, `toolbar`, `statusBar`) and a
container that the central docking area can be plugged into:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane fx:id="root" xmlns:fx="http://javafx.com/fxml">
    <top>
        <VBox>
            <MenuBar fx:id="menuBar">
                <Menu text="File">
                    <MenuItem text="Exit" />
                </Menu>
            </MenuBar>
            <ToolBar fx:id="toolbar" />
        </VBox>
    </top>
    <bottom>
        <HBox fx:id="statusBar" />
    </bottom>
</BorderPane>
```

`getWindowManager().getRootPane()` is set as the `BorderPane`'s center at
runtime — that is the area where module-registered views appear.

### 3. Register an FXML View at a Position

Modules register views with the `WindowManager`. The `Position` enum
(`TOP`, `LEFT`, `CENTER`, `RIGHT`, `BOTTOM`) decides where the view docks
inside the application window's central area.

```java
import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.platform.api.Module;
import io.softwareecg.wfx.platform.api.exceptions.PlatformException;
import io.softwareecg.wfx.windowmtg.api.FXMLView;
import io.softwareecg.wfx.windowmtg.api.Position;
import io.softwareecg.wfx.windowmtg.api.WindowManager;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
public class MyModule implements Module {

    @Override
    public void preload() throws PlatformException {
        try {
            FXMLView<MyController> view = new FXMLView.Builder<MyController>()
                    .withId("my-view")
                    .withTitle("My View")
                    .withPos(Position.CENTER)         // try LEFT/RIGHT/TOP/BOTTOM
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

Each `FXMLView.Builder` call binds a controller class to an FXML file. The
controller is resolved through the active `Lookup` strategy (Avaje by
default), so it can use constructor or field injection.

### 4. Follow the WFX DI Boundary

WFX uses DI for long-lived infrastructure:

- platform services
- modules
- event bus
- window manager
- application windows
- factories

Concrete `FXMLView` instances are runtime registration objects and should be
created explicitly. FXML controllers are not singletons by default; annotate
them only when they really need DI-managed dependencies. Use `@Prototype` for
controllers that must be created fresh per FXML load.

### Optional: Declare a Startup Order

Module discovery order is not specified. If your module must run before or
after others, declare a `@Priority`:

```java
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

@Singleton
@Priority(1000)   // smaller value = earlier; default is Integer.MAX_VALUE
public class MySidebarModule implements Module { ... }
```

## Dependencies

The framework uses:

- **Avaje Inject 11.x** - compile-time dependency injection
- **SLF4J 2.0.x** + **Logback 1.5.x** - logging
- **JavaFX 21** - UI framework
- **JUnit 4.13** + **Mockito 5.7** - test stack

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## Contributing

Contributions are welcome. Please feel free to submit a Pull Request.

## History

This project was originally developed as "wfx" at Weigend AM and has been
modernised to support Java 21. Package namespaces were renamed from
`de.weigend.*` to `io.softwareecg.*` during the rebrand.
