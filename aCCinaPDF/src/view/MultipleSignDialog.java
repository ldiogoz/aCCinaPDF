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

import com.itextpdf.text.DocumentException;
import controller.CCInstance;
import exception.SignatureFailedException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import listener.SignatureListener;
import model.CCSignatureSettings;

/**
 *
 * @author Toshiba
 */
public class MultipleSignDialog extends javax.swing.JDialog {

    private final ArrayList<File> signedDocsList = new ArrayList<>();

    /**
     * Creates new form MultipleSignStatusDialog
     *
     * @param parent
     * @param modal
     * @param alFiles
     * @param settings
     * @param dest
     */
    public MultipleSignDialog(java.awt.Frame parent, boolean modal, final ArrayList<File> alFiles, final CCSignatureSettings settings, final String dest) {
        super(parent, modal);
        initComponents();

        jProgressBar1.setMinimum(0);
        jProgressBar1.setMaximum(alFiles.size());
        jProgressBar1.setString("Assinados: 0 de " + alFiles.size());

        final DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        final SignatureListener sl = new SignatureListener() {

            @Override
            public void onSignatureComplete(String filename, boolean valid, String message) {
                dtm.addRow(new Object[]{filename, (valid ? "Assinatura aplicada com sucesso" : "Assinatura falhou") + (message.isEmpty() ? "" : " - " + message)});
            }
        };

        Runnable r = new Runnable() {
            @Override
            public void run() {
                int numSigned = 0;
                btnFechar.setEnabled(false);
                for (File file : alFiles) {
                    String destinationPath = "";
                    boolean validPath = false;
                    if (dest == null) {
                        if (file.getName().endsWith(".pdf")) {
                            destinationPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4).concat("(aCCinado).pdf");
                            if (new File(destinationPath).exists()) {
                                int num = 1;
                                while (!validPath) {
                                    destinationPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4).concat("(aCCinado" + num + ").pdf");
                                    if (new File(destinationPath).exists()) {
                                        destinationPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4).concat("(aCCinado).pdf");
                                        num++;
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        if (file.getName().endsWith(".pdf")) {
                            destinationPath = dest + File.separator + file.getName().substring(0, file.getName().length() - 4).concat("(aCCinado).pdf");
                            if (new File(destinationPath).exists()) {
                                int num = 1;
                                while (!validPath) {
                                    destinationPath = dest + File.separator + file.getName().substring(0, file.getName().length() - 4).concat("(aCCinado" + num + ").pdf");
                                    if (new File(destinationPath).exists()) {
                                        destinationPath = dest + File.separator + file.getName().substring(0, file.getName().length() - 4).concat("(aCCinado).pdf");
                                        num++;
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    try {
                        // Aplicar
                        if (CCInstance.getInstance().signPdf(file.getAbsolutePath(), destinationPath, settings, sl)) {
                            signedDocsList.add(new File(destinationPath));
                            numSigned++;
                            jProgressBar1.setValue(numSigned);
                            jProgressBar1.setString("Assinados: " + numSigned + " de " + alFiles.size());

                        } else {
                            //JOptionPane.showMessageDialog(this, "Erro desconhecido: ver log", "Assinatura falhou", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (CertificateException | IOException | DocumentException | KeyStoreException | NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
                        Logger.getLogger(MultipleSignDialog.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SignatureFailedException ex) {
                        if (ex.getLocalizedMessage().equals("Acção cancelada pelo utilizador!")) {
                            String msg = "Deseja cancelar a assinatura dos restantes documentos?";
                            Object[] options = {"Sim", "Não"};
                            int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                            if (opt == JOptionPane.YES_OPTION) {
                                jProgressBar1.setValue(jProgressBar1.getMaximum());
                                jProgressBar1.setString("A assinatura dos restantes documentos foi cancelada");
                                break;
                            }
                        }
                    }
                }
                btnFechar.setEnabled(true);
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public ArrayList<File> getSignedDocsList() {
        return signedDocsList;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        btnFechar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jProgressBar1.setStringPainted(true);

        btnFechar.setText("Fechar");
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Ficheiro", "Resultado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFechar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnFechar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnFecharActionPerformed

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
            java.util.logging.Logger.getLogger(MultipleSignDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MultipleSignDialog dialog = new MultipleSignDialog(new javax.swing.JFrame(), true, null, null, null);
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
    private javax.swing.JButton btnFechar;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
