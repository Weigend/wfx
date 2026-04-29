# Contributing to WFX

Thanks for considering a contribution. WFX is a small framework with a
narrow scope; the bar for changes is "does this make a real JavaFX RCP
application clearer or smaller?".

## Reporting issues

- Use [GitHub Issues](https://github.com/jweigend/wfx/issues).
- Include the JDK and JavaFX version you are using.
- A minimal reproducer (a single FXML view, a couple of lines of
  controller code) is much more useful than a description of a large app.

## Building

```bash
mvn clean install -DskipTests
```

To run the example application:

```bash
mvn -pl wfx-modules/example-gui exec:java
```

Requirements: Java 21+ and JavaFX 21+.

## Pull requests

- Branch from `main`.
- One topic per pull request. Mixed-purpose PRs are hard to review.
- Keep commits small and tell a story in the messages — the *why* matters
  more than the *what*; the diff already shows the *what*.
- Run the build (`mvn install`) before opening the PR.
- New code carries the Apache 2.0 header that the rest of the codebase uses.
- Public API changes need a short note in the README or a docstring update.

## Code style

- Java 21 features are fine.
- No reflection magic at framework boundaries; if a behaviour can be made
  explicit, prefer that over annotation processing or runtime introspection.
- DI is for long-lived infrastructure (see [README → DI Boundary](README.md)).
  Views and per-load controllers are explicit runtime objects.

## License

By submitting a contribution you agree that your work is licensed under
the [Apache License, Version 2.0](LICENSE).
