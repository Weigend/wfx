module io.softwareecg.wfx.windowmanager.core {
    requires io.softwareecg.wfx.lookup;
    requires io.softwareecg.wfx.windowmanager.api;
    requires io.avaje.inject;
    requires jakarta.inject;
    requires jakarta.annotation;
    requires org.slf4j;
    requires org.apache.commons.lang3;

    // FXMLLoader needs reflective access to DefaultApplicationWindow (@FXML fields)
    opens io.softwareecg.wfx.windowmanager.windows to javafx.fxml;

    provides io.avaje.inject.spi.InjectExtension
        with io.softwareecg.wfx.windowmanager.WindowmanagerModule;
    provides io.softwareecg.wfx.windowmanager.api.WindowManager
        with io.softwareecg.wfx.windowmanager.impl.WindowManagerImpl;
    provides io.softwareecg.wfx.windowmanager.api.ApplicationWindow
        with io.softwareecg.wfx.windowmanager.windows.DefaultApplicationWindow;
    provides io.softwareecg.wfx.windowmanager.impl.ViewContainerAreaFactory
        with io.softwareecg.wfx.windowmanager.impl.ViewContainerAreaFactoryImpl;
}
