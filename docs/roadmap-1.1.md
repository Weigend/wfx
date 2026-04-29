# WFX Roadmap — 1.0.1 / 1.1.0 / 1.2.0

Dieses Dokument plant die nächsten WFX-Releases auf Basis des Integrations-
Feedbacks aus [planing-1.1.md](planing-1.1.md). Quelle des Feedbacks: realer
Einbau von WFX in eine kleine JavaFX-Workbench ("Structure 101 Light").

## Leitlinien

- **Additiv vor breaking.** 1.1 fügt API hinzu, ändert keine. Erst 2.0
  würde z. B. `View` umstrukturieren.
- **Bug-Fixes nicht mit Features mischen.** Stack-Trace-NPEs gehören in
  einen 1.0.1-Patch, nicht in 1.1.
- **Tier-1 ≠ "alles auf einmal".** Selection und Workspace tragen 1.1, das
  Action-Framework wird in 1.2 separat geschnitten — es hat alleine die
  Reichweite eines eigenen Releases (Registry, Menü/Toolbar/Kontext-Adapter,
  Shortcut-Binding, Enabled-State-Bindings).

## Modul-Schnitt

Neues Modul für die in 1.1 hinzukommenden Singleton-Services:

```
wfx-modules/services/
  src/main/java/io/softwareecg/wfx/services/
    selection/        SelectionService + Impl
    workspace/        Workspace + Impl
    background/       BackgroundExecutor + Impl
    events/           SelectionChangedEvent, DocumentChangedEvent, …
```

Begründung: Diese Services sind weder Plattform-Kern (`platform-api/core`
trägt Module-Lifecycle, EventBus, Lookup) noch Window-Management. Eine
eigene Schicht hält die Abhängigkeit klar:
`services` → `platform-api` + `windowmanager-api`. Keine Rückrichtung.

Weitere Änderungen landen in bestehenden Modulen:

| Thema                         | Modul                                         |
|-------------------------------|-----------------------------------------------|
| `View`-Lifecycle-Hooks        | `windowmanager-api` (default-Methoden)        |
| `View.builder()` (programmatisch) | `windowmanager-api` (`SimpleView.Builder`) |
| `installStylesheet`           | `windowmanager-api` + `-core`                 |
| `unregisterAll`, `register(view, makeFocused)` | `windowmanager-api` + `-core`|
| `AbstractModule`              | `platform-api`                                |
| `SplashConfig.builder()`      | `platform-core`                               |

Das Action-Framework (1.2) bekommt sein eigenes Modul
`wfx-modules/extensions/actions` neben dem bestehenden
`extensions/ui-utils`, das dann als Konsument `MenuUtil`/`ToolBarUtils`
um Action-Adapter erweitert.

## Branching-Strategie

| Branch                | Zweck                                          | Merge-Ziel |
|-----------------------|------------------------------------------------|------------|
| `main`                | aktueller Stand 1.0.0 (+ unveröffentlichte Commits) | —      |
| `release/1.0.1`       | nur Bug-Fixes und Doku-Lücken                  | `main`     |
| `feature/1.1.0`       | additive API für die "Workbench-Tier"          | `main` (nach 1.0.1) |
| `feature/1.2.0-actions` | Action-Framework                             | `main` (nach 1.1)   |

Reihenfolge: 1.0.1 → tag `v1.0.1` → 1.1.0 → tag `v1.1.0` → 1.2.0 → tag `v1.2.0`.

## 1.0.1 — Patch (klein, unblockend)

**Zweck**: Bugs, die im Stack-Trace des Integrationsversuchs aufgetaucht
sind, und die Doku-Lücken, die nur per Source-Reading zu lösen waren.

### Bug-Fixes

1. **NPE in `ViewFocusHandler.registerRootArea`**
   ([ViewFocusHandler.java:86](../wfx-modules/windowmanager-core/src/main/java/io/softwareecg/wfx/windowmtg/impl/ViewFocusHandler.java))
   Null-Guard für `rootArea` ergänzen.

2. **Race in `WindowManagerImpl.restoreDefaultLayout`**
   ([WindowManagerImpl.java:217](../wfx-modules/windowmanager-core/src/main/java/io/softwareecg/wfx/windowmtg/impl/WindowManagerImpl.java))
   `mainRootArea.set(null)` triggert den oben genannten NPE im eigenen
   Listener. Reihenfolge bzw. Atomarität so anpassen, dass der Listener
   keinen `null`-Zwischenzustand sieht.

### Doku

3. README ergänzen um:
   - "Replacing the built-in StatusBarProgress"
   - "Custom splash screen" (heute nur via FXML-Override im Klassenpfad)
   - "Programmatic View ohne FXML"
   - Lifecycle-Diagramm (`init` → `preload` → `start` → `stop`) als Bild
   - Cross-Module-EventBus-Beispiel

**Aufwand**: ~1 PT. **Risiko**: niedrig — additive Doku, defensive Null-
Checks.

## 1.1.0 — Additive Workbench-API

**Zweck**: Mehrfach-View-Apps werden ohne Eigenbau-Kram möglich. Trio aus
`SelectionService` + `Workspace` + `View`-Lifecycle ist der Kern.

### Neue Services (Modul `wfx-modules/services`)

4. **`SelectionService`** — Cross-View-Selection-Sync.
   ```java
   public interface SelectionService {
       ReadOnlyObjectProperty<Object> current();
       <T> Optional<T> currentAs(Class<T> type);
       void select(Object value, Object source);   // source vermeidet Echo
       <T> void subscribe(Class<T> type, Consumer<T> listener);
   }
   ```
   `@Singleton`-Impl publiziert intern auf dem `EventBus`. Ersetzt
   statische Callback-Workarounds wie `LevelClassBox.setOnSelectionChange`.

