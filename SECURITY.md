# Security Policy

## Reporting a Vulnerability

If you believe you have found a security issue in WFX, please **do not**
open a public GitHub issue.

Instead, send an email to **johannes.weigend@weigend.de** with:

- a description of the vulnerability,
- a minimal reproducer (or a clear set of steps),
- the WFX version (commit SHA or tag) you tested against,
- the JDK and JavaFX versions you used.

You will get a response within 7 days. We will then coordinate a fix and a
public disclosure timeline with you.

## Scope

WFX is a desktop UI framework. Common vulnerability classes that apply:

- FXML loading attacks (controlling the controller class on the classpath)
- DI bean discovery on classpath manipulation
- Resource handling in `Lookup`/`ServiceLoader` paths

Out of scope:

- Bugs in third-party dependencies (Avaje Inject, JavaFX, SLF4J, …) —
  please report those to the respective projects directly.
- Issues that require the attacker to already control the JVM
  command line, the classpath, or the application's own JAR files.

## Supported versions

WFX is in active development; only the current `main` branch receives
fixes. There are no long-lived release branches yet.
