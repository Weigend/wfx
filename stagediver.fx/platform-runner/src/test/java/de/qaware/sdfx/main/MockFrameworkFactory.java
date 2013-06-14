package de.qaware.sdfx.main;

import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class MockFrameworkFactory implements FrameworkFactory {
    @Override
    public Framework newFramework(Map<String, String> stringStringMap) {
        return mock(Framework.class);
    }
}
