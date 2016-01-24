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

import com.itextpdf.text.FontFactory;
import controller.Bundle;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import model.CCSignatureSettings;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 *
 * @author admin
 */
public class AppearanceSettingsDialog extends javax.swing.JDialog {

    private CCSignatureSettings signatureSettings;
    private HashMap<com.itextpdf.text.Font, String> hmFonts;

    /**
     * Creates new form NewJDialog
     *
     * @param parent
     * @param modal
     * @param signatureSettings
     */
    public AppearanceSettingsDialog(java.awt.Frame parent, boolean modal, CCSignatureSettings signatureSettings) {
        super(parent, modal);
        initComponents();
        this.signatureSettings = signatureSettings;

        updateText();

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
        hmFonts = getAllFonts(dirs);
        ArrayList<com.itextpdf.text.Font> alFonts = new ArrayList<>(hmFonts.keySet());

        Collections.sort(alFonts, new Comparator<com.itextpdf.text.Font>() {
            @Override
            public int compare(com.itextpdf.text.Font f1, com.itextpdf.text.Font f2) {
                return f1.getFamilyname().compareToIgnoreCase(f2.getFamilyname());
            }
        });

        DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
        for (com.itextpdf.text.Font font : alFonts) {
            dcbm.addElement(font.getFamilyname());
        }
        cbFontType.setModel(dcbm);

        String fontLocation = signatureSettings.getAppearance().getFontLocation();
        boolean italic = signatureSettings.getAppearance().isItalic();
        boolean bold = signatureSettings.getAppearance().isBold();
        boolean showName = signatureSettings.getAppearance().isShowName();
        boolean showLocation = signatureSettings.getAppearance().isShowLocation();
        boolean showReason = signatureSettings.getAppearance().isShowReason();
        boolean showDate = signatureSettings.getAppearance().isShowDate();
        int align = signatureSettings.getAppearance().getAlign();

        switch (align) {
            case 0:
                cbAlign.setSelectedIndex(0);
                break;
            case 1:
                cbAlign.setSelectedIndex(1);
                break;
            case 2:
                cbAlign.setSelectedIndex(2);
                break;
            default:
                cbAlign.setSelectedIndex(0);
        }

        previewPanel1.setReason(signatureSettings.getReason());
        previewPanel1.setShowDate(showDate);

        if (signatureSettings.getCcAlias() != null) {
            previewPanel1.setAliasName(signatureSettings.getCcAlias().getName());
        } else {
            previewPanel1.setAliasName(Bundle.getBundle().getString("name"));
        }

        if (!signatureSettings.getLocation().isEmpty()) {
            previewPanel1.setLocation(signatureSettings.getLocation());
            cbShowLocation.setSelected(showLocation);
            previewPanel1.setShowLocation(showLocation);
        } else {
            cbShowLocation.setEnabled(false);
            cbShowLocation.setSelected(false);
        }

        cbShowName.setSelected(showName);
        previewPanel1.setShowName(showName);

        if (!signatureSettings.getReason().isEmpty()) {
            previewPanel1.setReason(signatureSettings.getReason());
            cbShowReason.setSelected(showReason);
            previewPanel1.setShowReason(showReason);
        } else {
            cbShowReason.setEnabled(false);
            cbShowReason.setSelected(false);
        }
        previewPanel1.setText(signatureSettings.getText());
        previewPanel1.setAlign(align);
        colorChooser.setPreviewPanel(new JPanel());
        Color color = signatureSettings.getAppearance().getFontColor();
        colorChooser.setColor(color);
        lblSampleText.setForeground(color);
        cbShowReason.setSelected(showReason);
        cbShowLocation.setSelected(showLocation);
        cbShowDateTime.setSelected(showDate);

        ColorSelectionModel model = colorChooser.getSelectionModel();
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Color newForegroundColor = colorChooser.getColor();
                lblSampleText.setForeground(newForegroundColor);
            }
        };
        model.addChangeListener(changeListener);

        if (fontLocation.contains("aCCinaPDF" + File.separator + "extrafonts")) {
            try {
                Font newFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontLocation));
                Font font = null;
                if (italic && bold) {
                    font = newFont.deriveFont(Font.ITALIC + Font.BOLD, 36);
                } else if (italic && !bold) {
                    font = newFont.deriveFont(Font.ITALIC, 36);
                } else if (!italic && bold) {
                    font = newFont.deriveFont(Font.BOLD, 36);
                } else {
                    font = newFont.deriveFont(Font.PLAIN, 36);
                }
                lblSampleText.setFont(font);
            } catch (FontFormatException | IOException ex) {
            }
        } else {
            if (italic && bold) {
                lblSampleText.setFont(new Font(fontLocation, Font.ITALIC + Font.BOLD, 36));
            } else if (italic && !bold) {
                lblSampleText.setFont(new Font(fontLocation, Font.ITALIC, 36));
            } else if (!italic && bold) {
                lblSampleText.setFont(new Font(fontLocation, Font.BOLD, 36));
            } else {
                lblSampleText.setFont(new Font(fontLocation, Font.PLAIN, 36));
            }
        }

        cbBold.setSelected(bold);
        cbItalic.setSelected(italic);

        updateSettings(fontLocation, bold, italic);

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

    private void updateText() {
        panelSignatureSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.signatureSettings")));
        panelFontSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.fontSettings")));
        lblShow.setText(Bundle.getBundle().getString("label.show"));
        cbShowName.setText(Bundle.getBundle().getString("name"));
        cbShowReason.setText(Bundle.getBundle().getString("reason"));
        cbShowLocation.setText(Bundle.getBundle().getString("location"));
        cbShowDateTime.setText(Bundle.getBundle().getString("dateAndTime"));
        lblAlign.setText(Bundle.getBundle().getString("label.align"));
        DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
        dcbm.addElement(WordUtils.capitalize(Bundle.getBundle().getString("left")));
        dcbm.addElement(WordUtils.capitalize(Bundle.getBundle().getString("center")));
        dcbm.addElement(WordUtils.capitalize(Bundle.getBundle().getString("right")));
        cbAlign.setModel(dcbm);
        lblSampleText.setText(Bundle.getBundle().getString("label.sampleText"));
        lblFontType.setText(Bundle.getBundle().getString("label.fontType"));
        cbBold.setText(Bundle.getBundle().getString("label.bold"));
        cbItalic.setText(Bundle.getBundle().getString("label.italic"));
        cbSetAsDefault.setText(Bundle.getBundle().getString("label.setAsDefault"));
        btnAccept.setText(Bundle.getBundle().getString("btn.accept"));
        btnCancel.setText(Bundle.getBundle().getString("btn.cancel"));
    }

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

    private void updateSettings(String font, boolean bold, boolean italic) {
        ComboBoxModel model2 = (ComboBoxModel) cbFontType.getModel();
        for (int i = 0; i < model2.getSize(); i++) {
            if (cbFontType.getItemAt(i).toString().equalsIgnoreCase(FontFactory.getFont(font).getFamilyname())) {
                cbFontType.setSelectedIndex(i);
            }
        }

        cbBold.setSelected(bold);
        cbItalic.setSelected(italic);
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
        panelFontSettings = new javax.swing.JPanel();
        lblSampleText = new javax.swing.JLabel();
        cbBold = new javax.swing.JCheckBox();
        cbItalic = new javax.swing.JCheckBox();
        lblFontType = new javax.swing.JLabel();
        cbFontType = new javax.swing.JComboBox();
        colorChooser = new javax.swing.JColorChooser();
        btnCancel = new javax.swing.JButton();
        btnAccept = new javax.swing.JButton();
        panelSignatureSettings = new javax.swing.JPanel();
        cbShowReason = new javax.swing.JCheckBox();
        cbShowLocation = new javax.swing.JCheckBox();
        previewPanel1 = new view.PreviewPanel();
        cbShowDateTime = new javax.swing.JCheckBox();
        lblShow = new javax.swing.JLabel();
        cbShowName = new javax.swing.JCheckBox();
        lblAlign = new javax.swing.JLabel();
        cbAlign = new javax.swing.JComboBox();
        cbSetAsDefault = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelFontSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Definições da Fonte"));

        lblSampleText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSampleText.setText("Texto Exemplo");
        lblSampleText.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cbBold.setText("Negrito");
        cbBold.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbBoldItemStateChanged(evt);
            }
        });

        cbItalic.setText("Itálico");
        cbItalic.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbItalicItemStateChanged(evt);
            }
        });

        lblFontType.setText("Tipo de Letra:");

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

        javax.swing.GroupLayout panelFontSettingsLayout = new javax.swing.GroupLayout(panelFontSettings);
        panelFontSettings.setLayout(panelFontSettingsLayout);
        panelFontSettingsLayout.setHorizontalGroup(
            panelFontSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFontSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFontSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSampleText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelFontSettingsLayout.createSequentialGroup()
                        .addComponent(lblFontType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbFontType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(colorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(panelFontSettingsLayout.createSequentialGroup()
                        .addComponent(cbBold)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbItalic)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelFontSettingsLayout.setVerticalGroup(
            panelFontSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFontSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSampleText, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFontSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFontType)
                    .addComponent(cbFontType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelFontSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbBold)
                    .addComponent(cbItalic))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 254, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnCancel.setText("Cancelar");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnAccept.setText("Aceitar");
        btnAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAcceptActionPerformed(evt);
            }
        });

        panelSignatureSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Definições da Assinatura"));

        cbShowReason.setSelected(true);
        cbShowReason.setText("Razão");
        cbShowReason.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbShowReasonItemStateChanged(evt);
            }
        });

        cbShowLocation.setSelected(true);
        cbShowLocation.setText("Localização");
        cbShowLocation.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbShowLocationItemStateChanged(evt);
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
            .addGap(0, 97, Short.MAX_VALUE)
        );

        cbShowDateTime.setSelected(true);
        cbShowDateTime.setText("Data e Hora");
        cbShowDateTime.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbShowDateTimeItemStateChanged(evt);
            }
        });

        lblShow.setText("Mostrar:");

        cbShowName.setSelected(true);
        cbShowName.setText("Nome");
        cbShowName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbShowNameItemStateChanged(evt);
            }
        });

        lblAlign.setText("Alinhar:");

        cbAlign.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Esquerda", "Centro", "Direita" }));
        cbAlign.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbAlignItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout panelSignatureSettingsLayout = new javax.swing.GroupLayout(panelSignatureSettings);
        panelSignatureSettings.setLayout(panelSignatureSettingsLayout);
        panelSignatureSettingsLayout.setHorizontalGroup(
            panelSignatureSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSignatureSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSignatureSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelSignatureSettingsLayout.createSequentialGroup()
                        .addComponent(lblShow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbShowName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbShowReason)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbShowLocation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbShowDateTime)
                        .addGap(45, 45, 45)
                        .addComponent(lblAlign)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbAlign, 0, 157, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelSignatureSettingsLayout.setVerticalGroup(
            panelSignatureSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSignatureSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(panelSignatureSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSignatureSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbShowReason)
                        .addComponent(lblShow)
                        .addComponent(cbShowName))
                    .addGroup(panelSignatureSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbShowLocation)
                        .addComponent(cbShowDateTime)
                        .addComponent(lblAlign)
                        .addComponent(cbAlign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        cbSetAsDefault.setText("Definir como definições padrão");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelFontSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbSetAsDefault)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAccept)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel))
                    .addComponent(panelSignatureSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(panelSignatureSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelFontSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSetAsDefault)
                    .addComponent(btnCancel)
                    .addComponent(btnAccept))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void cbFontTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbFontTypeItemStateChanged
        changeLabel();
    }//GEN-LAST:event_cbFontTypeItemStateChanged

    private void cbBoldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbBoldItemStateChanged
        changeLabel();
    }//GEN-LAST:event_cbBoldItemStateChanged

    private void cbItalicItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbItalicItemStateChanged
        changeLabel();
    }//GEN-LAST:event_cbItalicItemStateChanged

    private void cbShowReasonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbShowReasonItemStateChanged
        previewPanel1.setShowReason(cbShowReason.isSelected());
    }//GEN-LAST:event_cbShowReasonItemStateChanged

    private void cbShowLocationItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbShowLocationItemStateChanged
        previewPanel1.setShowLocation(cbShowLocation.isSelected());
    }//GEN-LAST:event_cbShowLocationItemStateChanged

    private void colorChooserMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorChooserMousePressed

    }//GEN-LAST:event_colorChooserMousePressed

    private void cbShowDateTimeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbShowDateTimeItemStateChanged
        previewPanel1.setShowDate(cbShowDateTime.isSelected());
    }//GEN-LAST:event_cbShowDateTimeItemStateChanged

    private void btnAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAcceptActionPerformed
        if (cbSetAsDefault.isSelected()) {
            writeFile();
        }

        signatureSettings.getAppearance().setShowName(cbShowName.isSelected());
        signatureSettings.getAppearance().setShowReason(cbShowReason.isSelected());
        signatureSettings.getAppearance().setShowLocation(cbShowLocation.isSelected());
        signatureSettings.getAppearance().setShowDate(cbShowDateTime.isSelected());
        signatureSettings.getAppearance().setFontColor(colorChooser.getColor());
        signatureSettings.getAppearance().setBold(cbBold.isSelected());
        signatureSettings.getAppearance().setItalic(cbItalic.isSelected());
        String fontLocation = getFontLocationByName((String) cbFontType.getSelectedItem());
        signatureSettings.getAppearance().setFontLocation(fontLocation);
        signatureSettings.getAppearance().setAlign(cbAlign.getSelectedIndex());

        this.dispose();
    }//GEN-LAST:event_btnAcceptActionPerformed

    private void cbShowNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbShowNameItemStateChanged
        previewPanel1.setShowName(cbShowName.isSelected());
    }//GEN-LAST:event_cbShowNameItemStateChanged

    private void cbAlignItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbAlignItemStateChanged
        if (cbAlign.getSelectedItem().toString().equalsIgnoreCase(Bundle.getBundle().getString("left"))) {
            previewPanel1.setAlign(0);
        } else if (cbAlign.getSelectedItem().toString().equalsIgnoreCase(Bundle.getBundle().getString("center"))) {
            previewPanel1.setAlign(1);
        } else {
            previewPanel1.setAlign(2);
        }
        previewPanel1.repaint();
    }//GEN-LAST:event_cbAlignItemStateChanged

    private String getFontLocationByName(String name) {
        Set<Map.Entry<com.itextpdf.text.Font, String>> entries = hmFonts.entrySet();
        for (Map.Entry<com.itextpdf.text.Font, String> entry : entries) {
            if (entry.getKey().getFamilyname().equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void writeFile() {
        try {
            File file = new File("aCCinaPDF.cfg");
            if (file.exists()) {
                FileInputStream in = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(in);
                in.close();
                FileOutputStream fileOut;
                properties.setProperty("fontLocation", getFontLocationByName(cbFontType.getSelectedItem().toString()));
                properties.setProperty("fontColor", String.valueOf(colorChooser.getColor().getRGB()));
                properties.setProperty("fontItalic", String.valueOf(cbItalic.isSelected()));
                properties.setProperty("fontBold", String.valueOf(cbBold.isSelected()));
                properties.setProperty("showName", String.valueOf(cbShowName.isSelected()));
                properties.setProperty("showReason", String.valueOf(cbShowReason.isSelected()));
                properties.setProperty("showLocation", String.valueOf(cbShowLocation.isSelected()));
                properties.setProperty("showDate", String.valueOf(cbShowDateTime.isSelected()));
                switch (cbAlign.getSelectedIndex()) {
                    case 0:
                        properties.setProperty("textAlign", "0");
                        break;
                    case 1:
                        properties.setProperty("textAlign", "1");
                        break;
                    case 2:
                        properties.setProperty("textAlign", "2");
                        break;
                }

                fileOut = new FileOutputStream(file);
                properties.store(fileOut, "Settings");
                fileOut.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void changeLabel() {
        String fontLocation = getFontCbBox();

        try {
            Font newFont = Font.createFont(Font.TRUETYPE_FONT, new File(getFontLocationByName(fontLocation)));
            Font font = null;

            if (cbBold.isSelected() && cbItalic.isSelected()) {
                font = newFont.deriveFont(Font.ITALIC + Font.BOLD, 36);
            } else if (cbBold.isSelected() && !cbItalic.isSelected()) {
                font = newFont.deriveFont(Font.BOLD, 36);
            } else if (!cbBold.isSelected() && cbItalic.isSelected()) {
                font = newFont.deriveFont(Font.ITALIC, 36);
            } else {
                font = newFont.deriveFont(Font.PLAIN, 36);
            }
            lblSampleText.setFont(font);
            return;
        } catch (FontFormatException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (cbBold.isSelected() && cbItalic.isSelected()) {
            lblSampleText.setFont(new Font(fontLocation, Font.BOLD + Font.ITALIC, 36));
        } else if (cbBold.isSelected() && !cbItalic.isSelected()) {
            lblSampleText.setFont(new Font(fontLocation, Font.BOLD, 36));
        } else if (!cbBold.isSelected() && cbItalic.isSelected()) {
            lblSampleText.setFont(new Font(fontLocation, Font.ITALIC, (Integer) 36));
        } else {
            lblSampleText.setFont(new Font(cbFontType.getSelectedItem().toString(), Font.PLAIN, (Integer) 36));
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
    private javax.swing.JButton btnAccept;
    private javax.swing.JButton btnCancel;
    private javax.swing.JComboBox cbAlign;
    private javax.swing.JCheckBox cbBold;
    private javax.swing.JComboBox cbFontType;
    private javax.swing.JCheckBox cbItalic;
    private javax.swing.JCheckBox cbSetAsDefault;
    private javax.swing.JCheckBox cbShowDateTime;
    private javax.swing.JCheckBox cbShowLocation;
    private javax.swing.JCheckBox cbShowName;
    private javax.swing.JCheckBox cbShowReason;
    private javax.swing.JColorChooser colorChooser;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel lblAlign;
    private javax.swing.JLabel lblFontType;
    private javax.swing.JLabel lblSampleText;
    private javax.swing.JLabel lblShow;
    private javax.swing.JPanel panelFontSettings;
    private javax.swing.JPanel panelSignatureSettings;
    private view.PreviewPanel previewPanel1;
    // End of variables declaration//GEN-END:variables
}
