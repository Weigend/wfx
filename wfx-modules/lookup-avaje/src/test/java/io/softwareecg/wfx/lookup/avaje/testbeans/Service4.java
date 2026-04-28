package io.softwareecg.wfx.lookup.avaje.testbeans;

import io.softwareecg.wfx.lookup.TypedTestService;
import jakarta.inject.Singleton;

@Singleton
public class Service4 implements TypedTestService<String> {
    @Override
    public String sayGoodbye() {
        return "Goodbye typed Avaje";
    }
}
