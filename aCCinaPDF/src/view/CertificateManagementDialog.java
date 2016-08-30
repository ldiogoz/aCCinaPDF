/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Bundle;
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

        updateText();

        if (keystore.equals(CCInstance.getInstance().getDefaultKeystore())) {
            rbUseDefaultKeystore.setSelected(true);
        } else {
            rbUseCustomKeystore.setSelected(true);
            tfCustomKeystore.setText(Settings.getSettings().getKeystorePath());
        }

        btnCertificateDetails.setEnabled(jList1.getSelectedIndices().length == 1);

        toggleButtons();
        refresh(keystore);
    }

    private void updateText() {
        setTitle(Bundle.getBundle().getString("title.ceritificateManagement"));
        rbUseDefaultKeystore.setText(Bundle.getBundle().getString("label.useDefaultKeystore"));
        rbUseCustomKeystore.setText(Bundle.getBundle().getString("label.useCustomKeystore"));
        btnChange.setText(Bundle.getBundle().getString("btn.change"));
        panelTrustedCertificates.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.trustedCertificates")));
        btnCertificateDetails.setText(Bundle.getBundle().getString("label.certificateDetails"));
        btnAccept.setText(Bundle.getBundle().getString("btn.accept"));
        btnCancel.setText(Bundle.getBundle().getString("btn.cancel"));
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
                    JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("emptyChain"), "", JOptionPane.INFORMATION_MESSAGE);
                }
                return ks;
            }
        } catch (java.security.cert.CertificateException | NoSuchAlgorithmException | KeyStoreException | FileNotFoundException e) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("fileNotKeystoreOrCorrupted"), "", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        } catch (IOException e) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("fileNotKeystoreOrCorrupted"), "", JOptionPane.ERROR_MESSAGE);
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
        panelTrustedCertificates = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        btnAddCert = new javax.swing.JButton();
        btnRemoveCert = new javax.swing.JButton();
        btnCertificateDetails = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        rbUseDefaultKeystore = new javax.swing.JRadioButton();
        rbUseCustomKeystore = new javax.swing.JRadioButton();
        tfCustomKeystore = new javax.swing.JTextField();
        btnChange = new javax.swing.JButton();
        btnAccept = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Gestão de Certificados");

        panelTrustedCertificates.setBorder(javax.swing.BorderFactory.createTitledBorder("Certificados Confiáveis"));

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

        btnCertificateDetails.setText("Detalhes do Certificado");
        btnCertificateDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCertificateDetailsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTrustedCertificatesLayout = new javax.swing.GroupLayout(panelTrustedCertificates);
        panelTrustedCertificates.setLayout(panelTrustedCertificatesLayout);
        panelTrustedCertificatesLayout.setHorizontalGroup(
            panelTrustedCertificatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTrustedCertificatesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTrustedCertificatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTrustedCertificatesLayout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addGroup(panelTrustedCertificatesLayout.createSequentialGroup()
                        .addComponent(btnAddCert)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveCert)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCertificateDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                        .addGap(263, 263, 263))))
        );
        panelTrustedCertificatesLayout.setVerticalGroup(
            panelTrustedCertificatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTrustedCertificatesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelTrustedCertificatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCert)
                    .addComponent(btnRemoveCert)
                    .addComponent(btnCertificateDetails))
                .addContainerGap())
        );

        btnCancel.setText("Cancelar");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbUseDefaultKeystore);
        rbUseDefaultKeystore.setSelected(true);
        rbUseDefaultKeystore.setText("Usar cadeia de certificados do aCCinaPDF");
        rbUseDefaultKeystore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbUseDefaultKeystoreActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbUseCustomKeystore);
        rbUseCustomKeystore.setText("Usar cadeia de certificados alternativa");
        rbUseCustomKeystore.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbUseCustomKeystoreStateChanged(evt);
            }
        });
        rbUseCustomKeystore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbUseCustomKeystoreActionPerformed(evt);
            }
        });

        tfCustomKeystore.setEnabled(false);

        btnChange.setText("Alterar");
        btnChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeActionPerformed(evt);
            }
        });

        btnAccept.setText("Aceitar");
        btnAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAcceptActionPerformed(evt);
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
                            .addComponent(rbUseDefaultKeystore)
                            .addComponent(rbUseCustomKeystore)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfCustomKeystore, javax.swing.GroupLayout.PREFERRED_SIZE, 478, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnChange)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelTrustedCertificates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnAccept)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbUseDefaultKeystore)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbUseCustomKeystore, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChange)
                    .addComponent(tfCustomKeystore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTrustedCertificates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnAccept))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeActionPerformed
        loadCustomKeystore();
    }//GEN-LAST:event_btnChangeActionPerformed

    private void rbUseCustomKeystoreStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbUseCustomKeystoreStateChanged

    }//GEN-LAST:event_rbUseCustomKeystoreStateChanged

    private void btnRemoveCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveCertActionPerformed
        int[] indices = jList1.getSelectedIndices();
        if (indices.length != 0) {
            String msg = (indices.length == 1 ? Bundle.getBundle().getString("removeCert1")
                    : Bundle.getBundle().getString("removeCert2") + " " + indices.length + " " + Bundle.getBundle().getString("removeCert3"));

            Object[] options = {Bundle.getBundle().getString("yes"), Bundle.getBundle().getString("no")};
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
        btnRemoveCert.setEnabled(rbUseCustomKeystore.isSelected() && jList1.getSelectedIndices().length > 0);
        btnCertificateDetails.setEnabled(jList1.getSelectedIndices().length == 1);
    }//GEN-LAST:event_jList1ValueChanged

    private void rbUseCustomKeystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbUseCustomKeystoreActionPerformed
        toggleButtons();
        refresh(isValidKeystore(new File(tfCustomKeystore.getText()), false));
    }//GEN-LAST:event_rbUseCustomKeystoreActionPerformed

    private void rbUseDefaultKeystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbUseDefaultKeystoreActionPerformed
        toggleButtons();
        refresh(CCInstance.getInstance().getDefaultKeystore());
    }//GEN-LAST:event_rbUseDefaultKeystoreActionPerformed

    private void btnAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAcceptActionPerformed
        Properties properties = new Properties();
        String configFile = "aCCinaPDF.cfg";
        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException ex) {
            Logger.getLogger(CertificateManagementDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (rbUseDefaultKeystore.isSelected()) {
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
                JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("selectValidKeystore"), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(configFile);
            properties.store(fileOut, "Settings");
            fileOut.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("errorSavingConfigFile"), "", JOptionPane.ERROR_MESSAGE);
        }

        this.dispose();
    }//GEN-LAST:event_btnAcceptActionPerformed

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

    private void btnCertificateDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCertificateDetailsActionPerformed
        if (jList1.getSelectedIndices().length == 1) {
            int index = jList1.getSelectedIndex();
            Certificate cert = alTrusted.get(index);
            CertificatePropertiesDialog cpd = new CertificatePropertiesDialog(parent, true, (X509Certificate) cert);
            cpd.setLocationRelativeTo(null);
            cpd.setVisible(true);
        }
    }//GEN-LAST:event_btnCertificateDetailsActionPerformed

    private void toggleButtons() {
        tfCustomKeystore.setVisible(rbUseCustomKeystore.isSelected());
        btnChange.setVisible(rbUseCustomKeystore.isSelected());
        btnAddCert.setEnabled(rbUseCustomKeystore.isSelected());
        btnRemoveCert.setEnabled(rbUseCustomKeystore.isSelected());
    }

    private String getUserInputPassword() {
        JPanel panel = new JPanel();
        JLabel lblInsertPassword = new JLabel(Bundle.getBundle().getString("insertKeystorePassword"));
        JPasswordField pf = new JPasswordField(10);
        panel.add(lblInsertPassword);
        panel.add(pf);
        String[] options = new String[]{Bundle.getBundle().getString("btn.ok"), Bundle.getBundle().getString("btn.cancel")};
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
    private javax.swing.JButton btnAccept;
    private javax.swing.JButton btnAddCert;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCertificateDetails;
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnRemoveCert;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelTrustedCertificates;
    private javax.swing.JRadioButton rbUseCustomKeystore;
    private javax.swing.JRadioButton rbUseDefaultKeystore;
    private javax.swing.JTextField tfCustomKeystore;
    // End of variables declaration//GEN-END:variables
}
