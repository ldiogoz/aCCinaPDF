/*
 *   Copyright 2015 Luís Diogo Zambujo, Micael Sousa Farinha and Miguel Frade
 *
 *   This file is part of aCCinaPDF.
 *
 *   aCCinaPDF is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   aCCinaPDF is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with aCCinaPDF.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
