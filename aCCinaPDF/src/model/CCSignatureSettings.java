/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import view.AppearanceSettingsDialog;

/**
 *
 * @author Diogo
 */
public final class CCSignatureSettings {

    private CCAlias ccAlias;
    private boolean visibleSignature;
    private boolean timestamp;
    private String timestampServer;
    private boolean ocspClient;
    private String reason;
    private String location;
    private int certificationLevel;
    private String text;
    private Rectangle positionOnDocument;
    private int pageNumber;
    private AppearanceSettings appearance;
    private String prefix;

    public CCSignatureSettings() {
        File fsettings = new File("aCCinaPDF.cfg");
        if (!fsettings.exists()) {
            Properties propertiesWrite = new Properties();
            File file = new File("aCCinaPDF.cfg");
            FileOutputStream fileOut;
            try {
                propertiesWrite.setProperty("prefix", "aCCinatura");
                propertiesWrite.setProperty("fontLocation", "extrafonts" + File.separator + "verdana.ttf");
                propertiesWrite.setProperty("fontItalic", "true");
                propertiesWrite.setProperty("fontBold", "false");
                propertiesWrite.setProperty("showName", "true");
                propertiesWrite.setProperty("showReason", "true");
                propertiesWrite.setProperty("showLocation", "true");
                propertiesWrite.setProperty("showDate", "true");
                propertiesWrite.setProperty("fontColor", "0");
                propertiesWrite.setProperty("signatureWidth", "403");
                propertiesWrite.setProperty("signatureHeight", "25");
                propertiesWrite.setProperty("pdfversion", String.valueOf(PdfWriter.PDF_VERSION_1_7));
                fileOut = new FileOutputStream(file);
                propertiesWrite.store(fileOut, "Settings");
                fileOut.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        setPrefix(getConfigParameter("prefix"));
        appearance = new AppearanceSettings();
        appearance.setBold(Boolean.valueOf(getConfigParameter("bold")));
        appearance.setItalic(Boolean.valueOf(getConfigParameter("italic")));
        appearance.setFontLocation(getConfigParameter("fontLocation"));
        appearance.setShowName(Boolean.valueOf(getConfigParameter("showName")));
        appearance.setShowDate(Boolean.valueOf(getConfigParameter("showDate")));
        appearance.setShowReason(Boolean.valueOf(getConfigParameter("showReason")));
        appearance.setShowLocation(Boolean.valueOf(getConfigParameter("showLocation")));
        appearance.setFontColor(new Color(Integer.valueOf(getConfigParameter("fontColor"))));
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String getConfigParameter(String parameter) {
        Properties propertiesRead = new Properties();
        String value = "";
        try {
            propertiesRead.load(new FileInputStream("aCCinaPDF.cfg"));
            value = propertiesRead.getProperty(parameter);
        } catch (FileNotFoundException ex) {
            controller.Logger.getLogger().addEntry(ex);
            return "Not Found";

        } catch (IOException ex) {
            controller.Logger.getLogger().addEntry(ex);
            return "Not Found";
        }
        return value;
    }

    public AppearanceSettings getAppearance() {
        return appearance;
    }

    public void setAppearance(AppearanceSettings appearance) {
        this.appearance = appearance;
    }

    public boolean isOcspClient() {
        return ocspClient;
    }

    public void setOcspClient(boolean ocspClient) {
        this.ocspClient = ocspClient;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Rectangle getPositionOnDocument() {
        return positionOnDocument;
    }

    public void setSignaturePositionOnDocument(Rectangle positionOnDocument) {
        this.positionOnDocument = positionOnDocument;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestampServer() {
        return timestampServer;
    }

    public void setTimestampServer(String timestampServer) {
        this.timestampServer = timestampServer;
    }

    public CCAlias getCcAlias() {
        return ccAlias;
    }

    public void setCcAlias(CCAlias ccAlias) {
        this.ccAlias = ccAlias;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCertificationLevel() {
        return certificationLevel;
    }

    public void setCertificationLevel(int certificationLevel) {
        this.certificationLevel = certificationLevel;
    }

    public boolean isTimestamp() {
        return timestamp;
    }

    public void setTimestamp(boolean timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isVisibleSignature() {
        return visibleSignature;
    }

    public void setVisibleSignature(boolean visibleSignature) {
        this.visibleSignature = visibleSignature;
    }

}
