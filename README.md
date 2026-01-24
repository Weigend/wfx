# WFX - Window Framework for JavaFX

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE.txt)

WFX (formerly stagediver.fx) is a lightweight rich client platform for JavaFX applications. Inspired by NetBeans RCP, it enables rapid development of JavaFX applications with typical window management features like tab views, split panes, and docking.

## Features

- **Window Management** - Tab-based views with drag & drop support, split panes, and flexible layouts
- **CDI Integration** - Full support for Jakarta CDI (Context and Dependency Injection)
- **ServiceLoader Support** - Alternative lightweight dependency injection via Java ServiceLoader
- **Modular Architecture** - Plugin-based module system for extending applications
- **FXML Integration** - Seamless integration with JavaFX FXML views

## Requirements

- **Java 17** or higher
- **JavaFX 21** or higher
- **Maven 3.8+** for building

## Quick Start

### Building the Project

```bash
mvn clean install -DskipTests
```

### Running the Example Application

```bash
cd example-gui
mvn compile exec:java
```

Or with CDI as lookup strategy:

```bash
cd example-gui
mvn compile exec:java -Dexec.mainClass=de.qaware.sdfx.examplegui.ExampleCDIMain
```

## Project Structure

| Module | Description |
|--------|-------------|
| `lookup` | Core lookup/service locator API |
| `lookup-cdi` | CDI-based lookup implementation |
| `platform-api` | Platform API interfaces |
| `platform-core` | Platform core implementation |
| `platform-runner` | Application launcher and lifecycle management |
| `windowmanager-api` | Window management API |
| `windowmanager-core` | Window management implementation |
| `extensions/cdi-contexts` | CDI context extensions (ViewScoped, etc.) |
| `extensions/ui-utils` | UI utility classes |
| `stagediverfx-all` | All-in-One JAR with all modules bundled |
| `example-gui` | Example application demonstrating the framework |

## Maven Dependency

The easiest way to use WFX is to add the `stagediverfx-all` dependency which includes all modules and their required runtime dependencies:

```xml
<dependency>
    <groupId>de.qaware.stagediver.fx</groupId>
    <artifactId>stagediverfx-all</artifactId>
    <version>1.4.0-SNAPSHOT</version>
</dependency>
```

This single dependency provides:
- All WFX modules bundled in one JAR
- CDI API and Weld SE implementation
- JavaFX FXML and Controls
- SLF4J logging API
- Logback as optional logging implementation
- Apache Commons Lang3

If you prefer to pick individual modules, you can also add them separately (e.g., `lookup`, `platform-runner`, `windowmanager-core`).

## Creating a Simple Application

### 1. Create a Main Class

```java
import de.qaware.sdfx.main.Main;
import javafx.application.Application;

public class MyApp extends Main {
    public static void main(String[] args) {
        Application.launch(MyApp.class, args);
    }
}
```

### 2. Create a Module

```java
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.api.FXMLViewBuilder;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.lookup.Lookup;

public class MyModule implements Module {
    
    @Override
    public void preload() {
        WindowManager wm = Lookup.lookup(WindowManager.class);
        wm.registerView(FXMLViewBuilder.create()
            .id("my-view")
            .title("My View")
            .fxml(getClass().getResource("/my-view.fxml"))
            .position(Position.CENTER)
            .build());
    }
    
    @Override
    public void start() {
        // Called after preload phase
    }
    
    @Override
    public void stop() {
        // Called on application shutdown
    }
}
```

### 3. Register the Module

Create `META-INF/services/de.qaware.sdfx.platform.api.Module`:
```
com.example.MyModule
```

## CDI Support

For CDI-based dependency injection, extend `CDIMain` instead:

```java
import de.qaware.sdfx.main.CDIMain;
import javafx.application.Application;

public class MyCDIApp extends CDIMain {
    public static void main(String[] args) {
        Application.launch(MyCDIApp.class, args);
    }
}
```

See [CDI Contexts README](extensions/cdi-contexts/README.md) for more details on CDI integration.

## Dependencies

The framework uses:
- **Weld SE 5.1.x** - CDI implementation (Jakarta EE 10)
- **SLF4J 2.x** + **Logback 1.5.x** - Logging
- **JavaFX 21** - UI framework

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](LICENSE.txt) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## History

This project was originally developed as "stagediver.fx" at QAware GmbH and has been modernized to support Java 17+ and Jakarta EE 10.
