wfx-Erweiterung: View-Menü + Default-Layout-Klassifizierung
Motivation
Jede RCP-Anwendung braucht (a) ein Menü, mit dem User geschlossene Standardpanels wieder öffnen können, und (b) einen "Default Layout"-Reset, der dynamisch erzeugte Tabs schließt aber Standardpanels zurückbringt. Heute kann das jeder Anwender per findOrCreateItem("view") → "Windows" → MenuItem(showView(...)) selbst zusammenstückeln (LoggerConsoleModule macht's so) — Boilerplate, das in die Plattform gehört.

1. View-Interface
Neue Default-Methode:


/**
 * Whether this view is part of the application's default layout.
 *
 * <p>True (the default): the view is registered by a module at startup,
 * appears in the auto-built View menu, and is re-registered when the user
 * triggers "Restore Default Layout".
 *
 * <p>False: the view is dynamic (per-document, per-task, per-JAR …).
 * Closing or omitting it on layout reset is intended.
 */
default boolean isPartOfDefaultLayout() {
    return true;
}
Begründung A (Methode am View) statt B (Flag am register(...)): Die Klassifizierung ist eine Eigenschaft der View-Klasse, nicht des Aufrufkontexts. Ein und dieselbe View-Instanz wird nicht mal "default", mal "dynamisch" registriert. Default true ⇒ bestehende wfx-Apps verhalten sich nach dem Upgrade ohne Codeänderung wie erwartet.

2. WindowManagerImpl
Neues Feld

/** Insertion-ordered registry of views that belong to the application's
 *  default layout. Populated on first register; never cleared by close/unregister. */
private final LinkedHashMap<String, ViewStatus> defaultLayoutViews = new LinkedHashMap<>();

/** Observable view of the same data for menu builders (sortiert nach
 *  Insertion-Order). */
private final ReadOnlyListWrapper<View> defaultLayoutViewsList =
        new ReadOnlyListWrapper<>(this, "defaultLayoutViews",
                FXCollections.observableArrayList());
Patch in register(ViewStatus, boolean, ViewArea, Position)
Direkt nach views.add(...):


if (viewStatus.getView().isPartOfDefaultLayout()) {
    if (defaultLayoutViews.putIfAbsent(viewStatus.getView().getViewId(), viewStatus) == null) {
        defaultLayoutViewsList.add(viewStatus.getView());
    }
}
putIfAbsent ⇒ Erstregistrierung gewinnt, spätere Re-Register (während restoreDefaultLayout) sind no-ops.

Patch in unregister / closeView
Nichts ändern. defaultLayoutViews bleibt absichtlich befüllt.

Patch in restoreDefaultLayout()

public void restoreDefaultLayout() {
    // Snapshot first so the teardown below can mutate viewsStatus freely.
    List<ViewStatus> snapshot = new ArrayList<>(defaultLayoutViews.values());

    // Existing teardown (subwindows, viewsStatus, views, mainRootArea reset).
    List<RootArea> currentSubwindows = new ArrayList<>(subWindows);
    currentSubwindows.forEach(this::remove);
    rootPane.getChildren().clear();
    viewsStatus.clear();
    views.clear();
    mainRootArea.set(new RootArea(rootPane, dragNDropManager, false));

    restoringLayout = true;
    try {
        for (ViewStatus vs : snapshot) {
            vs.restoreDefault();
            if (vs.getParent() == null) {
                register(vs.getView());
            } else {
                register(vs.getView(), vs.getParent().getView());
            }
        }
    } finally {
        restoringLayout = false;
    }
    setDividerPositions();
}
Effekt: alles aktuell Sichtbare verschwindet (inkl. dynamischer Tabs), die Default-Layout-Liste wird in Originalreihenfolge mit Originalposition / -parent neu aufgesetzt.

Neues API

/** Read-only list of views registered as part of the default layout,
 *  in insertion order. Stable across close/reopen — useful for auto-built
 *  View menus. */
ReadOnlyListProperty<View> getDefaultLayoutViews();
3. Auto-View-Menü
Neues wfx-Modul, z.B. in wfx-modules/extensions/view-menu/:


@Singleton
@Priority(50)  // Nach den Modulen, die eigene Default-Layout-Views registrieren.
public class ViewMenuModule implements Module {

    @Override
    public void start() {
        ApplicationWindow appWindow = Lookup.lookup(ApplicationWindow.class);
        WindowManager wm = Lookup.lookup(WindowManager.class);

        Menu viewMenu = MenuUtil.findOrCreateItem(
                appWindow.getMenu(), "view", () -> new Menu("View"), 1);

        rebuildItems(viewMenu, wm);
        wm.getDefaultLayoutViews().addListener(
                (ListChangeListener<View>) c -> rebuildItems(viewMenu, wm));
    }

    private void rebuildItems(Menu viewMenu, WindowManager wm) {
        viewMenu.getItems().clear();
        for (View v : wm.getDefaultLayoutViews()) {
            MenuItem item = MenuUtil.createMenuItem(
                    "view." + v.getViewId(),
                    v.getTitle(),
                    e -> wm.showView(v));   // bestehender showView re-registriert + fokussiert
            viewMenu.getItems().add(item);
        }
    }
}
showView(View) ist heute schon idempotent: registriert wieder, falls geschlossen, sonst Fokus.

4. Migration / Aufräumen
LoggerConsoleModule.start(): das manuelle Aufbauen des View → Windows → Logger Console-Items kann ersatzlos entfallen, sobald view-menu-Modul aktiv ist.
Andere wfx-Apps, die heute eine eigene Default-Layout-Logik haben, können sukzessive umstellen — nichts bricht, weil isPartOfDefaultLayout() defaultmäßig true zurückgibt.
In Structure202 wird:

ArchitectureWfxView.isPartOfDefaultLayout() → false überschrieben (jede tab-View ist dynamisch).
Outline + Quality erben den Default true.
Der jetzige Help-Eintrag „Default Layout" in S202MenuBar kann bleiben oder entfernt werden — wfx baut das View-Menü dann selbst.
5. Tests
In WindowManagerImplTest:


@Test
void restoreDefaultLayout_reopensClosedStandardView() {
    View a = TestView.standard("a");
    View b = TestView.standard("b");
    wm.register(a);
    wm.register(b);
    wm.closeView(a);

    wm.restoreDefaultLayout();

    assertThat(wm.findView("a")).isNotNull();
    assertThat(wm.findView("b")).isNotNull();
}

@Test
void restoreDefaultLayout_closesDynamicViews() {
    View standard  = TestView.standard("std");
    View dynamic   = TestView.dynamic("dyn");
    wm.register(standard);
    wm.register(dynamic);

    wm.restoreDefaultLayout();

    assertThat(wm.findView("std")).isNotNull();
    assertThat(wm.findView("dyn")).isNull();
}

@Test
void getDefaultLayoutViews_orderMatchesRegistrationOrder() { … }

@Test
void register_sameViewTwice_doesNotDuplicateInDefaultLayout() { … }

@Test
void parentRelationshipPreservedAcrossDefaultLayoutRestore() {
    // Outline LEFT, Quality with parent=outline (BOTTOM).
    // close beide, restoreDefaultLayout → quality muss wieder unter outline landen.
}
In ViewMenuModuleTest:

Register zwei Default-Layout-Views → Menü hat zwei Items in Insertion-Reihenfolge.
Close eine View → Menü-Items unverändert.
Klick auf das geschlossene Item → View ist wieder sichtbar.
Register eine dynamische View → keine Menü-Änderung.
6. Edge Cases / nicht in v1
Multi-RootArea (detached Subwindows): Default-Layout-Restore klappt nur in mainRootArea. Falls eine Default-Layout-View in einen Subwindow gezogen war, landet sie nach Restore wieder im Main. Akzeptabel.
Persistente Layouts (User-eigene gespeicherte Layouts) — separates Feature.
Menü-Sortierung jenseits Insertion-Order (alphabetisch, gruppiert) — separates Feature.