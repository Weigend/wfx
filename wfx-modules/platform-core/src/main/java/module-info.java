module io.softwareecg.wfx.platform.core {
    requires io.softwareecg.wfx.lookup;
    requires io.softwareecg.wfx.platform.api;
    requires io.softwareecg.wfx.windowmanager.api;
    requires io.avaje.inject;
    requires jakarta.inject;
    requires jakarta.annotation;
    requires org.slf4j;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    // FXMLLoader needs reflective access to ProgressController (@FXML fields)
    opens io.softwareecg.wfx.platform.core to javafx.fxml;

    provides io.avaje.inject.spi.InjectExtension
        with io.softwareecg.wfx.platform.core.CoreModule;
    provides io.softwareecg.wfx.platform.api.PlatformApplication
        with io.softwareecg.wfx.platform.core.PlatformApplicationImpl;
    provides io.softwareecg.wfx.platform.api.EventBus
        with io.softwareecg.wfx.platform.core.eventbus.SimpleEventBus;
}
