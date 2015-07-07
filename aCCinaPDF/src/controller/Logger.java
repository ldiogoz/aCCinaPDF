/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author Diogo
 */
public final class Logger {

    private static Logger logger;
    private String log = "";

    public static void create() {
        logger = new Logger();
    }

    public Logger() {
        addEntry(System.getProperty("os.name") + " " + System.getProperty("os.arch"));
    }

    public static Logger getLogger() {
        return logger;
    }

    public final String getLog() {
        return log;
    }

    public void addEntry(final String entry) {
        Calendar calendar = GregorianCalendar.getInstance();
        String now = calendar.getTime().toGMTString();
        this.log += now + " | " + entry + "\n";
    }

    public void addEntry(final Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        Calendar calendar = GregorianCalendar.getInstance();
        String now = calendar.getTime().toGMTString();

        this.log += now + " | " + stackTrace + "\n";
    }
}
