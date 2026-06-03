module io.softwareecg.wfx.lookup {
    requires org.slf4j;
    requires jakarta.annotation;

    exports io.softwareecg.wfx.lookup.api;

    uses io.softwareecg.wfx.lookup.api.LookupStrategy;
}
