/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import controller.Settings;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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

    public CCSignatureSettings(boolean forceCreateConfigFile) {
        appearance = new AppearanceSettings();
        String fsettings = "aCCinaPDF.cfg";
        if (!new File(fsettings).exists() || forceCreateConfigFile) {
            createConfigFile();
        }
        try {
            String pdfVersionStr = getConfigParameter("pdfversion");
            String renderQualityStr = getConfigParameter("renderQuality");
            String prefixStr = getConfigParameter("prefix");
            String boldStr = getConfigParameter("fontBold");
            String italicStr = getConfigParameter("fontItalic");
            String fontLocationStr = getConfigParameter("fontLocation");
            String showNameStr = getConfigParameter("showName");
            String showDateStr = getConfigParameter("showDate");
            String showReasonStr = getConfigParameter("showReason");
            String showLocationStr = getConfigParameter("showLocation");
            String fontColorStr = getConfigParameter("fontColor");
            String textAlignStr = getConfigParameter("textAlign");

            if (pdfVersionStr == null || renderQualityStr == null || prefixStr == null || boldStr == null || italicStr == null || fontLocationStr == null
                    || showNameStr == null || showDateStr == null || showReasonStr == null || showLocationStr == null || fontColorStr == null || textAlignStr == null) {
                new File(fsettings).delete();
                createConfigFile();
                return;
            }

            Settings.getSettings().setPdfVersion(pdfVersionStr);
            Settings.getSettings().setRenderImageQuality(Integer.valueOf(renderQualityStr));
            setPrefix(prefixStr);
            appearance.setBold(Boolean.valueOf(boldStr));
            appearance.setItalic(Boolean.valueOf(italicStr));
            appearance.setFontLocation(fontLocationStr);
            appearance.setShowName(Boolean.valueOf(showNameStr));
            appearance.setShowDate(Boolean.valueOf(showDateStr));
            appearance.setShowReason(Boolean.valueOf(showReasonStr));
            appearance.setShowLocation(Boolean.valueOf(showLocationStr));
            appearance.setFontColor(new Color(Integer.valueOf(fontColorStr)));
            appearance.setAlign(Integer.valueOf(textAlignStr));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createConfigFile() {
        String fsettings = "aCCinaPDF.cfg";
        Properties propertiesWrite = new Properties();
        FileOutputStream fileOut;
        try {
            propertiesWrite.setProperty("renderQuality", String.valueOf(Image.SCALE_SMOOTH));
            propertiesWrite.setProperty("pdfversion", "/1.7");
            propertiesWrite.setProperty("prefix", "aCCinatura");
            propertiesWrite.setProperty("fontLocation", "extrafonts" + File.separator + "verdana.ttf");
            propertiesWrite.setProperty("fontItalic", "true");
            propertiesWrite.setProperty("fontBold", "false");
            propertiesWrite.setProperty("textAlign", "0");
            propertiesWrite.setProperty("showName", "true");
            propertiesWrite.setProperty("showReason", "true");
            propertiesWrite.setProperty("showLocation", "true");
            propertiesWrite.setProperty("showDate", "true");
            propertiesWrite.setProperty("fontColor", "0");
            propertiesWrite.setProperty("signatureWidth", "403");
            propertiesWrite.setProperty("signatureHeight", "34");
            propertiesWrite.setProperty("pdfversion", String.valueOf(PdfWriter.PDF_VERSION_1_7));
            fileOut = new FileOutputStream(fsettings);
            propertiesWrite.store(fileOut, "Settings");
            fileOut.close();
            loadDefaults();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadDefaults() {
        Settings.getSettings().setPdfVersion("/1.7");
        Settings.getSettings().setRenderImageQuality(Image.SCALE_SMOOTH);
        setPrefix("aCCinatura");
        appearance.setBold(false);
        appearance.setItalic(true);
        appearance.setFontLocation("extrafonts" + File.separator + "verdana.ttf");
        appearance.setShowName(true);
        appearance.setShowDate(true);
        appearance.setShowReason(true);
        appearance.setShowLocation(true);
        appearance.setFontColor(new Color(0));
        appearance.setAlign(0);

        JOptionPane.showMessageDialog(null, "O ficheiro de configurações está corrompido ou pertence a uma versão antiga\nFoi criado um novo ficheiro de configurações", "", JOptionPane.INFORMATION_MESSAGE);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String getConfigParameter(String parameter) throws FileNotFoundException, IOException {
        Properties propertiesRead = new Properties();
        String configFile = "aCCinaPDF.cfg";
        propertiesRead.load(new FileInputStream(configFile));
        String value = propertiesRead.getProperty(parameter);
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
