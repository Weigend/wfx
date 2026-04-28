package io.softwareecg.wfx.lookup.avaje.testbeans;

import io.softwareecg.wfx.lookup.TestService;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

@Singleton
@Priority(1)
public class Service1 implements TestService {
    @Override
    public String sayHello() {
        return "Hello Avaje";
    }
}
