package de.qaware.sdfx.main;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StartupLoggerTest {

    private StartupLogger logger = new StartupLogger(getClass());

    @Mock
    private PrintStream output;

    @Before
    public void setUp() throws Exception {
        Field writerField = StartupLogger.class.getDeclaredField("writer");
        writerField.setAccessible(true);
        writerField.set(null, output);
    }

    @Test
    public void testError() throws Exception {
        StartupLogger.Level.ERROR.setEnabled(true);
        String msg = "Error Msg";
        logger.error(msg);
        validateMsg("error", msg);
    }

    @Test
    public void testErrorDisabled() throws Exception {
        StartupLogger.Level.ERROR.setEnabled(false);
        String msg = "Error Msg";
        logger.error(msg);
        verify(output, never()).print(anyString());
    }

    @Test
    public void testErrorException() throws Exception {
        StartupLogger.Level.ERROR.setEnabled(true);
        String msg = "Error Msg";
        Exception ex = new Exception("Test Exception");
        logger.error(msg, ex);
        String actual = validateMsg("error", msg);
        assertTrue(actual.contains(ex.getMessage()));
    }

    @Test
    public void testWarn() throws Exception {
        StartupLogger.Level.WARN.setEnabled(true);
        String msg = "warn Msg";
        logger.warn(msg);
        validateMsg("warn", msg);
    }

    @Test
    public void testWarnDisabled() throws Exception {
        StartupLogger.Level.WARN.setEnabled(false);
        String msg = "warn Msg";
        logger.warn(msg, "Abc");
        verify(output, never()).print(anyString());
    }

    @Test
    public void testWarnException() throws Exception {
        StartupLogger.Level.WARN.setEnabled(true);
        String msg = "Error Msg";
        Exception ex = new Exception("Test Exception");
        logger.warn(msg, ex);
        String actual = validateMsg("warn", msg);
        assertTrue(actual.contains(ex.getMessage()));
    }

    @Test
    public void testInfo() throws Exception {
        StartupLogger.Level.INFO.setEnabled(true);
        String msg = "info Msg";
        logger.info(msg);
        validateMsg("info", msg);
    }

    @Test
    public void testInfoDisabled() throws Exception {
        StartupLogger.Level.INFO.setEnabled(false);
        String msg = "info Msg";
        logger.info(msg);
        verify(output, never()).print(anyString());
    }

    @Test
    public void testInfoException() throws Exception {
        StartupLogger.Level.INFO.setEnabled(true);
        String msg = "info Msg";
        Exception ex = new Exception("Test Exception");
        logger.info(msg, ex);
        String actual = validateMsg("info", msg);
        assertTrue(actual.contains(ex.getMessage()));
    }

    @Test
    public void testDebug() throws Exception {
        StartupLogger.Level.DEBUG.setEnabled(true);
        String msg = "debug Msg";
        logger.debug(msg);
        validateMsg("debug", msg);
    }

    @Test
    public void testDebugDisabled() throws Exception {
        StartupLogger.Level.DEBUG.setEnabled(false);
        String msg = "debug Msg";
        logger.debug(msg, new RuntimeException("Test"));
        verify(output, never()).print(anyString());
    }

    @Test
    public void testDebugException() throws Exception {
        StartupLogger.Level.DEBUG.setEnabled(true);
        String msg = "debug Msg";
        Exception ex = new Exception("Test Exception");
        logger.debug(msg, ex);
        String actual = validateMsg("debug", msg);
        assertTrue(actual.contains(ex.getMessage()));
    }

    private String validateMsg(String level, String message) {

        ArgumentCaptor<String> writtenCaptor = ArgumentCaptor.forClass(String.class);

        verify(output).print(writtenCaptor.capture());
        String actual = writtenCaptor.getValue();
        assertTrue(actual.contains(level.toUpperCase()));
        assertTrue(actual.contains(message));
        return actual;
    }
}
