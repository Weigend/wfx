# Warum eine 13 Jahre alte JavaFX-Anwendung kein CDI mehr braucht

## Eine Migrationsgeschichte aus dem WFX-RCP-Framework

Johannes Weigend, April 2026

Als wir vor mehr als 13 Jahren begannen, eine große JavaFX-Anwendung zu bauen,
war die Welt der Rich-Client-Anwendungen in Java noch eine andere. Wer eine
professionelle Desktop-Anwendung mit Modulen, Menüs, Docking, Views,
Hintergrundinitialisierung und sauberem Fenstermanagement bauen wollte, schaute
auf zwei Plattformen: Eclipse RCP und NetBeans RCP.

Beide waren mächtig. Beide waren auch schwergewichtig. Eclipse brachte SWT und
OSGi mit. NetBeans basierte auf Swing. Für viele Anwendungen war das völlig in
Ordnung. Für unsere nicht.

Wir wollten JavaFX nutzen. Nicht als Spielerei, sondern als Grundlage für eine
große fachliche Anwendung. Ein wichtiger Grund war Visualisierung: Wir brauchten
gute Charts, gute UI-Komponenten und eine Oberfläche, die nicht wie ein Relikt
aus der Swing-Zeit wirkt. JavaFX brachte dafür eine gute Basis mit. Was JavaFX
nicht mitbrachte, war der Rest einer RCP-Plattform.

Also bauten wir ihn selbst.

Damals hieß das Projekt Stagediver.fx — entwickelt bei QAware, Apache 2.0.
Heute heißt der Kern WFX. Es ist ein
leichtgewichtiges JavaFX-RCP-Framework mit Dingen, die man in einer großen
Desktop-Anwendung irgendwann braucht: Docking, Tabbed Views, FXML-Integration,
Fensterlayout, Menü- und Toolbar-Erweiterungen, Preloader, Modul-Discovery,
Eventbus und Drag-and-Drop-Unterstützung.

Das Ziel war nie, Eclipse oder NetBeans vollständig nachzubauen. Das Ziel war
kleiner und pragmatischer: die Teile zu bauen, die eine große JavaFX-Anwendung
wirklich braucht, ohne dafür eine komplette Enterprise-Plattform in den Desktop
zu ziehen.

Natürlich haben wir es am Anfang trotzdem getan.

## Die naheliegende Entscheidung von damals

Die erste Version orientierte sich stark an den großen RCP-Vorbildern. Wir
starteten mit OSGi und Apache Felix. Später wechselten wir auf CDI mit Weld.

Aus heutiger Sicht klingt das schwergewichtig. Damals war es plausibel.

Java EE war etabliert. CDI war der Standard für Dependency Injection in der
Java-Welt. Man bekam Scopes, Injection, Producer, Qualifier, Events und
Extension Points in einem Modell. Wer 2013 eine modulare Java-Anwendung baute
und nicht alles selbst erfinden wollte, landete schnell bei OSGi oder CDI.

Außerdem wollten wir uns nicht zu weit von den bekannten RCP-Ideen entfernen.
Eclipse und NetBeans waren containergetriebene Plattformen. Wenn man in diese
Richtung dachte, war es nicht abwegig, auch für JavaFX ein containergetriebenes
Design zu wählen.

Das funktionierte. Lange sogar ziemlich gut.

Aber funktionierende Architektur ist nicht automatisch gute Architektur für
immer.

## Was sich über die Jahre verändert hat

Eine Desktop-Anwendung hat einen anderen Lebenszyklus als eine
Server-Anwendung.

Auf dem Server ist ein Containerstart von ein paar Sekunden oft akzeptabel. Ein
Service startet, bleibt lange aktiv und verarbeitet dann Requests. In einer
Desktop-Anwendung ist der Start sichtbarer. Ein Benutzer klickt auf ein Icon und
wartet. Ein Entwickler startet die Anwendung viele Male am Tag, um einen Fehler
zu reproduzieren. Jede Sekunde Startup-Zeit wird spürbar.

Noch wichtiger: Eine JavaFX-Anwendung hat einen sehr klaren UI-Lifecycle.
Bestimmte Dinge müssen auf dem JavaFX Application Thread passieren. Andere
Dinge dürfen dort gerade nicht passieren. Ein Preloader soll früh erscheinen,
damit der Benutzer sieht, dass etwas passiert. Teure Initialisierung sollte
danach laufen, idealerweise mit Fortschritt.

Ein allgemeiner DI-Container sieht diese Grenze nicht automatisch. Er baut
Beans, scannt den Classpath, wertet Metadaten aus, erzeugt Proxies und ruft
Lifecycle-Hooks auf. Das kann alles korrekt sein und trotzdem zum falschen
Zeitpunkt passieren.

