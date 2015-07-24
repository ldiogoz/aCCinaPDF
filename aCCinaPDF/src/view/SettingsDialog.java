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
import controller.Logger;
import model.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import model.CCSignatureSettings;

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
        loadSettings();
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

        String renderQuality = null;
        String pdfVersion = null;
        String prefix = null;
        String signatureWidthString = null;
        String signatureHeightString = null;
        try {
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
            JOptionPane.showMessageDialog(this, "O ficheiro de configurações não foi encontrado\nFoi criado um novo ficheiro de configurações", "", JOptionPane.INFORMATION_MESSAGE);
        }
        propertiesRead.load(new FileInputStream(configFile));
        value = propertiesRead.getProperty(parameter);
        if (value == null) {
            CCSignatureSettings signatureSettings = new CCSignatureSettings(false);
            JOptionPane.showMessageDialog(this, "O ficheiro de configurações está corrompido\nFoi criado um novo ficheiro de configurações", "", JOptionPane.INFORMATION_MESSAGE);
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

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tfHeight = new javax.swing.JTextField();
        tfWidth = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        tfPrefix = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Versão PDF:");
        jLabel1.setToolTipText("Versão que os documentos terão após a assinatura");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setToolTipText("Versão PDF que os documentos terão após a assinatura");

        jButton1.setText("Cancelar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Guardar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Preferências da Assinatura"));

        jLabel2.setText("Altura padrão:");

        jLabel3.setText("Largura padrão:");

        jLabel4.setText("pixéis");

        jLabel5.setText("pixéis");

        jLabel6.setText("Prefixo: ");
        jLabel6.setToolTipText("Prefixo (Nome) que a assinatura terá");

        tfPrefix.setToolTipText("Prefixo (Nome) que a assinatura terá");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(tfHeight))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(tfPrefix)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfWidth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tfPrefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        jLabel7.setText("Qualidade da renderização PDF:");
        jLabel7.setToolTipText("Qualidade com que é renderizado(mostrado) o PDF. Em computadores de baixa performance, deve ser usada a qualidade Baixa");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alta", "Média", "Baixa" }));
        jComboBox2.setToolTipText("Qualidade com que é renderizado(mostrado) o PDF. Em computadores de baixa performance, deve ser usada a qualidade Baixa");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int signatureHeight = 0;
        int signatureWidth = 0;
        try {
            signatureHeight = Integer.parseInt(tfHeight.getText());
            signatureWidth = Integer.parseInt(tfWidth.getText());

        } catch (NumberFormatException ex) {
            Logger.getLogger().addEntry(ex);
        }
        if (signatureHeight < 1) {
            JOptionPane.showMessageDialog(this, "Altura de assinatura inválida, insira um número maior que 0", "Altura inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (signatureWidth < 1) {
            JOptionPane.showMessageDialog(this, "Largura de assinatura inválida, insira um número maior que 0", "Largura inválida", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(getParent(), "Erro a guardar as definições!\nO ficheiro de configurações foi eliminado!", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            controller.Logger.getLogger().addEntry(ex);
            JOptionPane.showMessageDialog(getParent(), "Erro a guardar as definições - Ver log", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField tfHeight;
    private javax.swing.JTextField tfPrefix;
    private javax.swing.JTextField tfWidth;
    // End of variables declaration//GEN-END:variables
}
