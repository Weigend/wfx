package io.softwareecg.wfx.lookup.avaje.testbeans;

import io.softwareecg.wfx.lookup.avaje.AvajeLookupStrategyTest;
import jakarta.inject.Singleton;

@Singleton
public class Service3 implements AvajeLookupStrategyTest.TestService1 {
    @Override
    public String sayGoodbye() {
        return "Goodbye Avaje";
    }
}