Bei uns wurde genau das sichtbar.

Während der Migration fiel auf, dass ein Teil der Anwendung bereits vor dem WFX
Preloader initialisierte. Nicht, weil JavaFX das verlangte. Sondern weil sich
über Jahre Startup-Code, CDI-Scopes, Producer und UI-Bootstrapping vermischt
hatten. Manche Klassen wurden Singletons, weil der Container sie finden sollte.
Manche Producer erzeugten Objekte, deren echter Lifecycle eigentlich an ein
Fenster gebunden war. Manche Initialisierung lief in Konstruktoren oder
`@PostConstruct`, obwohl sie fachlich besser in einen sichtbaren
Startup-Schritt gehört hätte.

Konkret hieß das in unserer Anwendung: Das eingebettete Solr öffnete seinen
Index in `CoreContainer.createAndLoad(...)`, sobald der Container die
Bean-Kette auflöste — also während JavaFX `init()`. Die H2-Metadaten-Datenbank
tat dasselbe in einem `@PostConstruct`. Und ein eigener Bootstrapper rief
`stage.show()` auf, bevor WFX überhaupt `start(...)` gesehen hatte. Drei
Container-getriebene Initialisierungen, alle vor dem Preloader. Architektonisch
hatte der DI-Container den UI-Lifecycle überschrieben.

Das ist kein ungewöhnlicher Fehler. Es ist eher der Normalfall in langlebigen
Systemen: Eine Architekturentscheidung, die anfangs Ordnung schafft, wird über
Jahre zum Ort, an dem immer mehr Sonderfälle abgelegt werden.

## CDI war nicht das Problem. Aber es passte nicht mehr gut.

CDI ist ein gutes Modell für viele Arten von Java-Anwendungen. Es ist aber kein
spezielles Modell für JavaFX-Desktop-Anwendungen.

In unserer Anwendung störten vor allem vier Dinge.

Erstens: Runtime Discovery. Der Container muss zur Laufzeit herausfinden, welche
Beans es gibt und wie sie zusammenhängen. Das ist flexibel, aber für eine
Desktop-Anwendung oft zu teuer und zu spät. Viele Fehler erscheinen erst beim
Start.

Zweitens: Scopes, die nicht zur UI passen. `@ApplicationScoped` klingt
harmlos, aber in einer Desktop-Anwendung ist nicht alles, was technisch global
erreichbar sein soll, auch fachlich ein Anwendungssingleton. Ein Fenster, ein
FXML-Controller, ein Tree Item oder ein Task haben oft einen konkreten
UI-Kontext.

Drittens: Producer und Spezialisierungen. CDI macht es leicht, über
`@Produces`, `@Specializes`, `@Default` und Qualifier sehr viel indirekte
Verdrahtung aufzubauen. Das ist mächtig. In einer über Jahre gewachsenen
Anwendung wird es aber schnell schwer zu beantworten: Wer erzeugt dieses Objekt
eigentlich? Wann? Und warum genau diese Implementierung?

Viertens: Der Startup-Lifecycle. Für WFX ist die Regel einfach: zuerst die
Plattform initialisieren, dann den Preloader zeigen, dann Module laden, dann das
Hauptfenster öffnen. CDI kennt diese Reihenfolge nicht. Wenn man sie nicht sehr
diszipliniert schützt, wandert Initialisierung vor den Preloader.

Damit war für uns die Frage nicht mehr: "Ist CDI gut oder schlecht?"

Die Frage war: "Ist CDI 2026 noch das richtige Werkzeug für diese Art von
JavaFX-Anwendung?"

Unsere Antwort: nein.

## Warum Avaje besser passt

Wir wollten keine komplette Neuentwicklung. Die Anwendung ist groß, fachlich
gewachsen und produktiv. Wir wollten auch nicht Dependency Injection abschaffen.
Wir wollten die Teile behalten, die helfen, und die Teile entfernen, die den
Lifecycle verschleiern.

Avaje Inject passt dafür gut, weil es deutlich näher an dem liegt, was wir
heute brauchen:

- JSR-330-Annotationen wie `@Inject` und `@Singleton`
- Compile-time DI über Annotation Processing
- weniger Runtime-Magie
- gute Fehlermeldungen früher im Build
- keine Notwendigkeit für einen Weld/CDI-Container
- ein Modell, das sich gut mit ServiceLoader und eigenen Framework-Boundaries
  kombinieren lässt

Der wichtigste Punkt ist Compile-time DI. Die Bean-Struktur wird beim Build
erzeugt, nicht durch einen Container zur Laufzeit erraten. Das reduziert die
Startup-Arbeit und macht Fehler früher sichtbar.

Aber Avaje ist nicht einfach "CDI, nur schneller". Genau das ist wichtig.

