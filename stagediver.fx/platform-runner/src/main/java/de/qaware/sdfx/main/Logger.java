// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 17.07.13 08:30
//     description: Simple logger for the startup of stagediver.fx
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.main;

import java.io.PrintStream;

/**
 * This is a simple logger for the startup of stagediver.fx.
 */
public class Logger {

    private static boolean enableDebug = false;
    private static PrintStream writer = System.err;
    private String name;

    public Logger(Class clazz) {
        name = clazz.getName();
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }

    public static void setEnableDebug(boolean enableDebug) {
        Logger.enableDebug = enableDebug;
    }

    public void error(String message) {
        writeMessage("ERROR", message);
    }

    public void error(String message, Throwable e) {
        writeMessage("ERROR", message, e);
    }

    public void error(String message, Object... args) {
        writeMessage("ERROR", message, args);
    }

    public void warn(String message) {
        writeMessage("WARN", message);
    }

    public void warn(String message, Throwable e) {
        writeMessage("WARN", message, e);
    }

    public void warn(String message, Object... args) {
        writeMessage("WARN", message, args);
    }

    public void info(String message) {
        writeMessage("INFO", message);
    }

    public void info(String message, Throwable e) {
        writeMessage("INFO", message, e);
    }

    public void info(String message, Object... args) {
        writeMessage("INFO", message, args);
    }

    public void debug(String message) {
        if (isEnableDebug()) {
            writeMessage("DEBUG", message);
        }
    }

    public void debug(String message, Throwable e) {
        if (isEnableDebug()) {
            writeMessage("DEBUG", message, e);
        }
    }

    public void debug(String message, Object... args) {
        if (isEnableDebug()) {
            writeMessage("DEBUG", message, args);
        }
    }

    private void writeMessage(String level, String message) {
        writeMessage(level, message, new String[]{""});
    }

    private void writeMessage(String level, String message, Throwable e) {
        String msg = String.format("[%s]%s %s - %s - %s%n", Thread.currentThread().getName(), level, name, message,
                e.getMessage());
        writer.print(msg);
        e.printStackTrace(writer);
        writer.print('\n');
    }

    private void writeMessage(String level, String message, Object[] args) {
        String msg = String.format(message, args);
        msg = String.format("[%s] %s %s - %s%n", Thread.currentThread().getName(), level, name, msg);
        writer.print(msg);
    }
}
