/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import com.itextpdf.text.FontFactory;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import model.CCSignatureSettings;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author admin
 */
public class AppearanceSettingsDialog extends javax.swing.JDialog {

    private CCSignatureSettings signatureSettings;
    private HashMap<com.itextpdf.text.Font, String> fontList;

    /**
     * Creates new form NewJDialog
     * @param parent
     * @param modal
     * @param signatureSettings
     */
    public AppearanceSettingsDialog(java.awt.Frame parent, boolean modal, CCSignatureSettings signatureSettings) {
        super(parent, modal);
        initComponents();
        this.signatureSettings = signatureSettings;
        previewPanel1.setAliasName(signatureSettings.getAlias());

        // Pastas conforme o SO
        ArrayList<String> dirs = new ArrayList<>();
        if (SystemUtils.IS_OS_WINDOWS) {
            dirs.add(System.getenv("windir") + File.separator + "fonts");
        } else if (SystemUtils.IS_OS_LINUX) {
            dirs.add("/usr/share/fonts/truetype/");
            dirs.add("/usr/X11R6/lib/X11/fonts/");
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            dirs.add("/Library/Fonts");
            dirs.add("/System/Library/Fonts");
        }
        dirs.add("extrafonts");

        // Hashmap com fonts
        fontList = getAllFonts(dirs);
        ArrayList<String> fontsListArray = new ArrayList<>();
        for (Map.Entry<com.itextpdf.text.Font, String> font : fontList.entrySet()) {
            fontsListArray.add(font.getKey().getFamilyname());
        }
        Comparator<String> strCompare = new Comparator<String>() {
            @Override
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
            }
        };

        fontsListArray.sort(strCompare);
        for (String str : fontsListArray) {
            cbFontType.addItem(str);
        }

        String fontLocation = signatureSettings.getAppearance().getFontLocation();
        boolean italic = signatureSettings.getAppearance().isItalic();
        boolean bold = signatureSettings.getAppearance().isBold();
        boolean showLocation = signatureSettings.getAppearance().isShowLocation();
        boolean showReason = signatureSettings.getAppearance().isShowReason();
        boolean showDate = signatureSettings.getAppearance().isShowDate();

        previewPanel1.setReason(signatureSettings.getReason());
        previewPanel1.setShowDate(showDate);

        if (!signatureSettings.getLocation().isEmpty()) {
            previewPanel1.setLocation(signatureSettings.getLocation());
            checkBoxLocalizacao.setSelected(showLocation);
            previewPanel1.setShowLocation(showLocation);
            System.out.println(showLocation);
        } else {
            checkBoxLocalizacao.setEnabled(false);
            checkBoxLocalizacao.setSelected(false);
        }

        if (!signatureSettings.getReason().isEmpty()) {
            previewPanel1.setReason(signatureSettings.getReason());
            checkBoxRazao.setSelected(showReason);
            previewPanel1.setShowReason(showReason);
        } else {
            checkBoxRazao.setEnabled(false);
            checkBoxRazao.setSelected(false);
        }
        previewPanel1.setText(signatureSettings.getText());
        colorChooser.setPreviewPanel(new JPanel());
        Color color = signatureSettings.getAppearance().getFontColor();
        colorChooser.setColor(color);
        lblSignature.setForeground(color);
        checkBoxRazao.setSelected(showReason);
        checkBoxLocalizacao.setSelected(showLocation);
        checkBoxTimestamp.setSelected(showDate);

