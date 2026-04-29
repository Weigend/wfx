# Why a 13-Year-Old JavaFX Application No Longer Needs CDI

## A migration story from the WFX RCP framework

Johannes Weigend, April 2026

When we started building a large JavaFX application more than 13 years ago,
the world of rich-client applications in Java was a different one. Anyone who
wanted to build a professional desktop application with modules, menus,
docking, views, background initialisation and clean window management looked
at two platforms: Eclipse RCP and NetBeans RCP.

Both were powerful. Both were also heavyweight. Eclipse came with SWT and
OSGi. NetBeans was based on Swing. For many applications that was perfectly
fine. For ours it wasn't.

We wanted to use JavaFX. Not as a toy, but as the foundation for a large
domain-specific application. One important reason was visualisation: we needed
good charts, good UI components and a surface that didn't look like a relic
from the Swing era. JavaFX brought a good basis for that. What JavaFX didn't
bring was the rest of an RCP platform.

So we built it ourselves.

Back then the project was called Stagediver.fx — built at QAware, Apache 2.0.
Today the core is called WFX. It is a lightweight JavaFX RCP framework with
the things you eventually need in a large desktop application: docking,
tabbed views, FXML integration, window layout, menu and toolbar extensions, a
preloader, module discovery, an event bus and drag-and-drop support.

The goal was never to fully rebuild Eclipse or NetBeans. The goal was smaller
and more pragmatic: to build the parts a large JavaFX application really
needs, without dragging a complete enterprise platform into the desktop.

Of course we still did that at the beginning.

## The obvious decision back then

The first version followed the big RCP role models closely. We started with
OSGi and Apache Felix. Later we switched to CDI with Weld.

From today's perspective that sounds heavyweight. Back then it was plausible.

Java EE was established. CDI was the standard for dependency injection in the
Java world. You got scopes, injection, producers, qualifiers, events and
extension points in one model. Anyone building a modular Java application in
2013 who didn't want to invent everything from scratch ended up at OSGi or CDI
quickly.

We also didn't want to stray too far from the established RCP ideas. Eclipse
and NetBeans were container-driven platforms. Thinking in that direction, it
was not unreasonable to choose a container-driven design for JavaFX too.

That worked. For a long time, even quite well.

But architecture that works isn't automatically good architecture forever.

## What changed over the years

A desktop application has a different lifecycle than a server application.

On the server, a few seconds of container startup are often acceptable. A
service starts, stays alive for a long time, then processes requests. In a
desktop application, the start is more visible. A user clicks an icon and
waits. A developer starts the application many times a day to reproduce a
bug. Every second of startup time becomes noticeable.

Even more important: a JavaFX application has a very clear UI lifecycle.
Certain things must happen on the JavaFX Application Thread. Other things
must not happen there. A preloader should appear early so the user sees that
something is going on. Expensive initialisation should run after that,
ideally with progress.

A general-purpose DI container does not see this boundary automatically. It
builds beans, scans the classpath, evaluates metadata, creates proxies and
calls lifecycle hooks. All of that can be correct and still happen at the
wrong moment.

In our case, that is exactly what became visible.

During the migration we noticed that parts of the application were already
initialising before the WFX preloader. Not because JavaFX required it. But
because, over the years, startup code, CDI scopes, producers and UI
bootstrapping had become entangled. Some classes became singletons because
the container should find them. Some producers created objects whose real
lifecycle was actually tied to a window. Some initialisation ran in
constructors or `@PostConstruct` even though it really belonged in a visible
startup step.

Concretely, in our application: the embedded Solr opened its index in
`CoreContainer.createAndLoad(...)` as soon as the container resolved the
bean chain — that is, during JavaFX `init()`. The H2 metadata database did
the same in a `@PostConstruct`. And a custom bootstrapper called
`stage.show()` before WFX had even seen `start(...)`. Three container-driven
initialisations, all before the preloader. Architecturally, the DI container
had overridden the UI lifecycle.

This is not an unusual mistake. It is more like the normal case in long-lived
systems: an architectural decision that initially creates order becomes the
place where, over the years, more and more special cases get parked.

