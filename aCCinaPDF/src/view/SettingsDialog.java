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
package view;

import com.itextpdf.text.pdf.PdfWriter;
import controller.Bundle;
import controller.Logger;
import model.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import model.CCSignatureSettings;
import org.apache.commons.lang3.text.WordUtils;

/**
 *
 * @author Diogo
 */
public class SettingsDialog extends javax.swing.JDialog {

    /**
     * Creates new form SettingsDialog
     *
     * @param parent
     * @param modal
     */
    public SettingsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        updateText();
        loadSettings();
        DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
        for (Bundle.Locales locale : Bundle.Locales.values()) {
            dcbm.addElement(locale.toString());
        }
        cbLanguages.setModel(dcbm);
        Locale locale = Bundle.getBundle().getCurrentLocale();
        if (locale.equals(Bundle.getBundle().getLocale(Bundle.Locales.English))) {
            cbLanguages.setSelectedIndex(0);
        } else if (locale.equals(Bundle.getBundle().getLocale(Bundle.Locales.Portugues))) {
            cbLanguages.setSelectedIndex(1);
        }
    }

    private void updateText() {
        lblLanguage.setText(Bundle.getBundle().getString("language"));
        lblPdfRenderQuality.setText(Bundle.getBundle().getString("label.pdfRenderQuality"));
        lblPdfVersion.setText(Bundle.getBundle().getString("label.pdfVersion"));
        panelSignaturePreferences.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.signaturePreferences")));
        lblPrefix.setText(Bundle.getBundle().getString("label.prefix"));
        lblDefaultWidth.setText(Bundle.getBundle().getString("label.defaultWidth"));
        lblDefaultHeight.setText(Bundle.getBundle().getString("label.defaultHeight"));
        lblPixels1.setText(Bundle.getBundle().getString("label.pixels"));
        lblPixels2.setText(Bundle.getBundle().getString("label.pixels"));
        btnSave.setText(Bundle.getBundle().getString("btn.save"));
        btnCancel.setText(Bundle.getBundle().getString("btn.cancel"));
        int selectedIndex = jComboBox2.getSelectedIndex();
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[]{Bundle.getBundle().getString("highQuality"), Bundle.getBundle().getString("mediumQuality"), Bundle.getBundle().getString("lowQuality")}));
        jComboBox2.setSelectedIndex(selectedIndex);

    }

    private void loadSettings() {
        DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
        dcbm.addElement(PdfWriter.PDF_VERSION_1_2);
        dcbm.addElement(PdfWriter.PDF_VERSION_1_3);
        dcbm.addElement(PdfWriter.PDF_VERSION_1_4);
        dcbm.addElement(PdfWriter.PDF_VERSION_1_5);
        dcbm.addElement(PdfWriter.PDF_VERSION_1_6);
        dcbm.addElement(PdfWriter.PDF_VERSION_1_7);
        jComboBox1.setModel(dcbm);

        String language = null;
        String renderQuality = null;
        String pdfVersion = null;
        String prefix = null;
        String signatureWidthString = null;
        String signatureHeightString = null;
        try {
            language = getConfigParameter("language");
            renderQuality = getConfigParameter("renderQuality");
            pdfVersion = getConfigParameter("pdfversion");
            prefix = getConfigParameter("prefix");
            signatureWidthString = getConfigParameter("signatureWidth");
            signatureHeightString = getConfigParameter("signatureHeight");
        } catch (IOException ex) {
            loadSettings();
            return;
        }

        if (pdfVersion == null || prefix == null || signatureWidthString == null || signatureHeightString == null) {
            loadSettings();
            return;
        }

        switch (language) {
            case "en-US":
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
                break;
            case "pt-PT":
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.Portugues);
                break;
            default:
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
        }

        switch (Integer.valueOf(renderQuality)) {
            case 3:
                jComboBox2.setSelectedIndex(0);
                break;
            case 2:
                jComboBox2.setSelectedIndex(1);
                break;
            case 1:
                jComboBox2.setSelectedIndex(2);
                break;
            default:
                jComboBox2.setSelectedIndex(1);
        }

        switch (pdfVersion) {
            case "/1.2":
                jComboBox1.setSelectedIndex(0);
                break;
            case "/1.3":
                jComboBox1.setSelectedIndex(1);
                break;
            case "/1.4":
                jComboBox1.setSelectedIndex(2);
                break;
            case "/1.5":
                jComboBox1.setSelectedIndex(3);
                break;
            case "/1.6":
                jComboBox1.setSelectedIndex(4);
                break;
            case "/1.7":
                jComboBox1.setSelectedIndex(5);
                break;
            default:
                jComboBox1.setSelectedIndex(5);
        }
        int signatureWidth = Integer.parseInt(signatureWidthString);
        int signatureHeight = Integer.parseInt(signatureHeightString);
        tfPrefix.setText(prefix);
        tfHeight.setText(String.valueOf(signatureHeight));
        tfWidth.setText(String.valueOf(signatureWidth));
    }

    private String getConfigParameter(String parameter) throws FileNotFoundException, IOException {
        Properties propertiesRead = new Properties();
        String configFile = "aCCinaPDF.cfg";
        String value = "";
        if (!new File(configFile).exists()) {
            CCSignatureSettings signatureSettings = new CCSignatureSettings(true);
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("msg.newConfigFile"), "", JOptionPane.INFORMATION_MESSAGE);
        }
        propertiesRead.load(new FileInputStream(configFile));
        value = propertiesRead.getProperty(parameter);
        if (value == null) {
            CCSignatureSettings signatureSettings = new CCSignatureSettings(false);
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("msg.newConfigFile"), "", JOptionPane.INFORMATION_MESSAGE);
        }
        return value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblPdfVersion = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        panelSignaturePreferences = new javax.swing.JPanel();
        lblDefaultHeight = new javax.swing.JLabel();
        lblDefaultWidth = new javax.swing.JLabel();
        tfHeight = new javax.swing.JTextField();
        tfWidth = new javax.swing.JTextField();
        lblPixels2 = new javax.swing.JLabel();
        lblPixels1 = new javax.swing.JLabel();
        lblPrefix = new javax.swing.JLabel();
        tfPrefix = new javax.swing.JTextField();
        lblPdfRenderQuality = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        lblLanguage = new javax.swing.JLabel();
        cbLanguages = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblPdfVersion.setText("Versão PDF:");
        lblPdfVersion.setToolTipText("Versão que os documentos terão após a assinatura");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setToolTipText("Versão PDF que os documentos terão após a assinatura");

        btnCancel.setText("Cancelar");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnSave.setText("Guardar");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        panelSignaturePreferences.setBorder(javax.swing.BorderFactory.createTitledBorder("Preferências da Assinatura"));

        lblDefaultHeight.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblDefaultHeight.setText("Altura padrão:");

        lblDefaultWidth.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblDefaultWidth.setText("Largura padrão:");

        lblPixels2.setText("pixéis");

        lblPixels1.setText("pixéis");

        lblPrefix.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblPrefix.setText("Prefixo: ");
        lblPrefix.setToolTipText("Prefixo (Nome) que a assinatura terá");

        tfPrefix.setToolTipText("Prefixo (Nome) que a assinatura terá");

        javax.swing.GroupLayout panelSignaturePreferencesLayout = new javax.swing.GroupLayout(panelSignaturePreferences);
        panelSignaturePreferences.setLayout(panelSignaturePreferencesLayout);
        panelSignaturePreferencesLayout.setHorizontalGroup(
            panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSignaturePreferencesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSignaturePreferencesLayout.createSequentialGroup()
                        .addGroup(panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblDefaultHeight, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(lblPrefix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfPrefix)
                            .addComponent(tfHeight)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSignaturePreferencesLayout.createSequentialGroup()
                        .addComponent(lblDefaultWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPixels1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPixels2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelSignaturePreferencesLayout.setVerticalGroup(
            panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSignaturePreferencesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPrefix)
                    .addComponent(tfPrefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDefaultWidth)
                    .addComponent(lblPixels1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignaturePreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDefaultHeight)
                    .addComponent(tfHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPixels2))
                .addContainerGap())
        );

        lblPdfRenderQuality.setText("Qualidade da renderização PDF:");
        lblPdfRenderQuality.setToolTipText("Qualidade com que é renderizado(mostrado) o PDF. Em computadores de baixa performance, deve ser usada a qualidade Baixa");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alta", "Média", "Baixa" }));
        jComboBox2.setToolTipText("Qualidade com que é renderizado(mostrado) o PDF. Em computadores de baixa performance, deve ser usada a qualidade Baixa");

        lblLanguage.setText("Idioma:");

        cbLanguages.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbLanguages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLanguagesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelSignaturePreferences, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPdfRenderQuality)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblLanguage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbLanguages, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblPdfVersion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLanguage)
                    .addComponent(cbLanguages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPdfRenderQuality)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPdfVersion)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(panelSignaturePreferences, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSave))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        switch (cbLanguages.getSelectedIndex()) {
            case 0:
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
                break;
            case 1:
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.Portugues);
                break;
            default:
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
        }
        updateText();
        int signatureHeight = 0;
        int signatureWidth = 0;
        try {
            signatureHeight = Integer.parseInt(tfHeight.getText());
            signatureWidth = Integer.parseInt(tfWidth.getText());

        } catch (NumberFormatException ex) {
            Logger.getLogger().addEntry(ex);
        }
        if (signatureHeight < 1) {
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("msg.invalidHeight"), WordUtils.capitalize(Bundle.getBundle().getString("error")), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (signatureWidth < 1) {
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("msg.invalidWidth"), WordUtils.capitalize(Bundle.getBundle().getString("error")), JOptionPane.ERROR_MESSAGE);
            return;
        }

        int renderQualitySel = jComboBox2.getSelectedIndex();

        try {
            Properties propertiesWrite;
            try (FileInputStream in = new FileInputStream("aCCinaPDF.cfg")) {
                propertiesWrite = new Properties();
                propertiesWrite.load(in);
            }
            File file = new File("aCCinaPDF.cfg");
            FileOutputStream fileOut;
            if (renderQualitySel == 0) {
                Settings.getSettings().setRenderImageQuality(2);
                propertiesWrite.setProperty("renderQuality", String.valueOf(3));
            } else if (renderQualitySel == 1) {
                Settings.getSettings().setRenderImageQuality(1);
                propertiesWrite.setProperty("renderQuality", String.valueOf(2));
            } else if (renderQualitySel == 2) {
                Settings.getSettings().setRenderImageQuality(0);
                propertiesWrite.setProperty("renderQuality", String.valueOf(1));
            }
            switch (cbLanguages.getSelectedIndex()) {
                case 0:
                    propertiesWrite.setProperty("language", "en-US");
                    break;
                case 1:
                    propertiesWrite.setProperty("language", "pt-PT");
                    break;
                default:
                    propertiesWrite.setProperty("language", "en-US");
            }
            propertiesWrite.setProperty("prefix", tfPrefix.getText());
            propertiesWrite.setProperty("pdfversion", String.valueOf(jComboBox1.getSelectedItem()));
            propertiesWrite.setProperty("signatureWidth", tfWidth.getText());
            propertiesWrite.setProperty("signatureHeight", tfHeight.getText());
            fileOut = new FileOutputStream(file);
            propertiesWrite.store(fileOut, "Settings");
            fileOut.close();

            this.dispose();
        } catch (FileNotFoundException ex) {
            controller.Logger.getLogger().addEntry(ex);
            JOptionPane.showMessageDialog(getParent(), Bundle.getBundle().getString("msg.errorSavingSettings1"), WordUtils.capitalize(Bundle.getBundle().getString("error")), JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            controller.Logger.getLogger().addEntry(ex);
            JOptionPane.showMessageDialog(getParent(), Bundle.getBundle().getString("msg.errorSavingSettings2"), WordUtils.capitalize(Bundle.getBundle().getString("error")), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void cbLanguagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLanguagesActionPerformed

    }//GEN-LAST:event_cbLanguagesActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SettingsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                SettingsDialog dialog = new SettingsDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cbLanguages;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel lblDefaultHeight;
    private javax.swing.JLabel lblDefaultWidth;
    private javax.swing.JLabel lblLanguage;
    private javax.swing.JLabel lblPdfRenderQuality;
    private javax.swing.JLabel lblPdfVersion;
    private javax.swing.JLabel lblPixels1;
    private javax.swing.JLabel lblPixels2;
    private javax.swing.JLabel lblPrefix;
    private javax.swing.JPanel panelSignaturePreferences;
    private javax.swing.JTextField tfHeight;
    private javax.swing.JTextField tfPrefix;
    private javax.swing.JTextField tfWidth;
    // End of variables declaration//GEN-END:variables
}
