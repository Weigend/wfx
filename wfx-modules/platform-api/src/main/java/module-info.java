module io.softwareecg.wfx.platform.api {
    // Stage appears in the public API (PlatformApplication.start)
    requires transitive javafx.graphics;

    exports io.softwareecg.wfx.platform.api;
    exports io.softwareecg.wfx.platform.api.events;
    exports io.softwareecg.wfx.platform.api.exceptions;
}
