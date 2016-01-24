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

import controller.Bundle;
import controller.CCInstance;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.lang3.text.WordUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

/**
 *
 * @author Diogo
 */
public class CertificatePropertiesDialog extends javax.swing.JDialog {

    private java.awt.Frame parent;
    private final ArrayList<X509Certificate> certChainList = new ArrayList<>();
    private X509Certificate selectedCertificate;

    /**
     * Creates new form CertificatePropertiesDialog
     *
     * @param parent
     * @param modal
     * @param x509certificate
     */
    public CertificatePropertiesDialog(java.awt.Frame parent, boolean modal, X509Certificate x509certificate) {
        super(parent, modal);
        this.parent = parent;
        initComponents();
        updateText();
        jTextField1.setEditable(false);
        jTextField2.setEditable(false);
        jTextField3.setEditable(false);
        jTextField4.setEditable(false);
        jTextField5.setEditable(false);
        jTextField6.setEditable(false);
        jTextField7.setEditable(false);
        jTextField9.setEditable(false);
        jTextField10.setEditable(false);
        jTextField11.setEditable(false);
        jTextField12.setEditable(false);

        Certificate[] certificateChain = null;
        try {
            certificateChain = CCInstance.getInstance().getCompleteTrustedCertificateChain(x509certificate);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CertificatePropertiesDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        setupTree(certificateChain);
        setCertificateProperties(x509certificate);
        jTree1.setSelectionRow(jTree1.getRowCount() - 1);
        lastSelectedRow = (jTree1.getRowCount() - 1);
    }

    /**
     * Creates new form CertificatePropertiesDialog
     *
     * @param parent
     * @param modal
     * @param certificateChain
     */
    public CertificatePropertiesDialog(java.awt.Frame parent, boolean modal, Certificate[] certificateChain) {
        super(parent, modal);
        this.parent = parent;
        initComponents();
        updateText();
        jTextField1.setEditable(false);
        jTextField2.setEditable(false);
        jTextField3.setEditable(false);
        jTextField4.setEditable(false);
        jTextField5.setEditable(false);
        jTextField6.setEditable(false);
        jTextField7.setEditable(false);
        jTextField9.setEditable(false);
        jTextField10.setEditable(false);
        jTextField11.setEditable(false);
        jTextField12.setEditable(false);

        setupTree(certificateChain);
        setCertificateProperties((X509Certificate) certificateChain[0]);
        jTree1.setSelectionRow(jTree1.getRowCount() - 1);
        lastSelectedRow = (jTree1.getRowCount() - 1);
    }

    private void updateText() {
        lblCertificateChain.setText(Bundle.getBundle().getString("label.certificateChain"));
        btnExportCertificate.setText(Bundle.getBundle().getString("btn.exportCertificate"));
        panelSubject.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.certificateSubject")));
        panelIssuer.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.certificateIssuedBy")));
        panelValidity.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.validity")));
        panelUse.setBorder(javax.swing.BorderFactory.createTitledBorder(Bundle.getBundle().getString("panel.use")));
        lblSubjectCN.setText(Bundle.getBundle().getString("cert.cn") + ":");
        lblSubjectOU1.setText(Bundle.getBundle().getString("cert.ou") + ":");
        lblSubjectOU2.setText(Bundle.getBundle().getString("cert.ou") + ":");
        lblSubjectO.setText(Bundle.getBundle().getString("cert.o") + ":");
        lblSubjectC.setText(Bundle.getBundle().getString("cert.c") + ":");
        lblIssuerCN.setText(Bundle.getBundle().getString("cert.cn") + ":");
        lblIssuerOU.setText(Bundle.getBundle().getString("cert.ou") + ":");
        lblIssuerO.setText(Bundle.getBundle().getString("cert.o") + ":");
        lblIssuerC.setText(Bundle.getBundle().getString("cert.c") + ":");
        lblValidSince.setText(Bundle.getBundle().getString("cert.v1") + ":");
        lblValidUntil.setText(Bundle.getBundle().getString("cert.v2") + ":");
        btnClose.setText(Bundle.getBundle().getString("btn.close"));
    }

    private void setCertificateProperties(X509Certificate x509Certificate) {
        selectedCertificate = x509Certificate;
        jTextField1.setText(null);
        jTextField2.setText(null);
        jTextField3.setText(null);
        jTextField4.setText(null);
        jTextField5.setText(null);
        jTextField6.setText(null);
        jTextField7.setText(null);
        jTextField9.setText(null);
        jTextField10.setText(null);
        jTextField11.setText(null);
        jTextField12.setText(null);

        X500Name x500subject = null;
        X500Name x500issuer = null;
        try {
            x500subject = new JcaX509CertificateHolder(x509Certificate).getSubject();
            x500issuer = new JcaX509CertificateHolder(x509Certificate).getIssuer();
        } catch (CertificateEncodingException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }

        RDN subjectCN = null;
        if (x500subject.getRDNs(BCStyle.CN).length > 0) {
            subjectCN = x500subject.getRDNs(BCStyle.CN)[0];
        }
        RDN subjectOU1 = null;
        if (x500subject.getRDNs(BCStyle.OU).length >= 1) {
            subjectOU1 = x500subject.getRDNs(BCStyle.OU)[0];
            jTextField2.setText(IETFUtils.valueToString(subjectOU1.getFirst().getValue()));
            jTextField2.setCaretPosition(0);
        }
        RDN subjectOU2 = null;
        if (x500subject.getRDNs(BCStyle.OU).length >= 2) {
            subjectOU2 = x500subject.getRDNs(BCStyle.OU)[1];
            jTextField3.setText(IETFUtils.valueToString(subjectOU2.getFirst().getValue()));
            jTextField3.setCaretPosition(0);
        }
        RDN subjectO = null;
        if (x500subject.getRDNs(BCStyle.O).length > 0) {
            subjectO = x500subject.getRDNs(BCStyle.O)[0];
        }
        RDN subjectC = null;
        if (x500subject.getRDNs(BCStyle.C).length > 0) {
            subjectC = x500subject.getRDNs(BCStyle.C)[0];
        }
        if (!x500issuer.equals(x500subject)) {
            RDN issuerCN = x500issuer.getRDNs(BCStyle.CN)[0];
            if (1 == x500issuer.getRDNs(BCStyle.OU).length) {
                RDN issuerOU1 = x500issuer.getRDNs(BCStyle.OU)[0];
                jTextField7.setText(IETFUtils.valueToString(issuerOU1.getFirst().getValue()));
                jTextField7.setCaretPosition(0);
            }
            RDN issuerO = x500issuer.getRDNs(BCStyle.O)[0];
            RDN issuerC = x500issuer.getRDNs(BCStyle.C)[0];

            jTextField6.setText(IETFUtils.valueToString(issuerCN.getFirst().getValue()));
            jTextField6.setCaretPosition(0);
            jTextField9.setText(IETFUtils.valueToString(issuerO.getFirst().getValue()));
            jTextField9.setCaretPosition(0);
            jTextField10.setText(IETFUtils.valueToString(issuerC.getFirst().getValue()));
            jTextField10.setCaretPosition(0);
        }

        Date since = x509Certificate.getNotBefore();
        Date until = x509Certificate.getNotAfter();

        jTextField1.setText(WordUtils.capitalize(IETFUtils.valueToString(subjectCN.getFirst().getValue()).toLowerCase()));
        jTextField1.setCaretPosition(0);
        if (subjectO != null) {
            jTextField4.setText(IETFUtils.valueToString(subjectO.getFirst().getValue()));
        }
        jTextField4.setCaretPosition(0);
        if (subjectC != null) {
            jTextField5.setText(IETFUtils.valueToString(subjectC.getFirst().getValue()));
        }
        jTextField5.setCaretPosition(0);

        jTextField11.setText(since.toLocaleString());
        jTextField11.setCaretPosition(0);
        jTextField12.setText(until.toLocaleString());
        jTextField12.setCaretPosition(0);

        boolean usage[] = x509Certificate.getKeyUsage();
        if (null != usage) {
            boolean digitalSignature = usage[0];
            boolean nonRepudiation = usage[1];
            boolean keyEncipherment = usage[2];
            boolean dataEncipherment = usage[3];
            boolean keyAgreement = usage[4];
            boolean keyCertSign = usage[5];
            boolean cRLSign = usage[6];
            boolean encipherOnly = usage[7];
            boolean decipherOnly = usage[8];

            String uso = (digitalSignature ? Bundle.getBundle().getString("digitalSignature") + ", " : "")
                    + (nonRepudiation ? Bundle.getBundle().getString("nonRepudiation") + ", " : "")
                    + (keyEncipherment ? Bundle.getBundle().getString("keyEncipherment") + ", " : "")
                    + (dataEncipherment ? Bundle.getBundle().getString("dataEncipherment") + ", " : "")
                    + (keyAgreement ? Bundle.getBundle().getString("keyAgreement") + ", " : "")
                    + (keyCertSign ? Bundle.getBundle().getString("keyCertSign") + ", " : "")
                    + (cRLSign ? Bundle.getBundle().getString("cRLSign") + ", " : "")
                    + (encipherOnly ? Bundle.getBundle().getString("encipherOnly") + ", " : "")
                    + (decipherOnly ? Bundle.getBundle().getString("decipherOnly") + ", " : "");

            if (uso.length() == 0) {
                lblUso.setText(Bundle.getBundle().getString("label.none"));
            } else if (uso.endsWith(", ")) {
                lblUso.setText(uso.substring(0, uso.length() - 2));
            }
        } else {
            lblUso.setText(Bundle.getBundle().getString("unknown"));
        }

    }

    private void setupTree(Certificate[] certificateChain) {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) jTree1.getCellRenderer();
        Icon closedIcon = new ImageIcon(MainWindow.class.getResource("/image/certificate.png"));
        renderer.setLeafIcon(closedIcon);
        renderer.setOpenIcon(closedIcon);
        renderer.setClosedIcon(closedIcon);
        BasicTreeUI treeUI = (BasicTreeUI) jTree1.getUI();
        treeUI.setCollapsedIcon(null);
        treeUI.setExpandedIcon(null);

        if (null != certificateChain) {
            if (certificateChain.length > 0) {
                DefaultMutableTreeNode dmtn[] = new DefaultMutableTreeNode[certificateChain.length];

                boolean needCheck = true;
                for (int i = (certificateChain.length - 1); i >= 0; i--) {
                    if (!needCheck) {
                        dmtn[i] = new DefaultMutableTreeNode(getCertificateCN(certificateChain[i]));
                        if (i < (certificateChain.length - 1)) {
                            dmtn[i + 1].add(dmtn[i]);
                        }
                        certChainList.add((X509Certificate) certificateChain[i]);
                        continue;
                    }
                    if (CCInstance.getInstance().isTrustedCertificate((X509Certificate) certificateChain[i])) {
                        dmtn[i] = new DefaultMutableTreeNode(getCertificateCN(certificateChain[i]));
                        needCheck = false;
                    } else if (CCInstance.getInstance().hasTrustedIssuerCertificate((X509Certificate) certificateChain[i]) != null) {
                        dmtn[i] = new DefaultMutableTreeNode(getCertificateCN(certificateChain[i]));
                    } else {
                        dmtn[i] = new DefaultMutableTreeNode("(!)" + getCertificateCN(certificateChain[i]));

                    }
                    if (i < (certificateChain.length - 1)) {
                        dmtn[i + 1].add(dmtn[i]);
                    }
                    certChainList.add((X509Certificate) certificateChain[i]);
                }

                TreeModel tm = new DefaultTreeModel(dmtn[certificateChain.length - 1]);
                jTree1.setModel(tm);
                expandTree(jTree1);
            }
        }

        if (null != jTree1.getTreeExpansionListeners()) {
            if (null != jTree1.getTreeExpansionListeners()[0]) {
                jTree1.removeTreeExpansionListener(jTree1.getTreeExpansionListeners()[0]);
            }
        }
    }