Eine Migration wird dann schlecht, wenn man versucht, jedes CDI-Konstrukt
eins zu eins nachzubauen. Dann hat man am Ende dieselbe Komplexität mit anderen
Annotationen.

Wir nutzen die Migration deshalb als Schnitt, um die Grenzen neu zu ziehen.

## Die neue Grenze: DI für Infrastruktur, nicht für alles

Die wichtigste Regel in WFX ist inzwischen:

DI ist für langlebige Infrastruktur da. Views sind explizite Laufzeitobjekte.

Ein `WindowManager` ist ein Singleton. Ein Eventbus ist ein Singleton. Eine
Repository-Implementierung kann ein Singleton sein. Ein WFX-Modul ist meistens
ein Singleton. Ein Factory-Objekt kann ein Singleton sein.

Aber eine konkrete `FXMLView` ist kein Bean. Sie ist eine View-Registrierung:
ID, Titel, Position, FXML-Datei, Icon, Parent-Bezug. Das sind Laufzeitdaten.
Diese Objekte explizit zu bauen ist klarer als sie in einen DI-Container zu
stecken.

Bei FXML-Controllern sind wir ebenfalls vorsichtig. Ein Controller wird nicht
automatisch ein Singleton, nur weil er eine Klasse ist. Viele Controller tragen
UI-State. Wenn ein Controller DI braucht und pro FXML-Ladevorgang frisch
entstehen soll, ist `@Prototype` sinnvoll. Wenn er keine Dependencies braucht,
darf ihn der `FXMLLoader` einfach normal erzeugen.

Das klingt weniger "rein" als vollständige Constructor Injection überall. Für
eine RCP-Plattform ist es aber ehrlicher. WFX ist ein Framework mit dynamischen
Views, Modulen und UI-Lifecycle. An diesen Grenzen ist explizites Bauen oft
verständlicher als Container-Magie.

## Was wir beim Umbau gefunden haben

Die Migration hat nicht nur CDI ersetzt. Sie hat Architekturstaub sichtbar
gemacht.

Ein Beispiel war der Preloader. WFX ist so gedacht, dass der Preloader sehr früh
erscheint und die Module danach laden. In der großen Anwendung passierte aber
bereits vor dem WFX-Start einiges: Versions-Lookup, Eventbus-Binding,
Root-FXML-Laden, sogar `stage.show()`.

Technisch lief das. Architektonisch war es falsch.

Die Korrektur ist nicht, Avaje anders zu konfigurieren. Die Korrektur ist, den
Lifecycle wieder sauber zu machen:

1. JavaFX startet die Anwendung.
2. WFX initialisiert minimal.
3. WFX zeigt den Preloader.
4. Module laufen durch `preload()`.
5. WFX öffnet das Hauptfenster.
6. Module finalisieren ihre UI in `start()`.

Ein anderes Beispiel waren Scopes. Über die Jahre waren viele Klassen
`@Singleton`, weil CDI sie finden oder injizieren sollte. Bei Services ist das
oft richtig. Bei Controllern, Tasks oder UI-nahen Objekten ist es oft nur
zufällig richtig.

Manche `@Singleton`-Annotationen funktionieren lange, weil das System nur einen
Use Case hat. Eine Service-Implementation, die intern einen `repositoryId`-State
hielt, war so lange unauffällig, wie nur ein Repository konfiguriert war — und
wurde sofort falsch, sobald ein zweites dazukam. Der letzte Aufruf überschrieb
den Wert auf der gemeinsamen Instanz, und der Tree zeigte plötzlich Projekte
des falschen Repositories an. Mit `@Prototype` fiel der Fehler weg, weil jeder
Aufrufer seine eigene Instanz bekommt.

Die neue Prüfung lautet nicht: "Braucht diese Klasse DI?"

Sie lautet:

- Hat dieses Objekt Anwendungslifetime?
- Enthält es UI-State?
- Wird es pro View, pro Dialog oder pro Task neu gebraucht?
- Darf es vor dem Preloader gebaut werden?
- Ist die Initialisierung billig?

Erst danach kommt die Annotation.

Ein dritter Bereich waren versteckte Container-Verantwortungen. CDI macht
Dinge, die im Code nicht stehen. Eine Extension hatte zum Beispiel alle mit
`@EventSubscriber` annotierten Methoden im Klassenpfad gefunden und automatisch
auf den Eventbus registriert. Mit der Extension fiel das einfach aus — alle so
annotierten Handler wurden lautlos zu Dead Code, bis ein Benutzer auf ein
Bookmark klickte und nichts passierte. Die Korrektur war nicht "Extension
wieder einbauen", sondern die Verdrahtung explizit zu machen: dort, wo Items
injiziert werden, dort, wo FXML-Controller geladen werden, dort, wo Module sich
initialisieren.

