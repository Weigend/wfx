package io.softwareecg.wfx.lookup.avaje.testbeans;

import io.softwareecg.wfx.lookup.TestService;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

@Singleton
@Priority(-1000)
public class Service2 implements TestService {
    @Override
    public String sayHello() {
        return "Hello Alternative";
    }
}
