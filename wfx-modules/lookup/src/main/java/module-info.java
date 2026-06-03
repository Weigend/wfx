module io.softwareecg.wfx.lookup {
    requires org.slf4j;
    requires jakarta.annotation;

    exports io.softwareecg.wfx.lookup;

    uses io.softwareecg.wfx.lookup.LookupStrategy;
}