        ColorSelectionModel model = colorChooser.getSelectionModel();
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Color newForegroundColor = colorChooser.getColor();
                lblSignature.setForeground(newForegroundColor);
            }
        };
        model.addChangeListener(changeListener);

        if (italic && bold) {
            lblSignature.setFont(new Font(fontLocation, Font.ITALIC + Font.BOLD, 25));
        } else if (italic && !bold) {
            lblSignature.setFont(new Font(fontLocation, Font.ITALIC, 25));
        } else if (!italic && bold) {
            lblSignature.setFont(new Font(fontLocation, Font.BOLD, 25));
        } else {
            lblSignature.setFont(new Font(fontLocation, Font.PLAIN, 25));
        }

        updateSettings(fontLocation, 25, bold, italic);

        previewPanel1.repaint();
    }

    private HashMap<com.itextpdf.text.Font, String> getAllFonts(ArrayList<String> dirs) {
        final HashMap<com.itextpdf.text.Font, String> hmFonts = new HashMap<>();
        for (String dir : dirs) {
            File folder = new File(dir);
            getFontsFromFolder(folder, hmFonts);
        }
        return hmFonts;
    }

    ;

    private void getFontsFromFolder(File folder, HashMap<com.itextpdf.text.Font, String> fontList) {
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();

            for (File f : listOfFiles) {
                if (f.isDirectory()) {
                    getFontsFromFolder(f, fontList);
                } else if (f.isFile() && f.getName().endsWith(".ttf")) {
                    com.itextpdf.text.Font font = FontFactory.getFont(f.getAbsolutePath());
                    boolean contains = false;
                    for (com.itextpdf.text.Font ff : fontList.keySet()) {
                        if (ff.getFamilyname().equals(font.getFamilyname())) {
                            contains = true;
                            break;
                        }
                    }
                    if (!font.getFamilyname().equals("unknown")) {
                        if (!contains) {
                            fontList.put(font, f.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private void updateSettings(String font, int size, boolean bold, boolean italic) {
        ComboBoxModel model2 = (ComboBoxModel) cbFontType.getModel();
        for (int i = 0; i < model2.getSize(); i++) {
            if (cbFontType.getItemAt(i).toString().equalsIgnoreCase(FontFactory.getFont(font).getFamilyname())) {
                cbFontType.setSelectedIndex(i);
            }
        }

        checkBoxBold.setSelected(bold);
        checkBoxItalic.setSelected(italic);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel1 = new javax.swing.JPanel();
        lblSignature = new javax.swing.JLabel();
        checkBoxBold = new javax.swing.JCheckBox();
        checkBoxItalic = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        cbFontType = new javax.swing.JComboBox();
        colorChooser = new javax.swing.JColorChooser();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        checkBoxRazao = new javax.swing.JCheckBox();
        checkBoxLocalizacao = new javax.swing.JCheckBox();
        previewPanel1 = new model.PreviewPanel();
        checkBoxTimestamp = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        checkBoxGuardar = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Definições da Fonte"));

        lblSignature.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSignature.setText("Texto Exemplo");
        lblSignature.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        checkBoxBold.setText("Negrito");
        checkBoxBold.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxBoldItemStateChanged(evt);
            }
        });

        checkBoxItalic.setText("Itálico");
        checkBoxItalic.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxItalicItemStateChanged(evt);
            }
        });

        jLabel2.setText("Tipo de letra:");

        cbFontType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbFontTypeItemStateChanged(evt);
            }
        });

        colorChooser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                colorChooserMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSignature, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbFontType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(colorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(checkBoxBold)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxItalic)
                        .addGap(0, 356, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 13, Short.MAX_VALUE)
                .addComponent(lblSignature, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cbFontType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxBold)
                    .addComponent(checkBoxItalic))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButton1.setText("Cancelar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Aceitar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Definições da Assinatura"));

        checkBoxRazao.setSelected(true);
        checkBoxRazao.setText("Razão");
        checkBoxRazao.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxRazaoItemStateChanged(evt);
            }
        });

        checkBoxLocalizacao.setSelected(true);
        checkBoxLocalizacao.setText("Localização");
        checkBoxLocalizacao.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxLocalizacaoItemStateChanged(evt);
            }
        });

        previewPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout previewPanel1Layout = new javax.swing.GroupLayout(previewPanel1);
        previewPanel1.setLayout(previewPanel1Layout);
        previewPanel1Layout.setHorizontalGroup(
            previewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        previewPanel1Layout.setVerticalGroup(
            previewPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 127, Short.MAX_VALUE)
        );

        checkBoxTimestamp.setSelected(true);
        checkBoxTimestamp.setText("Data e Hora");
        checkBoxTimestamp.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                checkBoxTimestampItemStateChanged(evt);
            }
        });

        jLabel1.setText("Mostrar:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxRazao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxLocalizacao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxTimestamp)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(checkBoxRazao)
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(checkBoxLocalizacao)
                        .addComponent(checkBoxTimestamp)))
                .addContainerGap())
        );

        checkBoxGuardar.setText("Definir como definições padrão");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkBoxGuardar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1)
                    .addComponent(checkBoxGuardar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void cbFontTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbFontTypeItemStateChanged
        changeLabel();
    }//GEN-LAST:event_cbFontTypeItemStateChanged

    private void checkBoxBoldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBoxBoldItemStateChanged
        changeLabel();
    }//GEN-LAST:event_checkBoxBoldItemStateChanged

    private void checkBoxItalicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBoxItalicItemStateChanged
        changeLabel();
    }//GEN-LAST:event_checkBoxItalicItemStateChanged

    private void checkBoxRazaoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBoxRazaoItemStateChanged
        if (checkBoxRazao.isSelected()) {
            previewPanel1.setShowReason(true);
        } else {
            previewPanel1.setShowReason(false);
        }
    }//GEN-LAST:event_checkBoxRazaoItemStateChanged

    private void checkBoxLocalizacaoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBoxLocalizacaoItemStateChanged
        if (checkBoxLocalizacao.isSelected()) {
            previewPanel1.setShowLocation(true);
        } else {
            previewPanel1.setShowLocation(false);
        }
    }//GEN-LAST:event_checkBoxLocalizacaoItemStateChanged

    private void colorChooserMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorChooserMousePressed

    }//GEN-LAST:event_colorChooserMousePressed

    private void checkBoxTimestampItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkBoxTimestampItemStateChanged
        if (checkBoxTimestamp.isSelected()) {
            previewPanel1.setShowDate(true);
        } else {
            previewPanel1.setShowDate(false);
        }
    }//GEN-LAST:event_checkBoxTimestampItemStateChanged

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (checkBoxGuardar.isSelected()) {
            writeFile();
        }

        signatureSettings.getAppearance().setFontColor(colorChooser.getColor());
        signatureSettings.getAppearance().setBold(checkBoxBold.isSelected());
        signatureSettings.getAppearance().setItalic(checkBoxItalic.isSelected());
        String fontLocation = getFontLocationByName((String) cbFontType.getSelectedItem());
        signatureSettings.getAppearance().setFontLocation(fontLocation);

        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private String getFontLocationByName(String name) {
        Set<Map.Entry<com.itextpdf.text.Font, String>> entries = fontList.entrySet();
        for (Map.Entry<com.itextpdf.text.Font, String> entry : entries) {
            if (entry.getKey().getFamilyname().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
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

    private void writeFile() {

        Properties propertiesWrite = new Properties();
        File file = new File("aCCinaPDF.cfg");
        FileOutputStream fileOut;
        try {
            propertiesWrite.setProperty("fontLocation", getFontLocationByName(cbFontType.getSelectedItem().toString()));
            propertiesWrite.setProperty("fontColor", String.valueOf(colorChooser.getColor().getRGB()));
            propertiesWrite.setProperty("fontItalic", String.valueOf(checkBoxItalic.isSelected()));
            propertiesWrite.setProperty("fontBold", String.valueOf(checkBoxBold.isSelected()));
            propertiesWrite.setProperty("showReason", String.valueOf(checkBoxRazao.isSelected()));
            propertiesWrite.setProperty("showLocation", String.valueOf(checkBoxLocalizacao.isSelected()));
            propertiesWrite.setProperty("showDate", String.valueOf(checkBoxTimestamp.isSelected()));

            fileOut = new FileOutputStream(file);
            propertiesWrite.store(fileOut, "Settings");
            fileOut.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void changeLabel() {
        String font = getFontCbBox();

        if (checkBoxBold.isSelected() && checkBoxItalic.isSelected()) {
            lblSignature.setFont(new Font(font, Font.BOLD + Font.ITALIC, 25));
        } else if (checkBoxBold.isSelected() && !checkBoxItalic.isSelected()) {
            lblSignature.setFont(new Font(font, Font.BOLD, 25));
        } else if (!checkBoxBold.isSelected() && checkBoxItalic.isSelected()) {
            lblSignature.setFont(new Font(font, Font.ITALIC, (Integer) 25));
        } else {
            lblSignature.setFont(new Font(cbFontType.getSelectedItem().toString(), Font.PLAIN, (Integer) 25));
        }
    }

    private String getFontCbBox() {
        return cbFontType.getSelectedItem().toString();
    }

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
            java.util.logging.Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AppearanceSettingsDialog dialog = new AppearanceSettingsDialog(new javax.swing.JFrame(), true, null);
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
    private javax.swing.JComboBox cbFontType;
    private javax.swing.JCheckBox checkBoxBold;
    private javax.swing.JCheckBox checkBoxGuardar;
    private javax.swing.JCheckBox checkBoxItalic;
    private javax.swing.JCheckBox checkBoxLocalizacao;
    private javax.swing.JCheckBox checkBoxRazao;
    private javax.swing.JCheckBox checkBoxTimestamp;
    private javax.swing.JColorChooser colorChooser;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblSignature;
    private model.PreviewPanel previewPanel1;
    // End of variables declaration//GEN-END:variables
}
