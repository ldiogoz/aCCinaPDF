/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.Image;

/**
 *
 * @author Toshiba
 */
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