    private String getCertificateCN(Certificate cert) {
        X509Certificate x509cert = (X509Certificate) cert;
        org.bouncycastle.asn1.x500.X500Name x500name = null;
        try {
            x500name = new JcaX509CertificateHolder(x509cert).getSubject();
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(CertificatePropertiesDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        RDN rdn = x500name.getRDNs(BCStyle.CN)[0];

        return WordUtils.capitalize(IETFUtils.valueToString(rdn.getFirst().getValue()).toLowerCase());
    }

    private void expandTree(JTree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root));
    }

    private void expandAll(JTree tree, TreePath path) {
        TreeNode node = (TreeNode) path.getLastPathComponent();

        if (node.getChildCount() >= 0) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                TreeNode n = (TreeNode) enumeration.nextElement();
                TreePath p = path.pathByAddingChild(n);
                expandAll(tree, p);
            }
        }
        tree.expandPath(path);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        panelUse = new javax.swing.JPanel();
        lblUso = new javax.swing.JLabel();
        panelValidity = new javax.swing.JPanel();
        lblValidSince = new javax.swing.JLabel();
        lblValidUntil = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        panelIssuer = new javax.swing.JPanel();
        lblIssuerO = new javax.swing.JLabel();
        lblIssuerC = new javax.swing.JLabel();
        lblIssuerCN = new javax.swing.JLabel();
        lblIssuerOU = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        panelSubject = new javax.swing.JPanel();
        lblSubjectCN = new javax.swing.JLabel();
        lblSubjectOU1 = new javax.swing.JLabel();
        lblSubjectOU2 = new javax.swing.JLabel();
        lblSubjectO = new javax.swing.JLabel();
        lblSubjectC = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        btnExportCertificate = new javax.swing.JButton();
        lblCertificateChain = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        btnClose.setText("Fechar");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTree1MousePressed(evt);
            }
        });
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        panelUse.setBorder(javax.swing.BorderFactory.createTitledBorder("Uso"));

        lblUso.setText(" ");

        javax.swing.GroupLayout panelUseLayout = new javax.swing.GroupLayout(panelUse);
        panelUse.setLayout(panelUseLayout);
        panelUseLayout.setHorizontalGroup(
            panelUseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblUso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelUseLayout.setVerticalGroup(
            panelUseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelUseLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblUso)
                .addContainerGap())
        );

        panelValidity.setBorder(javax.swing.BorderFactory.createTitledBorder("Validade"));

        lblValidSince.setText("Desde:");

        lblValidUntil.setText("Até:");

        javax.swing.GroupLayout panelValidityLayout = new javax.swing.GroupLayout(panelValidity);
        panelValidity.setLayout(panelValidityLayout);
        panelValidityLayout.setHorizontalGroup(
            panelValidityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelValidityLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelValidityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelValidityLayout.createSequentialGroup()
                        .addComponent(lblValidSince)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelValidityLayout.createSequentialGroup()
                        .addComponent(lblValidUntil)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelValidityLayout.setVerticalGroup(
            panelValidityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelValidityLayout.createSequentialGroup()
                .addGroup(panelValidityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValidSince)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelValidityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblValidUntil)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelIssuer.setBorder(javax.swing.BorderFactory.createTitledBorder("Emitido por"));

        lblIssuerO.setText("Organização (O):");

        lblIssuerC.setText("País (C):");

        lblIssuerCN.setText("Nome Comum (CN):");

        lblIssuerOU.setText("Unidade Organizacional (OU):");

        javax.swing.GroupLayout panelIssuerLayout = new javax.swing.GroupLayout(panelIssuer);
        panelIssuer.setLayout(panelIssuerLayout);
        panelIssuerLayout.setHorizontalGroup(
            panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIssuerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelIssuerLayout.createSequentialGroup()
                        .addComponent(lblIssuerC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelIssuerLayout.createSequentialGroup()
                        .addComponent(lblIssuerO)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelIssuerLayout.createSequentialGroup()
                        .addGroup(panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblIssuerOU)
                            .addComponent(lblIssuerCN))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                        .addGroup(panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        panelIssuerLayout.setVerticalGroup(
            panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIssuerLayout.createSequentialGroup()
                .addGroup(panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIssuerCN)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIssuerOU)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIssuerO)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIssuerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIssuerC)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        panelSubject.setBorder(javax.swing.BorderFactory.createTitledBorder("Emitido para"));

        lblSubjectCN.setText("Nome Comum (CN):");

        lblSubjectOU1.setText("Unidade Organizacional (OU):");

        lblSubjectOU2.setText("Unidade Organizacional (OU):");

        lblSubjectO.setText("Organização (O):");

        lblSubjectC.setText("País (C):");

        javax.swing.GroupLayout panelSubjectLayout = new javax.swing.GroupLayout(panelSubject);
        panelSubject.setLayout(panelSubjectLayout);
        panelSubjectLayout.setHorizontalGroup(
            panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSubjectLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSubjectLayout.createSequentialGroup()
                        .addComponent(lblSubjectCN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelSubjectLayout.createSequentialGroup()
                        .addComponent(lblSubjectC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelSubjectLayout.createSequentialGroup()
                        .addComponent(lblSubjectO)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelSubjectLayout.createSequentialGroup()
                        .addComponent(lblSubjectOU2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelSubjectLayout.createSequentialGroup()
                        .addComponent(lblSubjectOU1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelSubjectLayout.setVerticalGroup(
            panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSubjectLayout.createSequentialGroup()
                .addGroup(panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubjectCN)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubjectOU1)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubjectOU2)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubjectO)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSubjectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubjectC)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        btnExportCertificate.setText("Exportar Certificado");
        btnExportCertificate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportCertificateActionPerformed(evt);
            }
        });

        lblCertificateChain.setText("Cadeia de Certificados:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExportCertificate, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblCertificateChain)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(panelIssuer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelSubject, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelValidity, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelUse, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnClose))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblCertificateChain)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnClose)
                            .addComponent(btnExportCertificate)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelSubject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelIssuer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelValidity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelUse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 32, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private int lastSelectedRow;

    private void export(X509Certificate x509c) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(Bundle.getBundle().getString("title.saveAs"));
            FileNameExtensionFilter cerFilter = new FileNameExtensionFilter(Bundle.getBundle().getString("filter.certificateFiles") + " (*.cer)", "cer");
            fileChooser.setFileFilter(cerFilter);
            File preferedFile = new File(getCertificateCN(x509c) + ".cer");
            fileChooser.setSelectedFile(preferedFile);

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String dest = fileChooser.getSelectedFile().getAbsolutePath();
                File file = new File(dest);
                byte[] buf = x509c.getEncoded();

                FileOutputStream os = new FileOutputStream(file);
                os.write(buf);
                os.close();

                Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                wr.write(new sun.misc.BASE64Encoder().encode(buf));
                JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("certSuccessfullyExported"), "", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (CertificateEncodingException ex) {
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("certExportFailed") + "\n" + Bundle.getBundle().getString("certInvalidEncoding"), "", JOptionPane.ERROR_MESSAGE);
            //Logger.getLogger(CertificatePropertiesDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("certExportFailed") + "\n" + Bundle.getBundle().getString("noWritePermissions"), "", JOptionPane.ERROR_MESSAGE);
            //Logger.getLogger(CertificatePropertiesDialog.class.getName()).log(Level.SEVERE, null, ex);
            export(x509c);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("certExportFailed") + "\n" + Bundle.getBundle().getString("errorCreatingOutputFile"), "", JOptionPane.ERROR_MESSAGE);
            //Logger.getLogger(CertificatePropertiesDialog.class.getName()).log(Level.SEVERE, null, ex);
            export(x509c);
        }
    }

    private void btnExportCertificateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportCertificateActionPerformed
        X509Certificate x509c = certChainList.get(jTree1.getSelectionRows()[0]);
        export(x509c);
    }//GEN-LAST:event_btnExportCertificateActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        refreshSelectedCertificate();
    }//GEN-LAST:event_jTree1ValueChanged

    private void jTree1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MousePressed
        refreshSelectedCertificate();
    }//GEN-LAST:event_jTree1MousePressed

    private void refreshSelectedCertificate() {
        if (jTree1.getSelectionRows().length == 1) {
            if (null != jTree1.getSelectionPath()) {
                if (1 == jTree1.getSelectionRows().length) {
                    X509Certificate x509c = certChainList.get(jTree1.getSelectionRows()[0]);
                    setCertificateProperties(x509c);
                    lastSelectedRow = jTree1.getSelectionRows()[0];
                }
            }
        } else {
            jTree1.setSelectionRow(lastSelectedRow);
        }
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
            java.util.logging.Logger.getLogger(CertificatePropertiesDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                CertificatePropertiesDialog dialog = new CertificatePropertiesDialog(new javax.swing.JFrame(), true, (X509Certificate) null);
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
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExportCertificate;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JTree jTree1;
    private javax.swing.JLabel lblCertificateChain;
    private javax.swing.JLabel lblIssuerC;
    private javax.swing.JLabel lblIssuerCN;
    private javax.swing.JLabel lblIssuerO;
    private javax.swing.JLabel lblIssuerOU;
    private javax.swing.JLabel lblSubjectC;
    private javax.swing.JLabel lblSubjectCN;
    private javax.swing.JLabel lblSubjectO;
    private javax.swing.JLabel lblSubjectOU1;
    private javax.swing.JLabel lblSubjectOU2;
    private javax.swing.JLabel lblUso;
    private javax.swing.JLabel lblValidSince;
    private javax.swing.JLabel lblValidUntil;
    private javax.swing.JPanel panelIssuer;
    private javax.swing.JPanel panelSubject;
    private javax.swing.JPanel panelUse;
    private javax.swing.JPanel panelValidity;
    // End of variables declaration//GEN-END:variables
}
