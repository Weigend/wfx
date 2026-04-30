Confirmed: kein View-Lifecycle, kein Framework-Level-Selection, kein Workspace-Konzept. Hier mein priorisierter Plan:

Tier 1 — Macht "Structure 101 Light" überhaupt erst möglich
1. Selection-Service als wfx-Lookup-Bean
Sobald View #2 kommt (Quality-Spinne), brauchst du Cross-View-Selection-Sync ("User hat Klasse X selektiert → alle Views reagieren"). Ohne das fühlt sich die UI fragmentiert an.


public interface SelectionService {
    ReadOnlyObjectProperty<Object> current();
    <T> Optional<T> currentAs(Class<T> type);
    void select(Object value, Object source);  // source vermeidet Echo
    <T> void subscribe(Class<T> type, Consumer<T> listener);
}
Ein @Singleton SelectionServiceImpl mit eigenem Event-Typ auf dem EventBus. Views publizieren ihre Selection ("ClassSelectedEvent", "PackageSelectedEvent"); andere Views abonnieren typisiert. Das ersetzt unsere statischen LevelClassBox.setOnSelectionChangeCallback-Workarounds und ist die Grundlage für Cross-View-Sync zwischen Architektur, Spinne, Refactoring, DSM.

2. Action-Framework
Das Refactoring-Tool wird viele Aktionen haben ("Move to Package X", "Extract Interface", "Rename Package"), die in Menü, Toolbar, View-Kontext-Menü und Tastatur-Shortcut gleichzeitig auftauchen. Ohne Action-Konzept duplizierst du die Verdrahtung 4×.


public abstract class Action {
    public abstract String getId();
    public abstract StringProperty textProperty();
    public abstract Ikon getIcon();
    public abstract BooleanProperty enabledProperty();    // bindet sich an SelectionService
    public abstract void run();
}

// Verdrahtung:
ActionRegistry.register(new RefactorMoveAction(...));
MenuUtil.addItem("refactor", "refactor.move", actionRegistry.get("refactor.move"));
ToolbarUtil.addButton(actionRegistry.get("refactor.move"));
Das ist das Muster aus NetBeans/Eclipse. Eine Action ist ein Singleton-Bean, das enabledProperty() an SelectionService bindet. Menü/Toolbar/Kontextmenü ziehen sich's aus der Registry. Diese Investition zahlt sich ab Action #5 vielfach aus, und du kommst leicht auf 30–50 Actions in einer ausgewachsenen Workbench.

3. Workspace / Document-Model
Aktuell hält S202Module rawAnalyzer, levelCalculator etc. als private Felder. Sobald 3 Views auf "die geladene Analyse" zugreifen, reicht das nicht.


public interface Workspace {
    <T> ObjectProperty<T> documentProperty(Class<T> type);
    <T> void put(Class<T> type, T value);
    <T> Optional<T> get(Class<T> type);
}
Ein @Singleton WorkspaceImpl als typisierter Document-Holder. S202Module.loadJarFiles() ruft workspace.put(DomainModel.class, model). Quality-Spinne und Refactoring-View binden sich an workspace.documentProperty(DomainModel.class) und reagieren automatisch auf neue Modelle. Klassische "Single Source of Truth"-Pattern.

Tier 2 — Spart in jedem Modul Boilerplate
4. View-Lifecycle-Hooks
Aktuell: keine sauberen Hooks für "Tab wurde geöffnet/geschlossen". Wir hängen EventBus.subscribe(...) in installStatusBar() und haben keine Cleanup-Stelle. Bei langlebigen Apps mit häufig öffnenden/schließenden Views wird das zum Memory-Leak.


public interface View {
    // existing methods...
    default void onAttach()  {}   // wird gerufen wenn der Tab erstmals sichtbar wird
    default void onDetach()  {}   // wird gerufen wenn unregister
}
Plus: EventBus.subscribeFor(View.class, ...) mit auto-unsubscribe bei View-detach.