Ein verwandter Fall war Qualifier-Auflösung. CDI ist großzügig mit mehreren
Qualifiern auf demselben Bean. Avaje speichert pro Bean einen Qualifier-Namen
— und wenn ein Bean zwei trägt, gewinnt einer zufällig. Eine
Embedded-Solr-Provider-Klasse trug `@Solr @Embedded`. Konsumenten des nicht
gewählten Qualifiers fanden den Bean nicht und Avaje fiel auf eine andere
Implementierung zurück. Daten landeten kurzzeitig in der falschen Datenbank,
bis wir den Qualifier auf einen einzelnen reduzierten. Die Lehre: Wer ein
CDI-Modell eins zu eins zu Avaje portieren will, übersieht solche
Lax-vs-Streng-Differenzen.

## Was bleibt

Wir entfernen nicht alles Dynamische.

WFX behält ein `Lookup`-Konzept. Puristen würden sagen: Das ist ein Service
Locator und widerspricht DI. Das stimmt in einem engen Sinn. Für ein Framework
wie WFX ist es trotzdem sinnvoll, solange es eine klare Boundary bleibt.

Module, Views, optionale Erweiterungen und FXML-Controller-Fallbacks sind
dynamische Framework-Grenzen. Dort ist ein kontrolliertes Lookup pragmatisch.
In fachlicher Logik wollen wir es nicht.

Auch ServiceLoader bleibt an einigen Stellen nützlich. Nicht jede Erweiterung
muss über DI laufen. Gerade für ein leichtgewichtiges RCP-Framework ist es gut,
wenn nicht jede Erweiterung einen Container voraussetzt.

Was verschwinden soll, sind CDI-spezifische Relikte:

- `beans.xml`
- Weld
- Arquillian (für CDI-Tests)
- `@ApplicationScoped`
- `@Produces`
- `@Specializes`
- `@Default`
- CDI-Extensions
- CDI als Voraussetzung für JavaFX-Controller-Injection

Nicht weil diese Dinge schlecht sind, sondern weil sie in dieser Anwendung
nicht mehr das richtige Abstraktionsniveau haben.

## Das Ergebnis, das wir wollen

Wir messen die Migration nicht nur an "es startet wieder".

Das Ziel ist:

- weniger Runtime-Container
- schnellere und transparentere Starts
- früh sichtbarer Preloader
- klarere Scopes
- weniger versteckte Producer-Logik
- weniger Feldinjektion
- bessere Trennung zwischen Infrastruktur und UI-Laufzeitobjekten
- einfachere Tests
- weniger Überraschungen beim Start

Noch wichtiger: Neue Entwickler sollen wieder leichter verstehen können, warum
ein Objekt existiert und wer es erzeugt.

Wenn eine Klasse `@Singleton` ist, soll das fachlich stimmen. Wenn ein
Controller pro View neu entstehen muss, soll er nicht aus Bequemlichkeit ein
Singleton sein. Wenn ein Objekt teuer initialisiert, soll das nicht heimlich im
DI-Container passieren, sondern sichtbar im Startup-Lifecycle.

## Fazit

CDI war für uns keine Fehlentscheidung. Es war eine nachvollziehbare Wahl für
die Zeit, in der diese Plattform entstanden ist.

Aber eine gute Architektur muss altern dürfen. Nach 13 Jahren JavaFX, mehreren
JDK-Generationen und vielen Entwicklern ist klarer, was diese Anwendung wirklich
braucht: kein Enterprise-Container-Modell im Desktop, sondern ein kleines,
explizites DI-Modell plus ein Framework-Lifecycle, der zu JavaFX passt.

Avaje ist dafür nicht interessant, weil es eine neue Mode ist. Es ist
interessant, weil es weniger tut.

Und genau das ist hier der Vorteil.

Die Migration ist deshalb nicht nur ein Austausch von CDI gegen Avaje. Sie ist
eine Gelegenheit, das System wieder einfacher zu machen: weniger Magie, klarere
Lebenszyklen, frühere Fehler und ein Startup, bei dem der Benutzer zuerst den
Preloader sieht statt die Nebenwirkungen eines Containers.

Für eine große JavaFX-Anwendung im Jahr 2026 ist das für uns der bessere
Kompromiss.

Der nächste Schritt ist GraalVM Native Image. Compile-time DI ist eine
Voraussetzung dafür: Ohne Runtime-Reflection-Magie wird die
Reachability-Konfiguration für Native Image kleiner, ehrlicher und
automatisch verifizierbar. Avaje bringt uns also nicht nur den ruhigeren
Start. Es öffnet die Tür zu einem komplett anderen Deployment-Modell.