## CDI was not the problem. But it no longer fit well.

CDI is a good model for many kinds of Java applications. But it is not a
specific model for JavaFX desktop applications.

In our application, four things bothered us in particular.

First: runtime discovery. The container has to figure out at runtime which
beans exist and how they relate. That is flexible, but for a desktop
application it is often too expensive and too late. Many errors only show up
at startup.

Second: scopes that don't match the UI. `@ApplicationScoped` sounds harmless,
but in a desktop application not everything that should be technically
reachable globally is also semantically an application singleton. A window,
an FXML controller, a tree item or a task often have a specific UI context.

Third: producers and specialisations. CDI makes it easy to build a lot of
indirect wiring through `@Produces`, `@Specializes`, `@Default` and
qualifiers. That is powerful. In an application that has grown over the
years it quickly becomes hard to answer: who actually creates this object?
When? And why this specific implementation?

Fourth: the startup lifecycle. The rule for WFX is simple: first initialise
the platform, then show the preloader, then load modules, then open the main
window. CDI does not know that order. If you don't protect it very
disciplined, initialisation drifts in front of the preloader.

So the question for us was no longer: "Is CDI good or bad?"

The question was: "Is CDI in 2026 still the right tool for this kind of
JavaFX application?"

Our answer: no.

## Why Avaje is a better fit

We didn't want a complete rewrite. The application is large, has grown
domain-wise and is in production. We also didn't want to abolish dependency
injection. We wanted to keep the parts that help and remove the parts that
obscure the lifecycle.

Avaje Inject fits well for that because it is much closer to what we need
today:

- JSR-330 annotations like `@Inject` and `@Singleton`
- compile-time DI via annotation processing
- less runtime magic
- good error messages earlier in the build
- no need for a Weld/CDI container
- a model that combines well with ServiceLoader and our own framework
  boundaries

The most important point is compile-time DI. The bean structure is generated
at build time, not guessed by a container at runtime. That reduces startup
work and makes errors visible earlier.

But Avaje is not simply "CDI, just faster". That is exactly the point.

A migration goes wrong when you try to rebuild every CDI construct one to
one. Then you end up with the same complexity in different annotations.

We therefore use the migration as a cut to redraw the boundaries.

## The new boundary: DI for infrastructure, not for everything

The most important rule in WFX has become:

DI is for long-lived infrastructure. Views are explicit runtime objects.

A `WindowManager` is a singleton. An event bus is a singleton. A repository
implementation can be a singleton. A WFX module is usually a singleton. A
factory object can be a singleton.

But a concrete `FXMLView` is not a bean. It is a view registration: ID,
title, position, FXML file, icon, parent reference. That is runtime data.
Building those objects explicitly is clearer than putting them into a DI
container.

We are also careful with FXML controllers. A controller does not
automatically become a singleton just because it is a class. Many controllers
carry UI state. If a controller needs DI and should be created fresh per
FXML load, `@Prototype` makes sense. If it has no dependencies, the
`FXMLLoader` can simply create it normally.

That sounds less "pure" than full constructor injection everywhere. For an
RCP platform it is more honest, though. WFX is a framework with dynamic
views, modules and a UI lifecycle. At those boundaries, building things
explicitly is often more understandable than container magic.

## What we found while rebuilding

The migration didn't just replace CDI. It made architectural dust visible.

One example was the preloader. WFX is designed so that the preloader appears
very early and the modules load after that. In the large application,
however, quite a lot was already happening before WFX even started: version
lookup, event bus binding, root FXML loading, even `stage.show()`.

Technically that worked. Architecturally it was wrong.

The fix is not to configure Avaje differently. The fix is to make the
lifecycle clean again:

1. JavaFX starts the application.
2. WFX initialises minimally.
3. WFX shows the preloader.
4. Modules run through `preload()`.
5. WFX opens the main window.
6. Modules finalise their UI in `start()`.

Another example was scopes. Over the years, many classes were `@Singleton`
because CDI was supposed to find or inject them. For services that is often
right. For controllers, tasks or UI-near objects it is often only
accidentally right.