5. **`Workspace`** — typisierter Document-Holder.
   ```java
   public interface Workspace {
       <T> ObjectProperty<T> documentProperty(Class<T> type);
       <T> void put(Class<T> type, T value);
       <T> Optional<T> get(Class<T> type);
   }
   ```
   Single Source of Truth. Views binden sich an
   `workspace.documentProperty(DomainModel.class)` und reagieren auto-
   matisch auf neue Modelle.

6. **`BackgroundExecutor`** — Threading-Glue weg aus jedem Modul.
   ```java
   public interface BackgroundExecutor {
       <T> void run(Task<T> task,
                    Consumer<T> onSuccess,
                    Consumer<Throwable> onFailure);
   }
   ```
   Auto-Progress auf den EventBus, Daemon-Pool, optional Cancellation.

### Window-Management-Erweiterungen

7. **View-Lifecycle-Hooks** in `View`:
   ```java
   default void onAttach() {}
   default void onDetach() {}
   ```
   Aufruf-Punkte in `WindowManagerImpl.register` / `unregister`.
   Plus `EventBus.subscribeFor(View.class, …)` mit Auto-Unsubscribe bei
   Detach (gegen Memory-Leaks bei langlaufenden Apps).

8. **`SimpleView.Builder`** — programmatische Views ohne FXML-Boilerplate.
   ```java
   View v = SimpleView.builder()
           .id("my-view")
           .title("My View")
           .pos(Position.CENTER)
           .rootNode(myParent)
           .build();
   ```
   Spart die übliche 60-Zeilen-Klasse mit den 6 `View`-Interface-Methoden.

9. **`ApplicationWindow.installStylesheet(URL)`** — kapselt das doppelte
   Scene/Parent-Attachment, damit Konsumenten kein Lehrgeld mehr zahlen.

10. **`WindowManager.unregisterAll()`** und
    `register(View, boolean makeFocused)` — Convenience für "alles
    schließen" und "Tab im Hintergrund öffnen".

### Plattform-Kern

11. **`AbstractModule`** — Konstruktor-Boilerplate für `name`/`version`
    sparen (Default-Methoden im Interface decken Reflection ab,
    `AbstractModule(String name)` ist der bewusste Override).

12. **`SplashConfig.builder()`** — programmatischer Splash, der
    intern das FXML erzeugt.
    ```java
    SplashConfig.builder()
            .title("My App")
            .subtitle("…")
            .image(getClass().getResource("logo.png"))
            .background(Color.web("#2b3e50"))
            .build();
    ```

**Aufwand**: ~5–8 PT. **Risiko**: niedrig — alles additiv, keine
Bestandsaufrufe ändern.

## 1.2.0 — Action-Framework

**Zweck**: Ein Action ist Singleton-Bean, das `enabledProperty()` an
den `SelectionService` (1.1) bindet. Menü, Toolbar und Kontextmenü
ziehen die Action aus der Registry — keine 4-fache Verdrahtung mehr.

```java
public abstract class Action {
    public abstract String getId();
    public abstract StringProperty textProperty();
    public abstract Ikon getIcon();
    public abstract BooleanProperty enabledProperty();
    public abstract void run();
}

ActionRegistry.register(new RefactorMoveAction(...));
MenuUtil.addItem("refactor", "refactor.move",
                 actionRegistry.get("refactor.move"));
ToolbarUtil.addButton(actionRegistry.get("refactor.move"));
```

Bewusst von 1.1 getrennt: das Vollbild umfasst `Action`,
`ActionRegistry`, Menü/Toolbar/Kontextmenü-Adapter,
Keyboard-Shortcut-Bindings, Enabled-State-Bindings — Reichweite eines
eigenen Minor-Releases.

**Trigger**: sobald die erste konsumierende App > 5 Actions braucht
(real: das Refactoring-Tool aus dem Structure-101-Light-Showcase).

**Aufwand**: ~5–10 PT. **Risiko**: mittel — `MenuUtil`/`ToolBarUtils`
bekommen neue Konsumenten-Wege.

## Reihenfolge & Sequencing

1. Branch `release/1.0.1` von `main` abzweigen.
2. 1.0.1-Bugs + Doku → PR → merge `main` → tag `v1.0.1`.
3. Branch `feature/1.1.0` von `main` (nach 1.0.1).
4. Modul `wfx-modules/services` anlegen, Aggregator `wfx-all` ergänzen.
5. `SelectionService`, `Workspace`, `BackgroundExecutor` (in dieser
   Reihenfolge — `BackgroundExecutor` ist unabhängig und kann parallel).
6. `View`-Lifecycle-Hooks + `EventBus.subscribeFor`.
7. `SimpleView.Builder`, `installStylesheet`, `unregisterAll`,
   `register(view, focused)`.
8. `AbstractModule`, `SplashConfig.builder()`.
9. `example-gui` um Demo-View erweitern, die das Trio benutzt
   (sonst entdecken wir API-Schnitzer erst beim Konsumenten).
10. README-Abschnitt "1.1 Quickstart: Selection + Workspace + Lifecycle".
11. PR → merge `main` → tag `v1.1.0`.
12. Branch `feature/1.2.0-actions`, sobald Bedarf konkret ist.

## Offene Punkte vor Start

- Naming-Check: heißt der Builder `SimpleView.Builder` oder
  `ProgrammaticView.Builder`? Letzteres beschreibt den Zweck genauer.
- Soll `BackgroundExecutor` in 1.1 oder erst in 1.2 — entscheidet sich
  daran, ob ein Konsument ihn direkt mitbenutzen will. Default-Annahme:
  ja, in 1.1.
- Avaje-Scope der neuen Services: `@Singleton` für alle drei
  (siehe Avaje-Scope-Regeln im Repo).
