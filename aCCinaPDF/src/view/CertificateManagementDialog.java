/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.CCInstance;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import model.Settings;
import org.apache.commons.lang3.text.WordUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

/**
 *
 * @author ldiog
 */
public class CertificateManagementDialog extends javax.swing.JDialog {

    private final DefaultListModel dlm = new DefaultListModel();
    private KeyStore keystore = CCInstance.getInstance().getKeystore();
    private File lastOpened = null;
    private ArrayList<Certificate> alTrusted = null;
    private java.awt.Frame parent;

    /**
     * Creates new form CertificateManagementDialog
     *
     * @param parent
     * @param modal
     */
    public CertificateManagementDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.parent = parent;

        if (keystore.equals(CCInstance.getInstance().getDefaultKeystore())) {
            jRadioButton1.setSelected(true);
        } else {
            jRadioButton2.setSelected(true);
            tfCustomKeystore.setText(Settings.getSettings().getKeystorePath());
        }

        btnDetalhes.setEnabled(jList1.getSelectedIndices().length == 1);

        toggleButtons();
        refresh(keystore);
    }

    private void refresh(KeyStore ks) {
        if (ks == null) {
            dlm.removeAllElements();
            jList1.setModel(dlm);
            return;
        }

        alTrusted = null;
        dlm.removeAllElements();
        try {
            alTrusted = CCInstance.getInstance().getTrustedCertificatesFromKeystore(ks);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException ex) {
            return;
        }

        Collections.sort(alTrusted, new Comparator<Certificate>() {
            @Override
            public int compare(final Certificate c1, final Certificate c2) {
                return getCertificateCN(c1).compareTo(getCertificateCN(c2));
            }
        });

        for (Certificate c : alTrusted) {
            dlm.addElement(getCertificateCN(c));
        }
        this.keystore = ks;
        jList1.setModel(dlm);
    }

    private String getCertificateCN(Certificate cert) {
        X509Certificate x509cert = (X509Certificate) cert;
        org.bouncycastle.asn1.x500.X500Name x500name = null;
        try {
            x500name = new JcaX509CertificateHolder(x509cert).getSubject();
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(CertificatePropertiesDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        RDN rdn = null;
        try {
            rdn = x500name.getRDNs(BCStyle.CN)[0];
        } catch (Exception e) {
            return WordUtils.capitalize(x500name.toString());
        }

        return WordUtils.capitalize(IETFUtils.valueToString(rdn.getFirst().getValue()).toLowerCase());
    }

    private void loadCustomKeystore() {
        JFileChooser jfc = new JFileChooser();
        File path;
        if (lastOpened == null) {
            path = new File(System.getProperty("user.home"));
        } else {
            if (lastOpened.exists()) {
                path = lastOpened;
            } else {
                path = new File(System.getProperty("user.home"));
            }
        }
        jfc.setCurrentDirectory(path);
        boolean validKeystore = false;
        while (!validKeystore) {
            int ret = jfc.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                lastOpened = jfc.getSelectedFile();
                KeyStore ks = isValidKeystore(lastOpened, true);
                if (ks != null) {
                    validKeystore = true;
                    refresh(ks);
                }
            } else if (ret == JFileChooser.CANCEL_OPTION) {
                break;
            }
        }
        if (validKeystore) {
            tfCustomKeystore.setText(lastOpened.getAbsolutePath());
        }
    }

    private KeyStore isValidKeystore(File file, boolean showDialog) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, null);
            if (ks.aliases().hasMoreElements()) {
                return ks;
            } else {
                if (showDialog) {
                    JOptionPane.showMessageDialog(this, "A cadeia de certificados escolhida está vazia!", "", JOptionPane.INFORMATION_MESSAGE);
                }
                return ks;
            }
        } catch (java.security.cert.CertificateException | NoSuchAlgorithmException | KeyStoreException | FileNotFoundException e) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "O ficheiro não contém uma cadeia de certificados ou está corrompido!", "", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        } catch (IOException e) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "O ficheiro não contém uma cadeia de certificados ou está corrompido!", "", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        btnAddCert = new javax.swing.JButton();
        btnRemoveCert = new javax.swing.JButton();
        btnDetalhes = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        tfCustomKeystore = new javax.swing.JTextField();
        btnChangeCustomKeystorePath = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gestão de Certificados");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Certificados Confiáveis"));

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        btnAddCert.setText("+");
        btnAddCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCertActionPerformed(evt);
            }
        });

        btnRemoveCert.setText("-");
        btnRemoveCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveCertActionPerformed(evt);
            }
        });

        btnDetalhes.setText("Detalhes do Certificado");
        btnDetalhes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetalhesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddCert)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveCert)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDetalhes, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                        .addGap(263, 263, 263))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCert)
                    .addComponent(btnRemoveCert)
                    .addComponent(btnDetalhes))
                .addContainerGap())
        );

        jButton4.setText("Cancelar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Usar cadeia de certificados do aCCinaPDF");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Usar cadeia de certificados alternativa");
        jRadioButton2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButton2StateChanged(evt);
            }
        });
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        tfCustomKeystore.setEnabled(false);

        btnChangeCustomKeystorePath.setText("Alterar");
        btnChangeCustomKeystorePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeCustomKeystorePathActionPerformed(evt);
            }
        });

        jButton2.setText("Aceitar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfCustomKeystore, javax.swing.GroupLayout.PREFERRED_SIZE, 478, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnChangeCustomKeystorePath)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChangeCustomKeystorePath)
                    .addComponent(tfCustomKeystore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void btnChangeCustomKeystorePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeCustomKeystorePathActionPerformed
        loadCustomKeystore();
    }//GEN-LAST:event_btnChangeCustomKeystorePathActionPerformed

    private void jRadioButton2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButton2StateChanged

    }//GEN-LAST:event_jRadioButton2StateChanged

    private void btnRemoveCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveCertActionPerformed
        int[] indices = jList1.getSelectedIndices();
        if (indices.length != 0) {
            String msg = (indices.length == 1 ? "Tem a certeza que quer remover o certificado seleccionado da lista de certificados confiáveis?"
                    : "Tem a certeza que quer remover os " + indices.length + " certificados seleccionados da lista de certificados confiáveis?");

            Object[] options = {"Sim", "Não"};
            int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (opt == JOptionPane.NO_OPTION) {
                return;
            }
            final ArrayList<Certificate> toRemove = new ArrayList<>();
            for (int index : indices) {
                toRemove.add(alTrusted.get(index));
            }

            String password = getUserInputPassword();
            if (password == null) {
                return;
            }
            for (Certificate cert : toRemove) {
                removeCertFromKeystore(cert, password);
                alTrusted.remove(cert);
            }
        }
    }//GEN-LAST:event_btnRemoveCertActionPerformed

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        btnRemoveCert.setEnabled(jRadioButton2.isSelected() && jList1.getSelectedIndices().length > 0);
        btnDetalhes.setEnabled(jList1.getSelectedIndices().length == 1);
    }//GEN-LAST:event_jList1ValueChanged

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        toggleButtons();
        refresh(isValidKeystore(new File(tfCustomKeystore.getText()), false));
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        toggleButtons();
        refresh(CCInstance.getInstance().getDefaultKeystore());
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Properties properties = new Properties();
        String configFile = "aCCinaPDF.cfg";
        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (jRadioButton1.isSelected()) {
            if (!CCInstance.getInstance().getKeystore().equals(CCInstance.getInstance().getDefaultKeystore())) {
                CCInstance.getInstance().setKeystore(CCInstance.getInstance().getDefaultKeystore());
                Settings.getSettings().setKeystorePath(null);
                properties.remove("keystore");
            }
        } else {
            if (null == lastOpened) {
                lastOpened = new File(tfCustomKeystore.getText());
            }
            try {
                KeyStore ks = isValidKeystore(lastOpened, false);
                if (!ks.equals(CCInstance.getInstance().getKeystore())) {
                    CCInstance.getInstance().setKeystore(ks);
                    String keystorePath = lastOpened.getAbsolutePath();
                    Settings.getSettings().setKeystorePath(keystorePath);
                    properties.setProperty("keystore", keystorePath);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Seleccione uma keystore válida e volte a tentar", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(configFile);
            properties.store(fileOut, "Settings");
            fileOut.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro a guardar no ficheiro de configurações!", "", JOptionPane.ERROR_MESSAGE);
        }

        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnAddCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCertActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.showOpenDialog(null);

        File[] files = chooser.getSelectedFiles();
        final ArrayList<Certificate> toAddList = new ArrayList<>();
        for (File file : files) {
            try {
                CertificateFactory fact = CertificateFactory.getInstance("X.509");
                FileInputStream is = new FileInputStream(file);
                Certificate cert = fact.generateCertificate(is);
                toAddList.add(cert);
            } catch (Exception ex) {
            }
        }
        if (!toAddList.isEmpty()) {
            String password = getUserInputPassword();
            if (password == null) {
                return;
            }
            addCertsToKeystore(toAddList, password);
            refresh(keystore);
        }
    }//GEN-LAST:event_btnAddCertActionPerformed

    private void btnDetalhesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetalhesActionPerformed
        if (jList1.getSelectedIndices().length == 1) {
            int index = jList1.getSelectedIndex();
            Certificate cert = alTrusted.get(index);
            CertificatePropertiesDialog cpd = new CertificatePropertiesDialog(parent, true, (X509Certificate) cert);
            cpd.setLocationRelativeTo(null);
            cpd.setVisible(true);
        }
    }//GEN-LAST:event_btnDetalhesActionPerformed

    private void toggleButtons() {
        tfCustomKeystore.setVisible(jRadioButton2.isSelected());
        btnChangeCustomKeystorePath.setVisible(jRadioButton2.isSelected());
        btnAddCert.setEnabled(jRadioButton2.isSelected());
        btnRemoveCert.setEnabled(jRadioButton2.isSelected());
    }

    private String getUserInputPassword() {
        JPanel panel = new JPanel();
        JLabel lblInsertPassword = new JLabel("Introduza a password da cadeia de certificados:");
        JPasswordField pf = new JPasswordField(10);
        panel.add(lblInsertPassword);
        panel.add(pf);
        String[] options = new String[]{"OK", "Cancelar"};
        int option = JOptionPane.showOptionDialog(null, panel, null, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        if (option == JOptionPane.OK_OPTION) {
            char[] password = pf.getPassword();
            return new String(password);
        }
        return null;
    }

    private void addCertsToKeystore(ArrayList<Certificate> certList, String password) {
        try {
            if (null == lastOpened) {
                lastOpened = new File(tfCustomKeystore.getText());
            }
            FileOutputStream out = new FileOutputStream(lastOpened);
            for (Certificate cert : certList) {
                keystore.setCertificateEntry(getCertificateCN(cert), cert);
                keystore.store(out, password.toCharArray());
            }
            out.close();
        } catch (KeyStoreException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeCertFromKeystore(Certificate cert, String password) {
        FileOutputStream out = null;
        try {
            if (null == lastOpened) {
                lastOpened = new File(tfCustomKeystore.getText());
            }
            out = new FileOutputStream(lastOpened);
            keystore.deleteEntry(keystore.getCertificateAlias(cert));
            keystore.store(out, password.toCharArray());
            out.close();
            refresh(keystore);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Certificate readCertificateFromFile(File f) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        Certificate cert = null;
        while (in.available() > 0) {
            cert = cf.generateCertificate(in);
        }
        in.close();
        return cert;
    }

    private InputStream fullStream(String fname) throws IOException {
        FileInputStream fis = new FileInputStream(fname);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = new byte[dis.available()];
        dis.readFully(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return bais;
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CertificateManagementDialog.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CertificateManagementDialog.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CertificateManagementDialog.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CertificateManagementDialog.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CertificateManagementDialog dialog = new CertificateManagementDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnAddCert;
    private javax.swing.JButton btnChangeCustomKeystorePath;
    private javax.swing.JButton btnDetalhes;
    private javax.swing.JButton btnRemoveCert;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField tfCustomKeystore;
    // End of variables declaration//GEN-END:variables
}