Some `@Singleton` annotations work for a long time because the system has
only one use case. A service implementation that internally held a
`repositoryId` state was unobtrusive as long as only one repository was
configured — and became wrong the moment a second one came along. The last
call overwrote the value on the shared instance, and the tree suddenly
showed projects of the wrong repository. With `@Prototype` the bug went
away because each caller gets its own instance.

The new check isn't: "Does this class need DI?"

It is:

- Does this object have application lifetime?
- Does it carry UI state?
- Is it needed fresh per view, per dialog, per task?
- May it be built before the preloader?
- Is its initialisation cheap?

Only after that comes the annotation.

A third area was hidden container responsibilities. CDI does things that
aren't in the code. An extension, for example, had found all methods
annotated with `@EventSubscriber` on the classpath and registered them on
the event bus automatically. With the extension gone, that simply stopped —
all such handlers became silent dead code, until a user clicked a bookmark
and nothing happened. The fix wasn't "put the extension back", but to make
the wiring explicit: where items are injected, where FXML controllers are
loaded, where modules initialise.

A related case was qualifier resolution. CDI is generous with multiple
qualifiers on the same bean. Avaje stores one qualifier name per bean — and
if a bean carries two, one wins randomly. An embedded-Solr provider class
carried `@Solr @Embedded`. Consumers of the qualifier that didn't win
couldn't find the bean, and Avaje fell back to a different implementation.
Data briefly landed in the wrong database, until we reduced the qualifier
to a single one. The lesson: anyone trying to port a CDI model one to one
to Avaje will overlook such lax-vs-strict differences.

## What stays

We don't remove everything dynamic.

WFX keeps a `Lookup` concept. Purists would say: that's a service locator
and contradicts DI. That's true in a narrow sense. For a framework like WFX
it still makes sense, as long as it stays a clear boundary.

Modules, views, optional extensions and FXML-controller fallbacks are
dynamic framework boundaries. There, a controlled lookup is pragmatic. In
domain logic we don't want it.

ServiceLoader also remains useful in some places. Not every extension has
to go through DI. Especially for a lightweight RCP framework, it is good
if not every extension requires a container.

What should disappear are CDI-specific relics:

- `beans.xml`
- Weld
- Arquillian (for CDI-based tests)
- `@ApplicationScoped`
- `@Produces`
- `@Specializes`
- `@Default`
- CDI extensions
- CDI as a prerequisite for JavaFX controller injection

Not because these things are bad, but because they are no longer the right
level of abstraction in this application.

## The result we want

We don't measure the migration just by "it starts again".

The goal is:

- less runtime container
- faster and more transparent startup
- preloader visible early
- clearer scopes
- less hidden producer logic
- less field injection
- better separation between infrastructure and UI runtime objects
- simpler tests
- fewer surprises at startup

Even more important: new developers should once again be able to understand
why an object exists and who creates it.

If a class is `@Singleton`, that should be true by domain. If a controller
has to be created fresh per view, it shouldn't be a singleton out of
convenience. If an object initialises expensively, that shouldn't happen
secretly inside the DI container, but visibly in the startup lifecycle.

## Conclusion

CDI wasn't a wrong decision for us. It was a reasonable choice for the time
when this platform came into being.

But good architecture must be allowed to age. After 13 years of JavaFX,
several JDK generations and many developers, it is clearer what this
application really needs: not an enterprise container model in the desktop,
but a small, explicit DI model plus a framework lifecycle that fits JavaFX.

Avaje is interesting not because it is a new fashion. It is interesting
because it does less.

And that is exactly the advantage here.

The migration is therefore not just an exchange of CDI for Avaje. It is an
opportunity to make the system simpler again: less magic, clearer
lifecycles, earlier errors and a startup where the user sees the preloader
first instead of the side effects of a container.

For a large JavaFX application in 2026, that is the better trade-off for us.

The next step is GraalVM Native Image. Compile-time DI is a prerequisite
for that: without runtime reflection magic, the reachability configuration
for Native Image becomes smaller, more honest and automatically verifiable.
Avaje therefore brings us not only a quieter startup. It opens the door to
a completely different deployment model.
