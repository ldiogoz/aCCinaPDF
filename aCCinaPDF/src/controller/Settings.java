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

import java.awt.Image;

public class Settings {

    private int renderImageQuality = Image.SCALE_SMOOTH;
    private String pdfVersion = "/1.7";

    public String getPdfVersion() {
        return pdfVersion;
    }

    public void setPdfVersion(String pdfVersion) {
        this.pdfVersion = pdfVersion;
    }

    public int getRenderImageQuality() {
        return renderImageQuality;
    }

    public void setRenderImageQuality(int renderImageQuality) {
        this.renderImageQuality = renderImageQuality;
    }

    private static final Settings settings = new Settings();

    public static Settings getSettings() {
        return settings;
    }

    public Settings() {
    }

}
