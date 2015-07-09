/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import controller.CCInstance;
import exception.RevisionExtractionException;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
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

        this.setSize(1024, 768);
        lblRevision.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        final DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(null);
        panelSignatureDetails.setVisible(false);
        btnGuardar.setEnabled(false);
        jtValidation.setModel(null);
        jtValidation.setVisible(false);
        progressBar.setString("A Validar Ficheiro 1 de " + files.size());
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
                    } else {
                        if (sv.isValid()) {
                            tempEntry.getKey().setValidationStatus(ValidationFileListEntry.ValidationStatus.ALL_OK);
                        } else {
                            tempEntry.getKey().setValidationStatus(ValidationFileListEntry.ValidationStatus.INVALID);
                        }
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
                            JOptionPane.showMessageDialog(MultipleValidationDialog.this, "Não foi possível encontrar o ficheiro keystore/aCCinaPDF_cacerts ", "Erro", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(MultipleValidationDialog.this, "Erro desconhecido - ver log", "Erro", JOptionPane.ERROR_MESSAGE);
                            controller.Logger.getLogger().addEntry(ex);
                        }
                        break;
                    }
                    numParsed++;
                    progressBar.setValue(numParsed);
                    progressBar.setString("A Validar Ficheiro " + numParsed + " de " + files.size());

                    TreeModel tm = new DefaultTreeModel(dmtn);
                    jtFiles.setModel(tm);
                }
                progressBar.setString("Validação Concluída");
                if (numParsed > 0) {
                    jtFiles.setSelectionRow(0);
                    btnGuardar.setEnabled(true);
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

    private void showSignatureValidationDetails(SignatureValidation sv) {
        if (null == sv) {
            panelSignatureDetails.setVisible(false);
        } else {
            lblRevision.setText("<html><u>" + sv.getRevision() + " de " + sv.getNumRevisions() + " (Clique para extrair a revisão)</u></html>");
            final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            if (sv.getSignature().getTimeStampToken() == null) {
                Calendar cal = sv.getSignature().getSignDate();
                String date = df.format(cal.getTime());
                lblDate.setText(date + " (hora do computador do signatário)");
            } else {
                Calendar ts = sv.getSignature().getTimeStampDate();
                String date = df.format(ts.getTime());
                lblDate.setText(date + " +" + (ts.getTimeZone().getRawOffset() < 10 ? "0" : "") + ts.getTimeZone().getRawOffset());
            }
            boolean ltv = (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK);
            lblLTV.setText(ltv ? "Sim" : "Não");
            String reason = sv.getSignature().getReason();
            if (reason == null) {
                lblReason.setText("Não definida");
            } else if (reason.equals("")) {
                lblReason.setText("Não definida");
            } else {
                lblReason.setText(reason);
            }
            String location = sv.getSignature().getLocation();
            if (location == null) {
                lblLocation.setText("Não definido");
            } else if (location.equals("")) {
                lblLocation.setText("Não definido");
            } else {
                lblLocation.setText(location);
            }

            try {
                int certLevel = CCInstance.getInstance().getCertificationLevel(sv.getFilename());
                if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING) {
                    lblAllowsChanges.setText("Apenas anotações");
                } else if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS) {
                    lblAllowsChanges.setText("Preenchimento de formulário e anotações");
                } else if (certLevel == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
                    lblAllowsChanges.setText("Não");
                } else {
                    lblAllowsChanges.setText("Sim");
                }
            } catch (IOException ex) {
                controller.Logger.getLogger().addEntry(ex);
            }

            if (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK) {
                msg = ("O estado de revogação do certificado inerente a esta assinatura foi verificado com recurso a "
                        + (sv.getOcspCertificateStatus() == CertificateStatus.OK ? "OCSP pela entidade: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getOcsp().getCerts()[0].getSubject(), "CN") + " em " + df.format(sv.getSignature().getOcsp().getProducedAt()) : (sv.getCrlCertificateStatus() == CertificateStatus.OK ? "CRL" : ""))
                        + (sv.getSignature().getTimeStampToken() != null ? "\nO carimbo de data e hora é válido e foi assinado por: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O") : ""));
                lblAdditionalInfo.setText("<html><u>Clique aqui para ver</u></html>");
                lblAdditionalInfo.setForeground(new java.awt.Color(0, 0, 255));
                lblAdditionalInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else if (sv.getSignature().getTimeStampToken() != null) {
                msg = ("O carimbo de data e hora é válido e foi assinado por: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O"));
                lblAdditionalInfo.setText("<html><u>Clique aqui para ver</u></html>");
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
            JOptionPane.showMessageDialog(this, "O Ficheiro extraído está corrompido e não pôde ser aberto", "Erro", JOptionPane.ERROR_MESSAGE);
            controller.Logger.getLogger().addEntry("O Ficheiro está corrompido");
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
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblRevision = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblLTV = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblReason = new javax.swing.JLabel();
        lblLocation = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblAllowsChanges = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblAdditionalInfo = new javax.swing.JLabel();
        btnFechar = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        btnGuardar = new javax.swing.JButton();

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

        jButton1.setText("Mostrar Certificado");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel1.setText("Revisão:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel2.setText("Habilitada para validação a longo termo:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel4.setText("Data:");

        lblRevision.setForeground(new java.awt.Color(0, 0, 255));
        lblRevision.setText(" ");
        lblRevision.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRevisionMouseClicked(evt);
            }
        });

        lblDate.setText(" ");

        lblLTV.setText(" ");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel3.setText("Razão:");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel5.setText("Local:");

        lblReason.setText(" ");

        lblLocation.setText(" ");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel6.setText("Permite alterações:");

        lblAllowsChanges.setText(" ");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel7.setText("Informação adicional:");

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
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblRevision))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDate))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblReason))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblLocation))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAdditionalInfo))
                            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSignatureDetailsLayout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblAllowsChanges, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblLTV)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelSignatureDetailsLayout.setVerticalGroup(
            panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblRevision))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblReason))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblLocation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblLTV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblAllowsChanges))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
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

        btnFechar.setText("Fechar");
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });

        progressBar.setStringPainted(true);

        btnGuardar.setText("Guardar em ficheiro");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
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
                        .addComponent(btnGuardar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFechar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFechar)
                    .addComponent(btnGuardar)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnFecharActionPerformed

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
            DefaultMutableTreeNode noSignatures = new TreeNodeWithState("O Documento não está assinado", TreeNodeWithState.State.NOT_SIGNED);
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
                        childChanged = new TreeNodeWithState("<html>A revisão do documento que é coberto pela certificação não foi alterada<br>No entanto, ocorreram alterações posteriores ao documento</html>", TreeNodeWithState.State.CERTIFIED_WARNING);
                    } else {
                        childChanged = new TreeNodeWithState("O Documento está certificado e não foi modificado", TreeNodeWithState.State.CERTIFIED);
                    }
                } else {
                    childChanged = new TreeNodeWithState("O Documento foi alterado ou corrompido desde que foi aplicada esta certificação", TreeNodeWithState.State.INVALID);
                }
            } else {
                if (sv.isValid()) {
                    if (sv.isChanged()) {
                        childChanged = new TreeNodeWithState("<html>A revisão do documento que é coberto pela assinatura não foi alterada<br>No entanto, ocorreram alterações posteriores ao documento</html>", TreeNodeWithState.State.VALID_WARNING);
                    } else {
                        childChanged = new TreeNodeWithState("O Documento está assinado e não foi modificado", TreeNodeWithState.State.VALID);
                    }
                } else {
                    childChanged = new TreeNodeWithState("O Documento foi alterado ou corrompido desde que foi aplicada esta assinatura", TreeNodeWithState.State.INVALID);
                }
            }

            TreeNodeWithState childVerified = null;
            if (sv.getOcspCertificateStatus().equals(CertificateStatus.OK) || sv.getCrlCertificateStatus().equals(CertificateStatus.OK)) {
                childVerified = new TreeNodeWithState("O Certificado inerente a esta assinatura foi verificado e é válido", TreeNodeWithState.State.VALID);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.REVOKED) || sv.getCrlCertificateStatus().equals(CertificateStatus.REVOKED)) {
                childVerified = new TreeNodeWithState("O Certificado inerente a esta assinatura foi revogado", TreeNodeWithState.State.INVALID);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && sv.getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED)) {
                childVerified = new TreeNodeWithState("Não foi feita a verificação da revogação de certificados durante a assinatura", TreeNodeWithState.State.WARNING);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHAINED)) {
                childVerified = new TreeNodeWithState("O Certificado não está encadeado a um certificado designado como âncora confiável", TreeNodeWithState.State.WARNING);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.EXPIRED)) {
                childVerified = new TreeNodeWithState("O Certificado expirou", TreeNodeWithState.State.WARNING);
            } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.CHAINED_LOCALLY)) {
                childVerified = new TreeNodeWithState("<html>A assinatura não contém a âncora completa nem verificações de revogação.<br>No entanto, o certificado do assinante foi emitido por um certificado na âncora confiável</html>", TreeNodeWithState.State.VALID_WARNING);
            }

            TreeNodeWithState childTimestamp = null;
            if (sv.isValidTimeStamp()) {
                childTimestamp = new TreeNodeWithState("A Assinatura inclui um carimbo de Data e Hora válido", TreeNodeWithState.State.VALID);
            } else {
                childTimestamp = new TreeNodeWithState("A Data e Hora da assinatura são do relógio do computador do signatário", TreeNodeWithState.State.WARNING);
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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jtValidation.getLastSelectedPathComponent();
        SignatureValidation sv = null;
        if (dtn.isLeaf()) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) dtn.getParent();
            sv = (SignatureValidation) dmtn.getUserObject();
        } else {
            sv = (SignatureValidation) dtn.getUserObject();
        }
        X509Certificate x509c = sv.getSignature().getSigningCertificate();
        CertificatePropertiesDialog cpd = new CertificatePropertiesDialog((MainWindow) this.getParent(), true, x509c);
        cpd.setLocationRelativeTo(null);
        cpd.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

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

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        String toWrite = "";
        String newLine = System.getProperty("line.separator");
        if (hmValidation.size() > 0) {
            for (Map.Entry<ValidationFileListEntry, ArrayList<SignatureValidation>> entry : hmValidation.entrySet()) {
                toWrite += "Ficheiro: " + entry.getKey().getFilename() + " (";
                int numSigs = entry.getKey().getNumSignatures();
                if (numSigs == 0) {
                    toWrite += "Sem assinaturas";
                } else if (numSigs == 1) {
                    toWrite += "1 assinatura";
                } else {
                    toWrite += numSigs + " assinaturas";
                }
                toWrite += ")" + newLine;

                for (SignatureValidation sv : entry.getValue()) {
                    toWrite += "\t" + sv.getName() + " - ";
                    toWrite += (sv.isCertification() ? "Certificado" : "Assinado") + " por " + sv.getSignerName();
                    toWrite += newLine + "\t\t";
                    if (sv.isChanged()) {
                        toWrite += "O Documento foi alterado ou corrompido desde que foi certificado";
                    } else {
                        if (sv.isCertification()) {
                            if (sv.isValid()) {
                                if (sv.isChanged() || !sv.isCoversEntireDocument()) {
                                    toWrite += "A revisão do documento que é coberto pela certificação não foi alterada. No entanto, ocorreram alterações posteriores ao documento";
                                } else {
                                    toWrite += "O Documento está certificado e não foi modificado";
                                }
                            } else {
                                toWrite += "O Documento foi alterado ou corrompido desde que foi aplicada esta certificação";
                            }
                        } else {
                            if (sv.isValid()) {
                                if (sv.isChanged()) {
                                    toWrite += "A revisão do documento que é coberto pela assinatura não foi alterada. No entanto, ocorreram alterações posteriores ao documento";
                                } else {
                                    toWrite += "O Documento está assinado e não foi modificado";
                                }
                            } else {
                                toWrite += "O Documento foi alterado ou corrompido desde que foi aplicada esta assinatura";
                            }
                        }
                    }
                    toWrite += newLine + "\t\t";
                    if (sv.getOcspCertificateStatus().equals(CertificateStatus.OK) || sv.getCrlCertificateStatus().equals(CertificateStatus.OK)) {
                        toWrite += "O Certificado inerente a esta assinatura foi verificado e é válido";
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.REVOKED) || sv.getCrlCertificateStatus().equals(CertificateStatus.REVOKED)) {
                        toWrite += "O Certificado inerente a esta assinatura foi revogado";
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && sv.getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED)) {
                        toWrite += "Não foi feita a verificação da revogação de certificados durante a assinatura";
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHAINED)) {
                        toWrite += "O Certificado não está encadeado a um certificado designado como âncora confiável";
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.EXPIRED)) {
                        toWrite += "O Certificado expirou";
                    } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.CHAINED_LOCALLY)) {
                        toWrite += "A assinatura não contém a âncora completa nem verificações de revogação. No entanto, o certificado do assinante foi emitido por um certificado na âncora confiável";
                    }
                    toWrite += newLine + "\t\t";
                    if (sv.isValidTimeStamp()) {
                        toWrite += "A Assinatura inclui um carimbo de Data e Hora válido";
                    } else {
                        toWrite += "A Data e Hora da assinatura são do relógio do computador do signatário";
                    }
                    toWrite += newLine + "\t\t";

                    toWrite += "Revisão: " + sv.getRevision() + " de " + sv.getNumRevisions();
                    toWrite += newLine + "\t\t";
                    final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    if (sv.getSignature().getTimeStampToken() == null) {
                        Calendar cal = sv.getSignature().getSignDate();
                        String date = df.format(cal.getTime());
                        toWrite += (date + " (hora do computador do signatário)");
                    } else {
                        Calendar ts = sv.getSignature().getTimeStampDate();
                        String date = df.format(ts.getTime());
                        toWrite += "Data: " + (date + " +" + (ts.getTimeZone().getRawOffset() < 10 ? "0" : "") + ts.getTimeZone().getRawOffset());
                    }
                    toWrite += newLine + "\t\t";
                    boolean ltv = (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK);
                    toWrite += "Habilitada para validação a longo termo: " + (ltv ? "Sim" : "Não");
                    String reason = sv.getSignature().getReason();
                    toWrite += newLine + "\t\t";
                    toWrite += "Razão: ";
                    if (reason == null) {
                        toWrite += "Não definida";
                    } else if (reason.equals("")) {
                        toWrite += "Não definida";
                    } else {
                        toWrite += reason;
                    }
                    String location = sv.getSignature().getLocation();
                    toWrite += newLine + "\t\t";
                    toWrite += "Localização: : ";
                    if (location == null) {
                        toWrite += "Não definido";
                    } else if (location.equals("")) {
                        toWrite += "Não definido";
                    } else {
                        toWrite += location;
                    }
                    toWrite += newLine + "\t\t";
                    toWrite += "Permite alterações: ";
                    try {
                        int certLevel = CCInstance.getInstance().getCertificationLevel(sv.getFilename());
                        if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING) {
                            toWrite += "Apenas anotações";
                        } else if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS) {
                            toWrite += "Preenchimento de formulário e anotações";
                        } else if (certLevel == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
                            toWrite += "Não";
                        } else {
                            toWrite += "Sim";
                        }
                    } catch (IOException ex) {
                        controller.Logger.getLogger().addEntry(ex);
                    }
                    toWrite += newLine + "\t\t";
                    if (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK) {
                        toWrite += ("O estado de revogação do certificado inerente a esta assinatura foi verificado com recurso a "
                                + (sv.getOcspCertificateStatus() == CertificateStatus.OK ? "OCSP pela entidade: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getOcsp().getCerts()[0].getSubject(), "CN") + " em " + df.format(sv.getSignature().getOcsp().getProducedAt()) : (sv.getCrlCertificateStatus() == CertificateStatus.OK ? "CRL" : ""))
                                + (sv.getSignature().getTimeStampToken() != null ? "O carimbo de data e hora é válido e foi assinado por: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O") : ""));
                    } else if (sv.getSignature().getTimeStampToken() != null) {
                        toWrite += ("O carimbo de data e hora é válido e foi assinado por: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O"));
                    }
                    toWrite += newLine;
                }
            }
            writeToFile(toWrite);
        }
        System.out.println(toWrite);
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void lblAdditionalInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAdditionalInfoMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (msg != null) {

                JOptionPane.showMessageDialog(null, msg);
            }
        }
    }//GEN-LAST:event_lblAdditionalInfoMouseClicked

    private void writeToFile(String str) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar como");
        boolean validPath = false;
        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("Ficheiro de texto (*.txt)", "txt");
        fileChooser.setFileFilter(pdfFilter);
        File preferedFile = new File("relatorioValidacao.txt");
        fileChooser.setSelectedFile(preferedFile);

        while (!validPath) {
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.CANCEL_OPTION) {
                return;
            }
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String dest = fileChooser.getSelectedFile().getAbsolutePath();
                if (new File(dest).exists()) {
                    String msg = "Já existe um ficheiro na directoria seleccionada com o mesmo nome.\nPretende substituí-lo ou escolher um destino novo?";
                    Object[] options = {"Substituir", "Escolher destino novo", "Cancelar"};
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
                        JOptionPane.showMessageDialog(null, "Relatório de validação guardado com sucesso!", "", JOptionPane.INFORMATION_MESSAGE);
                    } catch (FileNotFoundException ex) {
                        controller.Logger.getLogger().addEntry(ex);
                        JOptionPane.showMessageDialog(null, "Erro ao guardar o relatório de validação!\nVer log para mais informação", "", JOptionPane.ERROR_MESSAGE);
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
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane jsp1;
    private javax.swing.JScrollPane jsp2;
    private javax.swing.JTree jtFiles;
    private javax.swing.JTree jtValidation;
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
