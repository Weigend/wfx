module io.softwareecg.wfx.windowmanager.api {
    requires io.softwareecg.wfx.lookup;

    // All JavaFX types below appear in the public API surface
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    exports io.softwareecg.wfx.windowmanager.api;
    exports io.softwareecg.wfx.windowmanager.api.exceptions;
}
