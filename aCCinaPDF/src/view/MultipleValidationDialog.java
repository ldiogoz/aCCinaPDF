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
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import controller.Bundle;
import controller.CCInstance;
import exception.RevisionExtractionException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import listener.ValidationListener;
import model.CertificateStatus;
import model.SignatureValidation;
import model.TreeNodeWithState;
import model.ValidationFileListEntry;
import model.FileListTreeCellRenderer;
import model.ValidationTreeCellRenderer;
import org.apache.commons.lang3.text.WordUtils;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

/**
 *
 * @author Toshiba
 */
public class MultipleValidationDialog extends javax.swing.JDialog {

    private final HashMap<ValidationFileListEntry, ArrayList<SignatureValidation>> hmValidation = new HashMap<>();
    private JFrame window;

    /**
     * Creates new form MultipleValidationDialog
     *
     * @param parent
     * @param modal
     * @param files
     */
    public MultipleValidationDialog(java.awt.Frame parent, boolean modal, final ArrayList<File> files) {
        super(parent, modal);
        initComponents();

        updateText();

        this.setSize(1024, 768);
        lblRevision.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        final DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(null);
        panelSignatureDetails.setVisible(false);
        btnSaveInFile.setEnabled(false);
        jtValidation.setModel(null);
        jtValidation.setVisible(false);
        progressBar.setString(Bundle.getBundle().getString("pb.validating") + " 1 " + Bundle.getBundle().getString("of") + " " + files.size());
        progressBar.setMaximum(files.size());
        final ValidationListener vl = new ValidationListener() {
            int numParsed = 1;

            @Override
            public void onValidationComplete(SignatureValidation sv) {
                Map.Entry<ValidationFileListEntry, ArrayList<SignatureValidation>> tempEntry = null;
                while (tempEntry == null) {
                    for (Map.Entry<ValidationFileListEntry, ArrayList<SignatureValidation>> entry : hmValidation.entrySet()) {
                        if (entry.getKey().getFilename().equals(sv.getFilename())) {
                            tempEntry = entry;
                            break;
                        }
                    }
                    if (sv.isCertification()) {
                        if (sv.isValid()) {
                            tempEntry.getKey().setValidationStatus(ValidationFileListEntry.ValidationStatus.CERTIFIED);
                        } else {
                            tempEntry.getKey().setValidationStatus(ValidationFileListEntry.ValidationStatus.INVALID);
                        }
                    } else if (sv.isValid()) {
                        tempEntry.getKey().setValidationStatus(ValidationFileListEntry.ValidationStatus.ALL_OK);
                    } else {
                        tempEntry.getKey().setValidationStatus(ValidationFileListEntry.ValidationStatus.INVALID);
                    }
                    tempEntry.getValue().add(sv);
                }
            }
        };

        Runnable r = new Runnable() {
            @Override
            public void run() {
                int numParsed = 0;
                for (File file : files) {
                    try {
                        ArrayList<SignatureValidation> svList = new ArrayList<>();
                        int numSigs = CCInstance.getInstance().getNumberOfSignatures(file.getAbsolutePath());
                        ValidationFileListEntry vfle = new ValidationFileListEntry(file.getAbsolutePath(), numSigs, ValidationFileListEntry.ValidationStatus.UNKNOWN);
                        hmValidation.put(vfle, svList);
                        CCInstance.getInstance().validatePDF(file.getAbsolutePath(), vl);
                        dmtn.insert(new DefaultMutableTreeNode(vfle), 0);
                    } catch (IOException | DocumentException | GeneralSecurityException ex) {
                        if (ex.getLocalizedMessage().contains("keystore\\aCCinaPDF_cacerts")) {
                            JOptionPane.showMessageDialog(MultipleValidationDialog.this, Bundle.getBundle().getString("errorDefaultKeystore"), WordUtils.capitalize(Bundle.getBundle().getString("error")), JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(MultipleValidationDialog.this, Bundle.getBundle().getString("unknownErrorLog"), WordUtils.capitalize(Bundle.getBundle().getString("error")), JOptionPane.ERROR_MESSAGE);
                            controller.Logger.getLogger().addEntry(ex);
                        }
                        break;
                    }
                    numParsed++;
                    progressBar.setValue(numParsed);
                    progressBar.setString(Bundle.getBundle().getString("pb.validating") + " " + numParsed + " " + Bundle.getBundle().getString("of") + " " + files.size());

                    TreeModel tm = new DefaultTreeModel(dmtn);
                    jtFiles.setModel(tm);
                }
                progressBar.setString(Bundle.getBundle().getString("pb.validationComplete"));
                if (numParsed > 0) {
                    jtFiles.setSelectionRow(0);
                    btnSaveInFile.setEnabled(true);
                }
            }
        };

        Thread t = new Thread(r);
        t.start();

        jtValidation.setVisible(
                true);

        FileListTreeCellRenderer renderer1 = new FileListTreeCellRenderer();
        jtFiles.setCellRenderer(renderer1);
        ToolTipManager.sharedInstance().registerComponent(jtFiles);
        ValidationTreeCellRenderer renderer = new ValidationTreeCellRenderer();
        jtValidation.setCellRenderer(renderer);
    }

    private void updateText() {
        lbRevision.setText(WordUtils.capitalize(Bundle.getBundle().getString("revision")) + ":");
        lbDate.setText(Bundle.getBundle().getString("date") + ":");
        lbReason.setText(Bundle.getBundle().getString("reason") + ":");
        lbLocation.setText(Bundle.getBundle().getString("location") + ":");
        lbLtv.setText(Bundle.getBundle().getString("isLtv") + ":");
        lbAllowsChanges.setText(Bundle.getBundle().getString("allowsChanges") + ":");
        lbAdditionalInfo.setText(Bundle.getBundle().getString("extraInfo") + ":");
        lblAdditionalInfo.setText(Bundle.getBundle().getString("extraInfoNone"));
        btnShowCertificateDetails.setText(Bundle.getBundle().getString("label.certificateDetails"));
        btnSaveInFile.setText(Bundle.getBundle().getString("btn.saveInFile"));
        btnClose.setText(Bundle.getBundle().getString("btn.close"));
    }

    private void showSignatureValidationDetails(SignatureValidation sv) {
        if (null == sv) {
            panelSignatureDetails.setVisible(false);
        } else {
            lblRevision.setText("<html><u>" + sv.getRevision() + " " + Bundle.getBundle().getString("of") + " " + sv.getNumRevisions() + " (" + Bundle.getBundle().getString("label.clickToExtractRevision") + ")</u></html>");
            final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("Z");
            if (sv.getSignature().getTimeStampToken() == null) {
                Calendar cal = sv.getSignature().getSignDate();
                String date = df.format(cal.getTime().toLocaleString());
                lblDate.setText(date + " " + sdf.format(cal.getTime()) + " (" + Bundle.getBundle().getString("signerDateTimeSmall") + ")");
            } else {
                Calendar ts = sv.getSignature().getTimeStampDate();
                String date = df.format(ts.getTime());
                lblDate.setText(date + " " + sdf.format(ts.getTime()));
            }
            boolean ltv = (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK);
            lblLTV.setText(ltv ? Bundle.getBundle().getString("yes") : Bundle.getBundle().getString("no"));
            String reason = sv.getSignature().getReason();
            if (reason == null) {
                lblReason.setText(Bundle.getBundle().getString("notDefined"));
            } else if (reason.isEmpty()) {
                lblReason.setText(Bundle.getBundle().getString("notDefined"));
            } else {
                lblReason.setText(reason);
            }
            String location = sv.getSignature().getLocation();
            if (location == null) {
                lblLocation.setText(Bundle.getBundle().getString("notDefined"));
            } else if (location.isEmpty()) {
                lblLocation.setText(Bundle.getBundle().getString("notDefined"));
            } else {
                lblLocation.setText(location);
            }

            try {
                int certLevel = CCInstance.getInstance().getCertificationLevel(sv.getFilename());
                if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING) {
                    lblAllowsChanges.setText(Bundle.getBundle().getString("onlyAnnotations"));
                } else if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS) {
                    lblAllowsChanges.setText(Bundle.getBundle().getString("annotationsFormFilling"));
                } else if (certLevel == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
                    lblAllowsChanges.setText(Bundle.getBundle().getString("no"));
                } else {
                    lblAllowsChanges.setText(Bundle.getBundle().getString("yes"));
                }
            } catch (IOException ex) {
                controller.Logger.getLogger().addEntry(ex);
            }

            if (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK) {
                msg = (Bundle.getBundle().getString("validationCheck1") + " "
                        + (sv.getOcspCertificateStatus() == CertificateStatus.OK ? Bundle.getBundle().getString("validationCheck2") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getOcsp().getCerts()[0].getSubject(), "CN") + " " + Bundle.getBundle().getString("at") + " " + df.format(sv.getSignature().getOcsp().getProducedAt()) : (sv.getCrlCertificateStatus() == CertificateStatus.OK ? "CRL" : ""))
                        + (sv.getSignature().getTimeStampToken() != null ? Bundle.getBundle().getString("validationCheck3") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O") : ""));
                lblAdditionalInfo.setText("<html><u>" + Bundle.getBundle().getString("label.clickToView") + "</u></html>");
                lblAdditionalInfo.setForeground(new java.awt.Color(0, 0, 255));
                lblAdditionalInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else if (sv.getSignature().getTimeStampToken() != null) {
                msg = (Bundle.getBundle().getString("validationCheck3") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O"));
                lblAdditionalInfo.setText("<html><u>" + Bundle.getBundle().getString("label.clickToView") + "</u></html>");
                lblAdditionalInfo.setForeground(new java.awt.Color(0, 0, 255));
                lblAdditionalInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                msg = null;
            }

            panelSignatureDetails.setVisible(true);
        }
    }

    private String msg = null;

    private void openPdfReaderFromFile(File file) {
        this.hide();
        if (testPdf(file)) {
            String filePath = file.getAbsolutePath();
            SwingController sc = new SwingController();
            SwingViewBuilder factory = new SwingViewBuilder(sc);
            JPanel viewerComponentPanel = factory.buildViewerPanel();
            ComponentKeyBinding.install(sc, viewerComponentPanel);
            sc.getDocumentViewController().setAnnotationCallback(new org.icepdf.ri.common.MyAnnotationCallback(sc.getDocumentViewController()));
            window = new JFrame(filePath);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    window.dispose();
                    MultipleValidationDialog.this.show();
                }
            });
            window.getContentPane().add(viewerComponentPanel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            sc.openDocument(filePath);
            file.deleteOnExit();
        } else {
            JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("msg.fileCorruptedNotOpened"), WordUtils.capitalize(Bundle.getBundle().getString("error")), JOptionPane.ERROR_MESSAGE);
            controller.Logger.getLogger().addEntry(Bundle.getBundle().getString("msg.fileCorrupted"));
        }
    }

    private boolean testPdf(File toTest) {
        try {
            if (null == toTest) {
                return false;
            }
            PdfReader pdfReader = new PdfReader(toTest.getAbsolutePath());
            pdfReader.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jsp1 = new javax.swing.JScrollPane();
        jtFiles = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jsp2 = new javax.swing.JScrollPane();
        jtValidation = new javax.swing.JTree();
        panelSignatureDetails = new javax.swing.JPanel();
        btnShowCertificateDetails = new javax.swing.JButton();
        lbRevision = new javax.swing.JLabel();
        lbLtv = new javax.swing.JLabel();
        lbDate = new javax.swing.JLabel();
        lblRevision = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblLTV = new javax.swing.JLabel();
        lbReason = new javax.swing.JLabel();
        lbLocation = new javax.swing.JLabel();
        lblReason = new javax.swing.JLabel();
        lblLocation = new javax.swing.JLabel();
        lbAllowsChanges = new javax.swing.JLabel();
        lblAllowsChanges = new javax.swing.JLabel();
        lbAdditionalInfo = new javax.swing.JLabel();
        lblAdditionalInfo = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        btnSaveInFile = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jSplitPane1.setDividerLocation(400);

        jtFiles.setModel(null);
        jtFiles.setRootVisible(false);
        jtFiles.setRowHeight(22);
        jtFiles.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jtFilesComponentResized(evt);
            }
        });
        jtFiles.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jtFilesValueChanged(evt);
            }
        });
        jsp1.setViewportView(jtFiles);

        jSplitPane1.setLeftComponent(jsp1);

        jtValidation.setRootVisible(false);
        jtValidation.setRowHeight(32);
        jtValidation.setShowsRootHandles(true);
        jtValidation.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jtValidationValueChanged(evt);
            }
        });
        jsp2.setViewportView(jtValidation);

        btnShowCertificateDetails.setText("Mostrar Certificado");
        btnShowCertificateDetails.setToolTipText("Abre uma janela com os detalhes dos certificado contidos nesta assinatura");
        btnShowCertificateDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowCertificateDetailsActionPerformed(evt);
            }
        });

        lbRevision.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbRevision.setText("Revisão:");

        lbLtv.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbLtv.setText("Habilitada para validação a longo termo:");
        lbLtv.setToolTipText("Referente a se a assinatura contém toda a informação necessária para validar todos os certificados nela contidos");

        lbDate.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbDate.setText("Data:");

        lblRevision.setForeground(new java.awt.Color(0, 0, 255));
        lblRevision.setText(" ");
        lblRevision.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRevisionMouseClicked(evt);
            }
        });

        lblDate.setText(" ");

        lblLTV.setText(" ");
        lblLTV.setToolTipText("Referente a se a assinatura contém toda a informação necessária para validar todos os certificados nela contidos");

        lbReason.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbReason.setText("Razão:");

        lbLocation.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbLocation.setText("Local:");

        lblReason.setText(" ");

        lblLocation.setText(" ");

        lbAllowsChanges.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbAllowsChanges.setText("Permite alterações:");

        lblAllowsChanges.setText(" ");

        lbAdditionalInfo.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lbAdditionalInfo.setText("Informação adicional:");

        lblAdditionalInfo.setText("Nenhuma");
        lblAdditionalInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAdditionalInfoMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelSignatureDetailsLayout = new javax.swing.GroupLayout(panelSignatureDetails);
        panelSignatureDetails.setLayout(panelSignatureDetailsLayout);
        panelSignatureDetailsLayout.setHorizontalGroup(
            panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnShowCertificateDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(lbRevision)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblRevision))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(lbDate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDate))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(lbReason)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblReason))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(lbLocation)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblLocation))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(lbAdditionalInfo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAdditionalInfo))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSignatureDetailsLayout.createSequentialGroup()
                                        .addComponent(lbAllowsChanges)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblAllowsChanges, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(lbLtv))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblLTV)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelSignatureDetailsLayout.setVerticalGroup(
            panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                .addComponent(btnShowCertificateDetails)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbRevision)
                    .addComponent(lblRevision))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbDate)
                    .addComponent(lblDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbReason)
                    .addComponent(lblReason))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLocation)
                    .addComponent(lblLocation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLtv)
                    .addComponent(lblLTV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbAllowsChanges)
                    .addComponent(lblAllowsChanges))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbAdditionalInfo)
                    .addComponent(lblAdditionalInfo))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jsp2, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
            .addComponent(panelSignatureDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jsp2, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelSignatureDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        btnClose.setText("Fechar");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        progressBar.setStringPainted(true);

        btnSaveInFile.setText("Guardar em ficheiro");
        btnSaveInFile.setToolTipText("Guarda o resultado da validação de todos os documentos num ficheiro de texto");
        btnSaveInFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveInFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSaveInFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(btnSaveInFile)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void jtValidationValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jtValidationValueChanged
        if (jtValidation.getSelectionRows().length == 0) {
            showSignatureValidationDetails(null);
        } else if (jtValidation.getSelectionRows().length == 1) {
            DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jtValidation.getLastSelectedPathComponent();
            SignatureValidation sv = null;
            if (dtn.isLeaf()) {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) dtn.getParent();
                sv = (SignatureValidation) dmtn.getUserObject();
            } else {
                sv = (SignatureValidation) dtn.getUserObject();
                jtValidation.expandRow(jtValidation.getSelectionRows()[0]);
            }
            showSignatureValidationDetails(sv);
        } else {
            jtValidation.setSelectionPath(evt.getOldLeadSelectionPath());
        }
    }//GEN-LAST:event_jtValidationValueChanged

    private void jtFilesValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jtFilesValueChanged
        jtValidation.setModel(null);
        ArrayList<SignatureValidation> svList = null;

        if (jtFiles.getSelectionRows().length > 1) {
            jtFiles.setSelectionPath(evt.getOldLeadSelectionPath());
            return;
        }

        if (evt.getNewLeadSelectionPath() == null) {
            panelSignatureDetails.setVisible(false);
            return;
        }

        for (Map.Entry<ValidationFileListEntry, ArrayList<SignatureValidation>> entry : hmValidation.entrySet()) {
            int numSigs = CCInstance.getInstance().getNumberOfSignatures(entry.getKey().getFilename());
            if (String.valueOf(evt.getPath().getLastPathComponent()).equals("(" + numSigs + ") " + entry.getKey().getFilename())) {
                svList = entry.getValue();
                break;
            }
        }

        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(null);

        if (svList == null) {
            return;
        }
        if (svList.isEmpty()) {
            panelSignatureDetails.setVisible(false);
            DefaultMutableTreeNode noSignatures = new TreeNodeWithState(Bundle.getBundle().getString("notSigned"), TreeNodeWithState.State.NOT_SIGNED);
            top.add(noSignatures);
            TreeModel tm = new DefaultTreeModel(top);
            jtValidation.setModel(tm);
            return;
        }

        for (SignatureValidation sv : svList) {
            DefaultMutableTreeNode sig = new DefaultMutableTreeNode(sv);

            TreeNodeWithState childChanged = null;
            if (sv.isCertification()) {
                if (sv.isValid()) {
                    if (sv.isChanged() || !sv.isCoversEntireDocument()) {
                        childChanged = new TreeNodeWithState("<html>" + Bundle.getBundle().getString("tn.1") + "<br>" + Bundle.getBundle().getString("tn.2") + "</html>", TreeNodeWithState.State.CERTIFIED_WARNING);
                    } else {
                        childChanged = new TreeNodeWithState(Bundle.getBundle().getString("certifiedOk"), TreeNodeWithState.State.CERTIFIED);
                    }
                } else {
                    childChanged = new TreeNodeWithState(Bundle.getBundle().getString("changedAfterCertified"), TreeNodeWithState.State.INVALID);
                }
            } else if (sv.isValid()) {
                if (sv.isChanged()) {
                    childChanged = new TreeNodeWithState("<html>" + Bundle.getBundle().getString("tn.3") + "<br>" + Bundle.getBundle().getString("tn.4") + "</html>", TreeNodeWithState.State.VALID_WARNING);
                } else {
                    childChanged = new TreeNodeWithState(Bundle.getBundle().getString("signedOk"), TreeNodeWithState.State.VALID);
                }
            } else {
                childChanged = new TreeNodeWithState(Bundle.getBundle().getString("signedChangedOrCorrupted"), TreeNodeWithState.State.INVALID);
            }

            TreeNodeWithState childVerified = null;
            if (sv.getOcspCertificateStatus().equals(CertificateStatus.OK) || sv.getCrlCertificateStatus().equals(CertificateStatus.OK)) {
                childVerified = new TreeNodeWithState(Bundle.getBundle().getString("certOK"), TreeNodeWithState.State.VALID);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.REVOKED) || sv.getCrlCertificateStatus().equals(CertificateStatus.REVOKED)) {
                childVerified = new TreeNodeWithState(Bundle.getBundle().getString("certRevoked"), TreeNodeWithState.State.INVALID);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && sv.getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED)) {
                childVerified = new TreeNodeWithState(Bundle.getBundle().getString("certNotVerified"), TreeNodeWithState.State.WARNING);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHAINED)) {
                childVerified = new TreeNodeWithState(Bundle.getBundle().getString("certNotChained"), TreeNodeWithState.State.WARNING);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.EXPIRED)) {
                childVerified = new TreeNodeWithState(Bundle.getBundle().getString("certExpired"), TreeNodeWithState.State.WARNING);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.CHAINED_LOCALLY)) {
                childVerified = new TreeNodeWithState("<html>" + Bundle.getBundle().getString("tn.5") + "<br>" + Bundle.getBundle().getString("tn.6") + "</html>", TreeNodeWithState.State.VALID_WARNING);
            }

            TreeNodeWithState childTimestamp = null;
            if (sv.isValidTimeStamp()) {
                childTimestamp = new TreeNodeWithState(Bundle.getBundle().getString("validTimestamp"), TreeNodeWithState.State.VALID);
            } else {
                childTimestamp = new TreeNodeWithState(Bundle.getBundle().getString("signerDateTime"), TreeNodeWithState.State.WARNING);
            }

            sig.add(childChanged);
            sig.add(childVerified);
            sig.add(childTimestamp);
            top.add(sig);
        }
        TreeModel tm = new DefaultTreeModel(top);
        jtValidation.setModel(tm);

        if (jtValidation.getRowCount() > 0) {
            jtValidation.setSelectionRow(jtValidation.getRowCount() - 1);
        }
    }//GEN-LAST:event_jtFilesValueChanged

    private void btnShowCertificateDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowCertificateDetailsActionPerformed
        DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jtValidation.getLastSelectedPathComponent();
        SignatureValidation sv = null;
        if (dtn.isLeaf()) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) dtn.getParent();
            sv = (SignatureValidation) dmtn.getUserObject();
        } else {
            sv = (SignatureValidation) dtn.getUserObject();
        }
        CertificatePropertiesDialog cpd = new CertificatePropertiesDialog((MainWindow) this.getParent(), true, sv.getSignature().getSignCertificateChain());
        cpd.setLocationRelativeTo(null);
        cpd.setVisible(true);
    }//GEN-LAST:event_btnShowCertificateDetailsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (window != null) {
            if (window.isVisible()) {
                window.dispose();
            }
        }
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void jtFilesComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jtFilesComponentResized
        jtFiles.updateUI();
    }//GEN-LAST:event_jtFilesComponentResized

    private void lblRevisionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRevisionMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jtValidation.getLastSelectedPathComponent();
            SignatureValidation sv = null;
            if (dtn.isLeaf()) {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) dtn.getParent();
                sv = (SignatureValidation) dmtn.getUserObject();
            } else {
                sv = (SignatureValidation) dtn.getUserObject();
            }
            try {
                File f = CCInstance.getInstance().extractRevision(sv.getFilename(), sv.getName());
                openPdfReaderFromFile(f);
            } catch (IOException | RevisionExtractionException ex) {
                Logger.getLogger(WorkspacePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_lblRevisionMouseClicked

    private void btnSaveInFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveInFileActionPerformed
        String toWrite = "";
        String newLine = System.getProperty("line.separator");
        if (hmValidation.size() > 0) {
            for (Map.Entry<ValidationFileListEntry, ArrayList<SignatureValidation>> entry : hmValidation.entrySet()) {
                toWrite += WordUtils.capitalize(Bundle.getBundle().getString("file")) + ": " + entry.getKey().getFilename() + " (";
                int numSigs = entry.getKey().getNumSignatures();
                if (numSigs == 0) {
                    toWrite += Bundle.getBundle().getString("noSignatures");
                } else if (numSigs == 1) {
                    toWrite += "1 " + Bundle.getBundle().getString("signature");
                } else {
                    toWrite += numSigs + " " + Bundle.getBundle().getString("signatures");
                }
                toWrite += ")" + newLine;

                for (SignatureValidation sv : entry.getValue()) {
                    toWrite += "\t" + sv.getName() + " - ";
                    toWrite += (sv.isCertification() ? WordUtils.capitalize(Bundle.getBundle().getString("certificate")) : WordUtils.capitalize(Bundle.getBundle().getString("signed"))) + " " + Bundle.getBundle().getString("by") + " " + sv.getSignerName();
                    toWrite += newLine + "\t\t";
                    if (sv.isChanged()) {
                        toWrite += Bundle.getBundle().getString("certifiedChangedOrCorrupted");
                    } else if (sv.isCertification()) {
                        if (sv.isValid()) {
                            if (sv.isChanged() || !sv.isCoversEntireDocument()) {
                                toWrite += Bundle.getBundle().getString("certifiedButChanged");
                            } else {
                                toWrite += Bundle.getBundle().getString("certifiedOk");
                            }
                        } else {
                            toWrite += Bundle.getBundle().getString("changedAfterCertified");
                        }
                    } else if (sv.isValid()) {
                        if (sv.isChanged()) {
                            toWrite += Bundle.getBundle().getString("signedButChanged");
                        } else {
                            toWrite += Bundle.getBundle().getString("signedOk");
                        }
                    } else {
                        toWrite += Bundle.getBundle().getString("signedChangedOrCorrupted");
                    }
                    toWrite += newLine + "\t\t";
                    if (sv.getOcspCertificateStatus().equals(CertificateStatus.OK) || sv.getCrlCertificateStatus().equals(CertificateStatus.OK)) {
                        toWrite += Bundle.getBundle().getString("certOK");
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.REVOKED) || sv.getCrlCertificateStatus().equals(CertificateStatus.REVOKED)) {
                        toWrite += Bundle.getBundle().getString("certRevoked");
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && sv.getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED)) {
                        toWrite += Bundle.getBundle().getString("certNotVerified");
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHAINED)) {
                        toWrite += Bundle.getBundle().getString("certNotChained");
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.EXPIRED)) {
                        toWrite += Bundle.getBundle().getString("certExpired");
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.CHAINED_LOCALLY)) {
                        toWrite += Bundle.getBundle().getString("certChainedLocally");
                    }
                    toWrite += newLine + "\t\t";
                    if (sv.isValidTimeStamp()) {
                        toWrite += Bundle.getBundle().getString("validTimestamp");
                    } else {
                        toWrite += Bundle.getBundle().getString("signerDateTime");
                    }
                    toWrite += newLine + "\t\t";

                    toWrite += WordUtils.capitalize(Bundle.getBundle().getString("revision")) + ": " + sv.getRevision() + " " + Bundle.getBundle().getString("of") + " " + sv.getNumRevisions();
                    toWrite += newLine + "\t\t";
                    final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    final SimpleDateFormat sdf = new SimpleDateFormat("Z");
                    if (sv.getSignature().getTimeStampToken() == null) {
                        Calendar cal = sv.getSignature().getSignDate();
                        String date = sdf.format(cal.getTime().toLocaleString());
                        toWrite += date + " " + sdf.format(cal.getTime()) + " (" + Bundle.getBundle().getString("signerDateTimeSmall") + ")";
                    } else {
                        Calendar ts = sv.getSignature().getTimeStampDate();
                        String date = df.format(ts.getTime());
                        toWrite += Bundle.getBundle().getString("date") + " " + date + " " + sdf.format(ts.getTime());
                    }
                    toWrite += newLine + "\t\t";
                    boolean ltv = (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK);
                    toWrite += Bundle.getBundle().getString("isLtv") + ": " + (ltv ? Bundle.getBundle().getString("yes") : Bundle.getBundle().getString("no"));
                    String reason = sv.getSignature().getReason();
                    toWrite += newLine + "\t\t";
                    toWrite += Bundle.getBundle().getString("reason") + ": ";
                    if (reason == null) {
                        toWrite += Bundle.getBundle().getString("notDefined");;
                    } else if (reason.isEmpty()) {
                        toWrite += Bundle.getBundle().getString("notDefined");
                    } else {
                        toWrite += reason;
                    }
                    String location = sv.getSignature().getLocation();
                    toWrite += newLine + "\t\t";
                    toWrite += Bundle.getBundle().getString("location") + ": ";
                    if (location == null) {
                        toWrite += Bundle.getBundle().getString("notDefined");
                    } else if (location.isEmpty()) {
                        toWrite += Bundle.getBundle().getString("notDefined");
                    } else {
                        toWrite += location;
                    }
                    toWrite += newLine + "\t\t";
                    toWrite += Bundle.getBundle().getString("allowsChanges") + ": ";
                    try {
                        int certLevel = CCInstance.getInstance().getCertificationLevel(sv.getFilename());
                        if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING) {
                            toWrite += Bundle.getBundle().getString("onlyAnnotations");
                        } else if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS) {
                            toWrite += Bundle.getBundle().getString("annotationsFormFilling");
                        } else if (certLevel == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
                            toWrite += Bundle.getBundle().getString("no");
                        } else {
                            toWrite += Bundle.getBundle().getString("yes");
                        }
                    } catch (IOException ex) {
                        controller.Logger.getLogger().addEntry(ex);
                    }
                    toWrite += newLine + "\t\t";
                    if (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK) {
                        toWrite += (Bundle.getBundle().getString("validationCheck1") + " "
                                + (sv.getOcspCertificateStatus() == CertificateStatus.OK ? Bundle.getBundle().getString("validationCheck2") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getOcsp().getCerts()[0].getSubject(), "CN") + " " + Bundle.getBundle().getString("at") + " " + df.format(sv.getSignature().getOcsp().getProducedAt()) : (sv.getCrlCertificateStatus() == CertificateStatus.OK ? "CRL" : ""))
                                + (sv.getSignature().getTimeStampToken() != null ? Bundle.getBundle().getString("validationCheck3") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O") : ""));
                    } else if (sv.getSignature().getTimeStampToken() != null) {
                        toWrite += (Bundle.getBundle().getString("validationCheck3") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O"));
                    }
                    toWrite += newLine;
                }
            }
            writeToFile(toWrite);
        }
    }//GEN-LAST:event_btnSaveInFileActionPerformed

    private void lblAdditionalInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAdditionalInfoMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg);
            }
        }
    }//GEN-LAST:event_lblAdditionalInfoMouseClicked

    private void writeToFile(String str) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(Bundle.getBundle().getString("title.saveAs"));
        boolean validPath = false;
        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(Bundle.getBundle().getString("filter.textFiles") + " (*.txt)", "txt");
        fileChooser.setFileFilter(pdfFilter);
        File preferedFile = new File(Bundle.getBundle().getString("validationReport") + ".txt");
        fileChooser.setSelectedFile(preferedFile);

        while (!validPath) {
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.CANCEL_OPTION) {
                return;
            }
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String dest = fileChooser.getSelectedFile().getAbsolutePath();
                if (new File(dest).exists()) {
                    String msg = Bundle.getBundle().getString("msg.reportFileNameAlreadyExists");
                    Object[] options = {Bundle.getBundle().getString("btn.overwrite"), Bundle.getBundle().getString("btn.chooseNewPath"), Bundle.getBundle().getString("btn.cancel")};
                    int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (opt == JOptionPane.YES_OPTION) {
                        validPath = true;
                    } else if (opt == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                } else {
                    validPath = true;
                }

                if (validPath) {
                    try (PrintStream out = new PrintStream(new FileOutputStream(dest))) {
                        out.print(str);
                        JOptionPane.showMessageDialog(null, Bundle.getBundle().getString("msg.reportSavedSuccessfully"), "", JOptionPane.INFORMATION_MESSAGE);
                    } catch (FileNotFoundException ex) {
                        controller.Logger.getLogger().addEntry(ex);
                        JOptionPane.showMessageDialog(null, Bundle.getBundle().getString("msg.reportSaveFailed"), "", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                }
            }
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
            java.util.logging.Logger.getLogger(MultipleValidationDialog.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MultipleValidationDialog dialog = new MultipleValidationDialog(new javax.swing.JFrame(), true, null);
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
    private javax.swing.JButton btnSaveInFile;
    private javax.swing.JButton btnShowCertificateDetails;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane jsp1;
    private javax.swing.JScrollPane jsp2;
    private javax.swing.JTree jtFiles;
    private javax.swing.JTree jtValidation;
    private javax.swing.JLabel lbAdditionalInfo;
    private javax.swing.JLabel lbAllowsChanges;
    private javax.swing.JLabel lbDate;
    private javax.swing.JLabel lbLocation;
    private javax.swing.JLabel lbLtv;
    private javax.swing.JLabel lbReason;
    private javax.swing.JLabel lbRevision;
    private javax.swing.JLabel lblAdditionalInfo;
    private javax.swing.JLabel lblAllowsChanges;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblLTV;
    private javax.swing.JLabel lblLocation;
    private javax.swing.JLabel lblReason;
    private javax.swing.JLabel lblRevision;
    private javax.swing.JPanel panelSignatureDetails;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
}