5. Programmatic-View-Builder
Heute musst du für nicht-FXML-Views (wie unser ArchitectureWfxView) das View-Interface manuell implementieren — 6 Methoden Boilerplate pro View. Mit Builder gleicher Form wie FXMLView.Builder:


View v = View.builder()
    .id("s202-architecture-1")
    .title("Architecture 1")
    .pos(Position.CENTER)
    .rootNode(architectureView)
    .build();
Ein Dreizeiler statt 60-Zeilen-Klasse. (Wir hätten ArchitectureWfxView.java komplett gespart.)

6. Stylesheet-Hook am ApplicationWindow
Aktuell mussten wir Scene + Parent zweifach attachen, um CSS-Cascade-Probleme zu umgehen. Ein


applicationWindow.installStylesheet(URL);
das das Scene-Attachment korrekt macht (auch beim ApplicationWindow-Reinit), spart jedem Konsumenten dieses Lehrgeld.

7. Background-Task-Service
Wir hantieren in S202Module direkt mit Task<>, Thread, Daemon-Setup, setOnSucceeded-Verdrahtung mit publishProgress. Ein Service:


public interface BackgroundExecutor {
    <T> void run(Task<T> task, Consumer<T> onSuccess, Consumer<Throwable> onFailure);
}
mit auto-Progress-Eventbus, daemon-pool, optional cancellation. Spart in jedem Modul ~30 Zeilen Threading-Glue.

Tier 3 — Polish & Robustheit
8. Defensive Null-Checks
ViewFocusHandler.registerRootArea (Zeile 87) hat den NPE, den wir in unserem Stack-Trace gesehen haben. if (rootArea == null) return; löst das.
WindowManagerImpl.restoreDefaultLayout() setzt mainRootArea = null und triggert dadurch obigen NPE in seinem eigenen Listener. Race-Condition-fest machen.
9. AbstractModule-Basisklasse
80% aller Module-Implementierungen haben identisches getName()/getVersion()-Boilerplate. Eine Default-Klasse mit Konstruktor AbstractModule(String name) spart 20 Zeilen pro Modul.

10. WindowManager Convenience
unregisterAll() als API-Methode (statt new ArrayList<>(getVisibleViews()).forEach(unregister))
register(view, makeFocused: boolean) — manchmal will man einen Tab im Hintergrund öffnen
11. Splash-Builder

SplashConfig.builder()
    .title("My App")
    .subtitle("...")
    .image(getClass().getResource("logo.png"))
    .background(Color.web("#2b3e50"))
    .build();
Die Mechanik via /splash/splash.fxml-Override funktioniert, ist aber unbequem. Ein Builder, der das FXML programmatisch erzeugt, wäre user-friendly.

12. Doku-Lücken im README
"Replacing the built-in StatusBarProgress" (das mussten wir per Source-Reading rausfinden)
"Custom splash screen" (existiert nicht, nur durch Source-Reading entdeckt)
"Programmatic Views without FXML" (das Pattern hinter ArchitectureWfxView)
Lifecycle-Diagramm (init → preload → start → stop) als Bild
Cross-Module-Communication-Beispiel über EventBus
Empfehlung: was zuerst?
Wenn du eine Sache baust → Selection-Service (#1). Ohne den wirst du beim ersten Cross-View-Use-Case sofort hineinlaufen, und nachträglich einbauen ist viel teurer.

Wenn du drei baust → Selection + Workspace + Action-Framework (#1, #2, #3). Das ist das Trio, das eine "Workbench" von einer "App mit Tabs" unterscheidet, und das brauchst du für Struktur101-Light bevor du View #3 anfängst.

Tier 2 sind Convenience-Verbesserungen — die kann man inkrementell beim ersten Schmerzpunkt einbauen.

Falls du willst: ich kann gleich ein PR für #1 (Selection-Service) und/oder #4 (View-Lifecycle-Hooks) machen — die sind kompakt und haben sofortigen Nutzen, sobald du mit der Quality-Spinne anfängst. Sag Bescheid, was am ehesten ansteht.