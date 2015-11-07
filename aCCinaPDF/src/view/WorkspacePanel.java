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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import controller.CCInstance;
import exception.AliasException;
import exception.KeyStoreNotLoadedException;
import exception.LibraryNotFoundException;
import exception.LibraryNotLoadedException;
import exception.RevisionExtractionException;
import exception.SignatureFailedException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import listener.SignatureClickListener;
import listener.ValidationListener;
import model.CCAlias;
import model.CCSignatureSettings;
import model.CertificateStatus;
import model.Signature;
import model.SignatureValidation;
import model.TreeNodeWithState;
import model.ValidationTreeCellRenderer;
import org.icepdf.core.pobjects.Document;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

/**
 *
 * @author Diogo
 */
public class WorkspacePanel extends javax.swing.JPanel implements SignatureClickListener {

    private MainWindow mainWindow;
    private final CardLayout cl;
    private Status status;
    private Document document;
    private Signature tempSignature;
    private CCSignatureSettings signatureSettings;

    public enum Status {

        READY,
        SIGNING,
        VALIDATING
    };

    public enum CardEnum {

        SIGN_PANEL,
        VALIDATE_PANEL
    };

    /**
     * Creates new form MiddlePanel
     */
    public WorkspacePanel() {
        initComponents();
        status = Status.READY;
        cl = (CardLayout) this.rightPanel.getLayout();
        topToolbar.setVisible(false);
        bottomToolbar.setVisible(false);
        rightPanel.setVisible(false);
        clSign.setVisible(false);
        rightPanel.setBackground(Color.WHITE);
        lblText.setVisible(false);
        jScrollPane1.setVisible(false);
        btnChangeAppearance.setVisible(false);
        add(jMenuBar1, BorderLayout.NORTH);
        ToolTipManager.sharedInstance().registerComponent(jtValidation);
        jSplitPane1.setDividerSize(0);
        cbVisibleSignature.setSelected(true);
        lblRevision.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clearSignatureFields();
        jsImagePanel.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    e.consume();
                    if (e.getWheelRotation() < 0) {
                        zoomIn();
                    } else {
                        zoomOut();
                    }
                    fixTempSignaturePosition(true);
                }
            }
        });
    }

    public void setParent(MainWindow mw) {
        this.mainWindow = mw;
    }

    public void setDocument(Document document) {
        if (null == document) {
            this.document = null;
            topToolbar.setVisible(false);
            bottomToolbar.setVisible(false);
            imagePanel.clear();
            status = Status.READY;
            btnPageBackward.setEnabled(false);
            btnPageForward.setEnabled(false);
            lblTip.setVisible(true);
            removeTempSignature();
            hideRightPanel();
        } else {
            if (status.equals(Status.SIGNING)) {
                String msg = "Ainda não aplicou a assinatura\nDeseja abrir outro documento?";

                Object[] options = {"Sim", "Não"};
                int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (opt == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            lblTip.setVisible(false);
            this.document = document;
            imagePanel.setDocumentAndComponents(mainWindow, jsImagePanel, document, btnPageBackward, btnPageForward);

        }
        jSplitPane1.setDividerSize(0);
    }

    public void showPanelComponents() {
        jsPageNumber.setModel(new SpinnerNumberModel(1, 1, document.getNumberOfPages(), 1));
        btnZoomIn.setEnabled(true);
        btnZoomOut.setEnabled(true);
        topToolbar.setVisible(true);
        lblTotalPageNumber.setText(" de " + document.getNumberOfPages() + " - Zoom: " + ((int) (imagePanel.getScale() * 100)) + "%");
        bottomToolbar.setVisible(true);
        status = Status.READY;
        btnPageBackward.setEnabled(false);
        btnPageForward.setEnabled(document.getNumberOfPages() != 1);
        removeTempSignature();
        hideRightPanel();
        int numSigs = CCInstance.getInstance().getNumberOfSignatures(document.getDocumentLocation());
        if (numSigs > 0) {
            startValidationThread();
        }

        try {
            boolean permiteAlteracoes = CCInstance.getInstance().getCertificationLevel(document.getDocumentLocation()) != PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED;
            btnSign.setEnabled(permiteAlteracoes);
            btnSign.setToolTipText((permiteAlteracoes ? "Assinar" : "O documento está certificado não permite alterações"));
        } catch (IOException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }
    }

    public void changeCard(CardEnum ce, boolean clear) {
        if (null == document) {
            return;
        }

        if (status.equals(Status.SIGNING) || imagePanel.getStatus().equals(ImagePanel.Status.RENDERING)) {
            return;
        }

        cl.show(this.rightPanel, ce.name());
        jSplitPane1.setDividerSize(5);
        jSplitPane1.setDividerLocation(0.6);
        rightPanel.setVisible(true);

        switch (ce) {
            case SIGN_PANEL: {
                try {
                    if (CCInstance.getInstance().getCertificationLevel(document.getDocumentLocation()) != PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
                        status = Status.SIGNING;
                        startSmartcardSearchThread(clear);
                        if (clear) {
                            clearSignatureFields();
                            createTempSignature();
                            jtValidation.clearSelection();
                        }
                        signatureSettings = new CCSignatureSettings(false);
                        btnImage.setText("Adicionar Imagem de fundo");
                    } else {
                        JOptionPane.showMessageDialog(mainWindow, "Este Documento não pode ser assinado porque foi certificado com um nível\nde certificação que não permite quaisquer alterações ao mesmo.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(WorkspacePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case VALIDATE_PANEL:
                status = Status.VALIDATING;
                break;
        }
    }

    public Status getStatus() {
        return status;
    }

    private void openPdfReaderFromFile(File file) {
        if (testPdf(file)) {
            String filePath = file.getAbsolutePath();
            SwingController sc = new SwingController();
            SwingViewBuilder factory = new SwingViewBuilder(sc);
            JPanel viewerComponentPanel = factory.buildViewerPanel();
            ComponentKeyBinding.install(sc, viewerComponentPanel);
            sc.getDocumentViewController().setAnnotationCallback(new org.icepdf.ri.common.MyAnnotationCallback(sc.getDocumentViewController()));
            JFrame window = new JFrame(filePath);
            window.getContentPane().add(viewerComponentPanel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            sc.openDocument(filePath);
            file.deleteOnExit();
        } else {
            JOptionPane.showMessageDialog(mainWindow, "O Ficheiro extraído está corrompido e não pôde ser aberto", "Erro", JOptionPane.ERROR_MESSAGE);
            controller.Logger.getLogger().addEntry("O Ficheiro está corrompido");
        }
    }

    private String getConfigParameter(String parameter) throws FileNotFoundException, IOException {
        Properties propertiesRead = new Properties();
        String configFile = "aCCinaPDF.cfg";
        String value = "";
        if (!new File(configFile).exists()) {
            signatureSettings = new CCSignatureSettings(true);
            JOptionPane.showMessageDialog(mainWindow, "O ficheiro de configurações não foi encontrado\nFoi criado um novo ficheiro de configurações", "", JOptionPane.INFORMATION_MESSAGE);
        }
        propertiesRead.load(new FileInputStream(configFile));
        value = propertiesRead.getProperty(parameter);
        if (value == null) {
            signatureSettings = new CCSignatureSettings(true);
            JOptionPane.showMessageDialog(mainWindow, "O ficheiro de configurações está corrompido\nFoi criado um novo ficheiro de configurações", "", JOptionPane.INFORMATION_MESSAGE);
        }
        return value;
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

    final ArrayList<SignatureValidation> svList = new ArrayList<>();

    private void startValidationThread() {
        status = Status.VALIDATING;
        svList.clear();
        jtValidation.clearSelection();
        jtValidation.setModel(null);
        showSignatureValidationDetails(null);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    progressBar.setValue(0);
                    jtValidation.setModel(null);
                    jtValidation.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                    jtValidation.setRootVisible(false);
                    ValidationTreeCellRenderer renderer = new ValidationTreeCellRenderer();
                    jtValidation.setCellRenderer(renderer);
                    final int numSigs = CCInstance.getInstance().getNumberOfSignatures(document.getDocumentLocation());
                    if (numSigs > 0) {
                        jScrollPane2.setVisible(true);
                        progressBar.setString("A Validar Assinatura 1 de " + numSigs);
                        progressBar.setMaximum(numSigs);
                        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(null);
                        ValidationListener vl = new ValidationListener() {
                            int numParsed = 1;

                            @Override
                            public void onValidationComplete(SignatureValidation sv) {
                                if (status.equals(Status.VALIDATING)) {
                                    sv.setListener(WorkspacePanel.this);
                                    svList.add(sv);
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
                                        childVerified = new TreeNodeWithState("O Certificado inerente a esta assinatura foi verificado e é válido", (sv.isValid() ? TreeNodeWithState.State.VALID : TreeNodeWithState.State.WARNING));
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
                                        childTimestamp = new TreeNodeWithState("A Assinatura inclui um carimbo de Data e Hora válido", (sv.isValid() ? TreeNodeWithState.State.VALID : TreeNodeWithState.State.WARNING));
                                    } else {
                                        childTimestamp = new TreeNodeWithState("A Data e Hora da assinatura são do relógio do computador do signatário", TreeNodeWithState.State.WARNING);
                                    }

                                    sig.add(childChanged);
                                    sig.add(childVerified);
                                    sig.add(childTimestamp);
                                    top.add(sig);
                                    TreeModel tm = new DefaultTreeModel(top);
                                    jtValidation.setModel(tm);
                                    progressBar.setValue(progressBar.getValue() + 1);
                                    numParsed++;
                                    progressBar.setString("A Validar Assinatura " + numParsed + " de " + numSigs);
                                } else {
                                    CCInstance.getInstance().setValidating(false);
                                }
                            }
                        };
                        CCInstance.getInstance().validatePDF(document.getDocumentLocation(), vl);
                        status = Status.READY;
                        jtValidation.setVisible(true);
                        progressBar.setString("Validação Concluída");
                        if (rightPanel.isVisible() && jtValidation.getRowCount() > 0) {
                            jtValidation.setSelectionRow(jtValidation.getRowCount() - 1);
                        }
                        imagePanel.setSignatureValidationList(svList);
                        panelValidationResult.setVisible(true);
                        status = Status.READY;
                    } else {
                        progressBar.setString("O Documento não está assinado");
                        status = Status.READY;
                        jScrollPane2.setVisible(false);
                    }
                } catch (IOException | DocumentException | GeneralSecurityException ex) {
                    controller.Logger.getLogger().addEntry(ex);
                    status = Status.READY;
                    progressBar.setString("Ocorreu um erro durante a validação");
                    jScrollPane2.setVisible(false);
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    private ScheduledExecutorService exec;

    private void startSmartcardSearchThread(final boolean clear) {
        //rightPanel.setVisible(false);
        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!populateComboBox(clear)) {
                    mainWindow.getLoadingDialog().dispose();
                    exec.shutdown();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
        mainWindow.createLoadingWindow().showDialog(LoadingDialog.LoadingType.SMARTCARD_SEARCHING);
        if (!mainWindow.getLoadingDialog().isVisible()) {
            if (null != exec) {
                exec.shutdown();
            }
        }
    }

    private CCAlias tempCCAlias;

    private boolean populateComboBox(boolean clear) {
        btnCheckAliasCertificate.setEnabled(false);
        cbAlias.removeAllItems();
        cbAlias.setPrototypeDisplayValue("");
        try {
            tempCCAlias = CCInstance.getInstance().loadKeyStoreAndAliases().get(0);
            ArrayList<CCAlias> aList = CCInstance.getInstance().getAliasList();
            if (!aList.isEmpty()) {
                Iterator<CCAlias> ccai = aList.iterator();
                while (ccai.hasNext()) {
                    CCAlias cca = ccai.next();
                    cbAlias.addItem(cca);
                }
                if (null != exec) {
                    if (!exec.isShutdown()) {
                        exec.shutdown();
                        exec = null;
                    }
                }
                changeCard(CardEnum.SIGN_PANEL, clear);
                mainWindow.hideLoadingDialog();
                if (clear) {
                    rightPanel.setVisible(true);
                    jSplitPane1.setDividerLocation(0.6);
                    clSign.setVisible(true);
                    cbAlias.adjustDropDownMenuWidth();
                    cbAlias.repaint();
                    btnCheckAliasCertificate.setEnabled(true);
                    fixTempSignaturePosition(false);
                }
                return true;
            }
        } catch (LibraryNotLoadedException ex) {
            controller.Logger.getLogger().addEntry(ex.getLocalizedMessage());
        } catch (KeyStoreNotLoadedException | CertificateException | KeyStoreException | AliasException ex) {
            controller.Logger.getLogger().addEntry(ex);
        } catch (LibraryNotFoundException ex) {
            controller.Logger.getLogger().addEntry(ex);
            return false;
        }
        if (null == exec) {
            startSmartcardSearchThread(clear);
        }

        if (null != mainWindow.getLoadingDialog()) {
            mainWindow.getLoadingDialog().setText("Insira o CC no leitor...");
        }

        return true;
    }

    private void clearSignatureFields() {
        cbVisibleSignature.setSelected(true);
        btnImage.setVisible(true);
        lblText.setVisible(true);
        jScrollPane1.setVisible(true);
        btnChangeAppearance.setVisible(true);
        btnImage.setVisible(true);
        tfReason.setText("Assinatura Eletrónica Qualificada (DL 62/2003)");
        tfLocation.setText("");
        jRadioButton1.setSelected(true);
        cbTimestamp.setSelected(true);
        tfTimestamp.setText("http://ts.cartaodecidadao.pt/tsa/server");
        tfText.setText("");
    }

    public void createTempSignature() {
        if (null == tempSignature) {
            int signatureWidth;
            int signatureHeight;
            try {
                String stringWidth = getConfigParameter("signatureWidth");
                String stringHeight = getConfigParameter("signatureHeight");
                if (stringWidth == null || stringHeight == null) {
                    createTempSignature();
                    return;
                } else {
                    signatureWidth = Integer.parseInt(stringWidth);
                    signatureHeight = Integer.parseInt(stringHeight);
                }
            } catch (IOException ex) {
                createTempSignature();
                return;
            }
            tempSignature = new Signature(document, imagePanel.getPageNumber(), imagePanel, new Dimension(signatureWidth, signatureHeight));
            int x = ((jsImagePanel.getWidth() / 2) + jsImagePanel.getHorizontalScrollBar().getValue() - (tempSignature.getWidth() / 2));
            int y = (jsImagePanel.getHeight() / 2) + jsImagePanel.getVerticalScrollBar().getValue() - (tempSignature.getHeight() / 2);
            tempSignature.setSize(new Dimension(signatureWidth, signatureHeight));
            tempSignature.setLocation(x, y);
            tempSignature.setVisible(true);
            tempSignature.repaint();
            imagePanel.add(tempSignature, 2, 0);
        }
        fixTempSignaturePosition(true);
        repaint();
    }

    public void removeTempSignature() {
        if (null != tempSignature) {
            tempSignature.destroy();
            tempSignature = null;
            repaint();
        }
    }

    public void hideRightPanel() {
        rightPanel.setVisible(false);
        clSign.setVisible(false);
        jSplitPane1.setDividerSize(0);
        imagePanel.refreshSignatureValidationListPanels();
    }

    public void setEnabledControlComponents(boolean b) {
        btnPageBackward.setEnabled(b);
        btnPageForward.setEnabled(b);
    }

    public void fixTempSignaturePosition(boolean force) {
        if (null != tempSignature) {
            if (!tempSignature.isInsideDocument()) {
                int signatureWidth = 403;
                int signatureHeight = 35;
                try {
                    String stringWidth = getConfigParameter("signatureWidth");
                    String stringHeight = getConfigParameter("signatureHeight");
                    if (stringWidth == null || stringHeight == null) {
                        fixTempSignaturePosition(force);
                        return;
                    } else {
                        signatureWidth = Integer.parseInt(stringWidth);
                        signatureHeight = Integer.parseInt(stringHeight);
                    }
                } catch (IOException ex) {
                    fixTempSignaturePosition(force);
                    return;
                }
                int x = ((jsImagePanel.getWidth() / 2) + jsImagePanel.getHorizontalScrollBar().getValue() - (tempSignature.getWidth() / 2));
                int y = (jsImagePanel.getHeight() / 2) + jsImagePanel.getVerticalScrollBar().getValue() - (tempSignature.getHeight() / 2);
                tempSignature.setSize(new Dimension(signatureWidth, signatureHeight));
                tempSignature.setLocation(x, y);
                if (!tempSignature.isInsideDocument()) {
                    if (force) {
                        forceFixTempSignaturePosition();
                    }
                } else {
                    repaint();
                }
            }
        }
    }

    private void forceFixTempSignaturePosition() {
        if (null != tempSignature) {
            if (!tempSignature.isInsideDocument()) {
                int signatureWidth = (int) document.getPageDimension(imagePanel.getPageNumber(), 0, imagePanel.getScale()).getWidth() / 3;
                int signatureHeight = (int) document.getPageDimension(imagePanel.getPageNumber(), 0, imagePanel.getScale()).getHeight() / 10;
                int x = ((jsImagePanel.getWidth() / 2) + jsImagePanel.getHorizontalScrollBar().getValue() - (tempSignature.getWidth() / 2));
                int y = (jsImagePanel.getHeight() / 2) + jsImagePanel.getVerticalScrollBar().getValue() - (tempSignature.getHeight() / 2);
                tempSignature.setSize(new Dimension(signatureWidth, signatureHeight));
                tempSignature.setLocation(x, y);
                if (!tempSignature.isInsideDocument()) {
                    forceFixTempSignaturePosition();
                } else {
                    repaint();
                }
            }
        }
    }

    @Override
    public void onSignatureClick(SignatureValidation sv) {
        if (status != Status.SIGNING) {
            jtValidation.clearSelection();
            if (!rightPanel.isVisible()) {
                cl.show(this.rightPanel, String.valueOf(CardEnum.VALIDATE_PANEL));
                rightPanel.setVisible(true);
                jSplitPane1.setDividerSize(5);
                jSplitPane1.setDividerLocation(0.6);
            } else if (this.status == Status.SIGNING) {
                String msg = "Ainda não aplicou a assinatura em curso\nDeseja cancelar a assinatura em curso para visualizar a assinatura seleccionada?";
                Object[] options = {"Sim", "Não"};
                int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (opt == JOptionPane.YES_OPTION) {
                    removeTempSignature();
                    cl.show(this.rightPanel, String.valueOf(CardEnum.VALIDATE_PANEL));
                } else {
                    return;
                }
            }

            for (int i = 0; i < jtValidation.getRowCount(); i++) {
                TreePath tp = jtValidation.getPathForRow(i);
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tp.getLastPathComponent();
                if (dmtn.getUserObject() instanceof SignatureValidation) {
                    SignatureValidation sVal = (SignatureValidation) dmtn.getUserObject();
                    if (sv.equals(sVal)) {
                        jtValidation.setSelectionRow(i);
                        jtValidation.expandRow(i);
                    }
                }
            }

            status = Status.READY;
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

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        leftPanel = new javax.swing.JPanel();
        jsImagePanel = new javax.swing.JScrollPane();
        imagePanel = new view.ImagePanel();
        lblTip = new javax.swing.JLabel();
        topToolbar = new javax.swing.JToolBar();
        btnPageBackward = new javax.swing.JButton();
        btnPageForward = new javax.swing.JButton();
        btnZoomOut = new javax.swing.JButton();
        btnZoomIn = new javax.swing.JButton();
        btnSign = new javax.swing.JButton();
        btnValidate = new javax.swing.JButton();
        bottomToolbar = new javax.swing.JToolBar();
        jLabel7 = new javax.swing.JLabel();
        lblPage = new javax.swing.JLabel();
        jsPageNumber = new javax.swing.JSpinner();
        lblTotalPageNumber = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel();
        clSign = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        tabGeneral = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        tfLocation = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tfReason = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbTimestamp = new javax.swing.JCheckBox();
        tfTimestamp = new javax.swing.JTextField();
        lbTimestamp = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        tabAppearance = new javax.swing.JPanel();
        btnImage = new javax.swing.JButton();
        cbVisibleSignature = new javax.swing.JCheckBox();
        btnChangeAppearance = new javax.swing.JButton();
        lblText = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tfText = new javax.swing.JTextArea();
        cbAlias = new view.WideDropDownComboBox();
        btnCheckAliasCertificate = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnApplySignature = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        clValidate = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        panelValidationResult = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtValidation = new javax.swing.JTree();
        panelSignatureDetails = new javax.swing.JPanel();
        btnCheckCertificate = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblRevision = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblReason = new javax.swing.JLabel();
        lblLocation = new javax.swing.JLabel();
        lblLTV = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblAllowsChanges = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblAdditionalInfo = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jSplitPane1.setBorder(null);
        jSplitPane1.setAutoscrolls(true);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(331, 0));
        jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPane1PropertyChange(evt);
            }
        });

        jsImagePanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsImagePanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jsImagePanel.setAutoscrolls(true);
        jsImagePanel.setMinimumSize(new java.awt.Dimension(0, 0));
        jsImagePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jsImagePanelComponentResized(evt);
            }
        });
        jsImagePanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jsImagePanelPropertyChange(evt);
            }
        });

        imagePanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(255, 255, 255)));
        imagePanel.setAutoscrolls(true);
        imagePanel.setPreferredSize(new java.awt.Dimension(1, 1));

        lblTip.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTip.setText("<html><center>Nenhum documento para mostrar<br>Use o explorador da esquerda ou arraste e solte ficheiros ou directorias sobre esta janela para abrir documentos</center></html>");

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTip, javax.swing.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
                .addContainerGap())
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTip, javax.swing.GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE)
                .addContainerGap())
        );

        jsImagePanel.setViewportView(imagePanel);

        topToolbar.setFloatable(false);
        topToolbar.setRollover(true);
        topToolbar.setMinimumSize(new java.awt.Dimension(0, 45));

        btnPageBackward.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        btnPageBackward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/arrow_left.png"))); // NOI18N
        btnPageBackward.setText(" Página Anterior ");
        btnPageBackward.setToolTipText("Mostra a página anterior do documento aberto");
        btnPageBackward.setDisabledIcon(null);
        btnPageBackward.setFocusable(false);
        btnPageBackward.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPageBackward.setIconTextGap(0);
        btnPageBackward.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPageBackward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPageBackwardActionPerformed(evt);
            }
        });
        topToolbar.add(btnPageBackward);

        btnPageForward.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        btnPageForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/arrow_right.png"))); // NOI18N
        btnPageForward.setText(" Página Seguinte ");
        btnPageForward.setToolTipText("Mostra a página seguinte do documento aberto");
        btnPageForward.setDisabledIcon(null);
        btnPageForward.setFocusable(false);
        btnPageForward.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPageForward.setIconTextGap(0);
        btnPageForward.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPageForward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPageForwardActionPerformed(evt);
            }
        });
        topToolbar.add(btnPageForward);

        btnZoomOut.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        btnZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/zoomOut.png"))); // NOI18N
        btnZoomOut.setText(" Zoom - ");
        btnZoomOut.setToolTipText("Reduz o zoom do visualizador do documento");
        btnZoomOut.setDisabledIcon(null);
        btnZoomOut.setFocusable(false);
        btnZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomOut.setIconTextGap(0);
        btnZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomOutActionPerformed(evt);
            }
        });
        topToolbar.add(btnZoomOut);

        btnZoomIn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        btnZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/zoomIn.png"))); // NOI18N
        btnZoomIn.setText(" Zoom + ");
        btnZoomIn.setToolTipText("Aumenta o zoom do visualizador do documento");
        btnZoomIn.setDisabledIcon(null);
        btnZoomIn.setFocusable(false);
        btnZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomIn.setIconTextGap(0);
        btnZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomInActionPerformed(evt);
            }
        });
        topToolbar.add(btnZoomIn);

        btnSign.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        btnSign.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/certified.png"))); // NOI18N
        btnSign.setText(" Assinar ");
        btnSign.setToolTipText("Assinar documento(s)");
        btnSign.setFocusable(false);
        btnSign.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSign.setIconTextGap(0);
        btnSign.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        btnSign.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSignActionPerformed(evt);
            }
        });
        topToolbar.add(btnSign);

        btnValidate.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        btnValidate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/success.png"))); // NOI18N
        btnValidate.setText(" Validar ");
        btnValidate.setToolTipText("Validar documento(s)");
        btnValidate.setFocusable(false);
        btnValidate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnValidate.setIconTextGap(0);
        btnValidate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnValidate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidateActionPerformed(evt);
            }
        });
        topToolbar.add(btnValidate);

        bottomToolbar.setFloatable(false);
        bottomToolbar.setMinimumSize(new java.awt.Dimension(0, 18));

        jLabel7.setText(" ");
        bottomToolbar.add(jLabel7);

        lblPage.setText("Página ");
        bottomToolbar.add(lblPage);

        jsPageNumber.setMaximumSize(new java.awt.Dimension(40, 22));
        jsPageNumber.setMinimumSize(new java.awt.Dimension(40, 22));
        jsPageNumber.setPreferredSize(new java.awt.Dimension(40, 22));
        jsPageNumber.setValue((int)1);
        jsPageNumber.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsPageNumberStateChanged(evt);
            }
        });
        bottomToolbar.add(jsPageNumber);

        lblTotalPageNumber.setText(" de 3");
        bottomToolbar.add(lblTotalPageNumber);

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
            .addComponent(jsImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addComponent(bottomToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addComponent(topToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jsImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setLeftComponent(leftPanel);

        rightPanel.setBackground(new java.awt.Color(255, 255, 255));
        rightPanel.setAutoscrolls(true);
        rightPanel.setMaximumSize(new java.awt.Dimension(99999, 99999));
        rightPanel.setMinimumSize(new java.awt.Dimension(326, 869));
        rightPanel.setLayout(new java.awt.CardLayout());

        clSign.setMaximumSize(new java.awt.Dimension(99999, 99999));
        clSign.setMinimumSize(new java.awt.Dimension(326, 869));
        clSign.setPreferredSize(new java.awt.Dimension(326, 869));

        jLabel1.setText("Painel de Assinatura");

        tabGeneral.setFont(new java.awt.Font("Verdana", 0, 13)); // NOI18N

        jLabel6.setText("Tipo de Assinatura:");

        tfLocation.setToolTipText("Localização");

        jLabel5.setText("Localização:");

        tfReason.setToolTipText("Razão");

        jLabel4.setText("Razão:");

        cbTimestamp.setText("Incluir TimeStamp");
        cbTimestamp.setToolTipText("Incluir um timestamp na assinatura, que garante a veracidade da hora em que o documento foi assinado");
        cbTimestamp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTimestampActionPerformed(evt);
            }
        });

        lbTimestamp.setText("Servidor TimeStamp:");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Sem certificação");
        jRadioButton1.setToolTipText("Será apenas assinado o documento, sem qualquer tipo de certificação. Apesar de não ser invalidada, o documento poderá sofrer alterações futuras");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("<html>Não são permitidas alterações</html>");
        jRadioButton2.setToolTipText("Certificar o documento e bloqueá-lo, impedindo alterações futuras ao mesmo");

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Preenchimento de formulário permitido");
        jRadioButton3.setToolTipText("Certificar o documento, permitindo no entanto novas assinaturas ou certificações");

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText("<html>Preenchimento de formulário e<br>anotações permitidos</html>");
        jRadioButton4.setToolTipText("Certificar o documento, permitindo no entanto novas anotações e assinaturas ou certificações");

        javax.swing.GroupLayout tabGeneralLayout = new javax.swing.GroupLayout(tabGeneral);
        tabGeneral.setLayout(tabGeneralLayout);
        tabGeneralLayout.setHorizontalGroup(
            tabGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tfLocation, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfReason, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, tabGeneralLayout.createSequentialGroup()
                        .addGroup(tabGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lbTimestamp, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jRadioButton3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbTimestamp, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(tfTimestamp, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        tabGeneralLayout.setVerticalGroup(
            tabGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabGeneralLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jRadioButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbTimestamp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbTimestamp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfTimestamp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(345, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Geral", tabGeneral);

        btnImage.setText("Adicionar Imagem de fundo");
        btnImage.setToolTipText("Incluir uma imagem de fundo na assinatura visível");
        btnImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImageActionPerformed(evt);
            }
        });

        cbVisibleSignature.setSelected(true);
        cbVisibleSignature.setText("Assinatura Visível");
        cbVisibleSignature.setToolTipText("Assinatura visível");
        cbVisibleSignature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbVisibleSignatureActionPerformed(evt);
            }
        });

        btnChangeAppearance.setText("Alterar Aparência");
        btnChangeAppearance.setToolTipText("Alterar dados da aparência da assinatura como os dados a apresentar, o tipo de letra, a cor da letra");
        btnChangeAppearance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeAppearanceActionPerformed(evt);
            }
        });

        lblText.setText("Texto adicional da Assinatura:");
        lblText.setToolTipText("Mostrar texto adicional, abaixo da informação básica que pode ser escolhida na janela Alterar Aparência");

        tfText.setColumns(20);
        tfText.setLineWrap(true);
        tfText.setRows(5);
        tfText.setToolTipText("Texto extra a mostrar no bloco da assinatura visível");
        jScrollPane1.setViewportView(tfText);

        javax.swing.GroupLayout tabAppearanceLayout = new javax.swing.GroupLayout(tabAppearance);
        tabAppearance.setLayout(tabAppearanceLayout);
        tabAppearanceLayout.setHorizontalGroup(
            tabAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabAppearanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                    .addComponent(btnImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbVisibleSignature)
                    .addComponent(lblText)
                    .addComponent(btnChangeAppearance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        tabAppearanceLayout.setVerticalGroup(
            tabAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabAppearanceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbVisibleSignature)
                .addGap(3, 3, 3)
                .addComponent(btnImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnChangeAppearance)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Aparência", tabAppearance);

        cbAlias.setToolTipText("");
        cbAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAliasActionPerformed(evt);
            }
        });

        btnCheckAliasCertificate.setText("Mostrar Certificado");
        btnCheckAliasCertificate.setToolTipText("Abre uma janela com os detalhes do certificado no cartão de cidadão");
        btnCheckAliasCertificate.setEnabled(false);
        btnCheckAliasCertificate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckAliasCertificateActionPerformed(evt);
            }
        });

        btnRefresh.setText("Actualizar");
        btnRefresh.setToolTipText("Verifica alterações de cartão inserido no leitor");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnApplySignature.setText("Assinar");
        btnApplySignature.setToolTipText("Aplicar a assinatura no(s) documento(s)");
        btnApplySignature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplySignatureActionPerformed(evt);
            }
        });

        jButton3.setText("Cancelar");
        jButton3.setToolTipText("Cancela esta assinatura e fecha o painel de validação");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout clSignLayout = new javax.swing.GroupLayout(clSign);
        clSign.setLayout(clSignLayout);
        clSignLayout.setHorizontalGroup(
            clSignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clSignLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clSignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, clSignLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(clSignLayout.createSequentialGroup()
                        .addGroup(clSignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTabbedPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, clSignLayout.createSequentialGroup()
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnApplySignature, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(btnCheckAliasCertificate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(clSignLayout.createSequentialGroup()
                                .addComponent(cbAlias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        clSignLayout.setVerticalGroup(
            clSignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clSignLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clSignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbAlias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCheckAliasCertificate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clSignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnApplySignature)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        rightPanel.add(clSign, "SIGN_PANEL");

        clValidate.setMaximumSize(new java.awt.Dimension(99999, 99999));
        clValidate.setMinimumSize(new java.awt.Dimension(326, 869));

        jLabel2.setText("Painel de Validação");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jtValidation.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jtValidation.setMaximumSize(new java.awt.Dimension(46, 9916));
        jtValidation.setRowHeight(32);
        jtValidation.setShowsRootHandles(true);
        jtValidation.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jtValidationValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jtValidation);

        btnCheckCertificate.setText("Mostrar Certificado");
        btnCheckCertificate.setToolTipText("Abre uma janela com os detalhes dos certificado contidos nesta assinatura");
        btnCheckCertificate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckCertificateActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel3.setText("Data:");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel8.setText("Razão:");

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel9.setText("Local:");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel10.setText("Revisão:");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel11.setText("Habilitada para Validação a longo termo:");
        jLabel11.setToolTipText("Referente a se a assinatura contém toda a informação necessária para validar todos os certificados nela contidos");

        lblRevision.setForeground(new java.awt.Color(0, 0, 255));
        lblRevision.setText(" ");
        lblRevision.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRevisionMouseClicked(evt);
            }
        });

        lblDate.setText(" ");

        lblReason.setText(" ");

        lblLocation.setText(" ");

        lblLTV.setText(" ");
        lblLTV.setToolTipText("Referente a se a assinatura contém toda a informação necessária para validar todos os certificados nela contidos");

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel12.setText("Permite alterações:");

        lblAllowsChanges.setText(" ");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel13.setText("Informação adicional:");
        jLabel13.setToolTipText("");

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
            .addComponent(btnCheckCertificate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRevision))
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDate))
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblReason))
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblLocation))
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblLTV))
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblAllowsChanges))
                    .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblAdditionalInfo)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelSignatureDetailsLayout.setVerticalGroup(
            panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSignatureDetailsLayout.createSequentialGroup()
                .addComponent(btnCheckCertificate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblRevision, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(lblReason))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(lblLocation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(lblLTV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(lblAllowsChanges))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSignatureDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(lblAdditionalInfo))
                .addGap(58, 58, 58))
        );

        javax.swing.GroupLayout panelValidationResultLayout = new javax.swing.GroupLayout(panelValidationResult);
        panelValidationResult.setLayout(panelValidationResultLayout);
        panelValidationResultLayout.setHorizontalGroup(
            panelValidationResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addComponent(panelSignatureDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelValidationResultLayout.setVerticalGroup(
            panelValidationResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelValidationResultLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelSignatureDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        progressBar.setStringPainted(true);

        jButton1.setText("Fechar");
        jButton1.setToolTipText("Fecha o painel de validação");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Revalidar");
        jButton2.setToolTipText("Volta a verificar e validar todas as assinaturas neste documento");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout clValidateLayout = new javax.swing.GroupLayout(clValidate);
        clValidate.setLayout(clValidateLayout);
        clValidateLayout.setHorizontalGroup(
            clValidateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clValidateLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clValidateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(clValidateLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, clValidateLayout.createSequentialGroup()
                        .addGroup(clValidateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelValidationResult, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        clValidateLayout.setVerticalGroup(
            clValidateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clValidateLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(10, 10, 10)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelValidationResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        rightPanel.add(clValidate, "VALIDATE_PANEL");

        jSplitPane1.setRightComponent(rightPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomInActionPerformed
        zoomIn();
    }//GEN-LAST:event_btnZoomInActionPerformed

    private void btnZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomOutActionPerformed
        zoomOut();
    }//GEN-LAST:event_btnZoomOutActionPerformed

    public void zoomIn() {
        if (null != document) {
            float scale = imagePanel.scaleUp();
            btnZoomOut.setEnabled(0.3f <= scale);
            btnZoomIn.setEnabled(3f > scale);
            fixTempSignaturePosition(true);
            lblTotalPageNumber.setText(" de " + document.getNumberOfPages() + " - Zoom: " + ((int) (imagePanel.getScale() * 100)) + "%");
        }
    }

    public void zoomOut() {
        if (null != document) {
            float scale = imagePanel.scaleDown();
            btnZoomOut.setEnabled(0.3f <= scale);
            btnZoomIn.setEnabled(3f > scale);
            fixTempSignaturePosition(true);
            lblTotalPageNumber.setText(" de " + document.getNumberOfPages() + " - Zoom: " + ((int) (imagePanel.getScale() * 100)) + "%");
        }
    }

    public void pageUp() {
        if (null != document) {
            if (imagePanel.setPageNumberControl(ImagePanel.DocumentPageControl.PAGE_UP)) {
                jsPageNumber.setValue((imagePanel.getPageNumber() + 1));
                lblTotalPageNumber.setText(" de " + document.getNumberOfPages() + " - Zoom: " + ((int) (imagePanel.getScale() * 100)) + "%");
                jsImagePanel.getHorizontalScrollBar().setValue(0);
                jsImagePanel.getVerticalScrollBar().setValue(0);
                fixTempSignaturePosition(true);
                repaint();
            }
        }
    }

    public void pageDown() {
        if (null != document) {
            if (imagePanel.setPageNumberControl(ImagePanel.DocumentPageControl.PAGE_DOWN)) {
                jsPageNumber.setValue((imagePanel.getPageNumber() + 1));
                lblTotalPageNumber.setText(" de " + document.getNumberOfPages() + " - Zoom: " + ((int) (imagePanel.getScale() * 100)) + "%");
                jsImagePanel.getHorizontalScrollBar().setValue(0);
                jsImagePanel.getVerticalScrollBar().setValue(0);
                fixTempSignaturePosition(true);
                repaint();
            }
        }
    }

    private void btnPageBackwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPageBackwardActionPerformed
        pageDown();
    }//GEN-LAST:event_btnPageBackwardActionPerformed

    private void btnPageForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPageForwardActionPerformed
        pageUp();
    }//GEN-LAST:event_btnPageForwardActionPerformed

    private void jsImagePanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jsImagePanelPropertyChange

    }//GEN-LAST:event_jsImagePanelPropertyChange

    private void btnSignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSignActionPerformed
        changeCard(CardEnum.SIGN_PANEL, true);
    }//GEN-LAST:event_btnSignActionPerformed

    private void btnValidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValidateActionPerformed
        if (mainWindow.getOpenedFiles().size() > 1) {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem m = null;
            ActionListener validateOne = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!rightPanel.isVisible()) {
                        changeCard(CardEnum.VALIDATE_PANEL, true);
                        startValidationThread();
                    } else if (WorkspacePanel.this.status == WorkspacePanel.Status.SIGNING) {
                        String msg = "Ainda não aplicou a assinatura em curso\nDeseja cancelar a assinatura em curso para visualizar a assinatura seleccionada?";
                        Object[] options = {"Sim", "Não"};
                        int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        if (opt == JOptionPane.YES_OPTION) {
                            status = Status.READY;
                            changeCard(CardEnum.VALIDATE_PANEL, true);
                            startValidationThread();
                        }
                    }
                }
            };
            ActionListener validateAll = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MultipleValidationDialog mvd = new MultipleValidationDialog(mainWindow, true, mainWindow.getOpenedFiles());
                    mvd.setLocationRelativeTo(null);
                    mvd.setVisible(true);
                }
            };
            int numSigs = CCInstance.getInstance().getNumberOfSignatures(document.getDocumentLocation());
            if (numSigs == 0) {
                m = new JMenuItem("Este documento não está assinado");
                m.addActionListener(validateOne);
                m.setEnabled(false);
                popup.add(m);
            } else {
                m = new JMenuItem("Validar apenas este Documento");
                m.addActionListener(validateOne);
                popup.add(m);
            }

            if (mainWindow.getSelectedOpenedFiles().size() > 1 && mainWindow.getSelectedOpenedFiles().size() < mainWindow.getOpenedFiles().size()) {
                m = new JMenuItem("Validar os Documentos em lote seleccionados");
                m.addActionListener(validateAll);
                popup.add(m);
                m = new JMenuItem("Validar todos os Documentos em lote");
                m.addActionListener(validateAll);
                popup.add(m);
            } else {
                m = new JMenuItem("Validar todos os Documentos em lote");
                m.addActionListener(validateAll);
                popup.add(m);
            }

            popup.setLightWeightPopupEnabled(true);
            popup.show(btnValidate, 0, btnValidate.getHeight());

        } else {
            if (!rightPanel.isVisible()) {
                changeCard(CardEnum.VALIDATE_PANEL, true);
                startValidationThread();
            } else if (WorkspacePanel.this.status == WorkspacePanel.Status.SIGNING) {
                String msg = "Ainda não aplicou a assinatura em curso\nDeseja cancelar a assinatura em curso para visualizar a assinatura seleccionada?";
                Object[] options = {"Sim", "Não"};
                int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (opt == JOptionPane.YES_OPTION) {
                    status = Status.READY;
                    changeCard(CardEnum.VALIDATE_PANEL, true);
                    startValidationThread();
                } else {
                    return;
                }
            }
        }
        removeTempSignature();
    }//GEN-LAST:event_btnValidateActionPerformed

    private void assinarDocumento(Document document, boolean ocsp, boolean timestamp) {
        try {
            if (tempCCAlias.getCertificate().getPublicKey().equals(CCInstance.getInstance().loadKeyStoreAndAliases().get(0).getCertificate().getPublicKey())) {
                try {
                    String path1 = document.getDocumentLocation();
                    String path2 = null;

                    if (path1.endsWith(".pdf")) {
                        path2 = path1.substring(0, path1.length() - 4).concat("(aCCinado).pdf");
                    }

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Guardar como");
                    if (null != path2) {
                        boolean validPath = false;
                        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf");
                        fileChooser.setFileFilter(pdfFilter);
                        File preferedFile = new File(path2);
                        fileChooser.setCurrentDirectory(preferedFile);
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

                                signatureSettings.setOcspClient(ocsp);
                                signatureSettings.setTimestamp(timestamp);

                                if (validPath) {
                                    if (!CCInstance.getInstance().signPdf(document.getDocumentLocation(), dest, signatureSettings, null)) {
                                        JOptionPane.showMessageDialog(mainWindow, "Erro desconhecido: ver log", "Assinatura falhou", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    status = Status.READY;
                                    ArrayList<File> list = new ArrayList<>();
                                    list.add(new File(document.getDocumentLocation()));
                                    int tempPage = imagePanel.getPageNumber();
                                    mainWindow.closeDocuments(list, false);
                                    mainWindow.loadPdf(new File(dest), false);
                                    hideRightPanel();
                                    imagePanel.setPageNumber(tempPage);
                                    JOptionPane.showMessageDialog(mainWindow, "Assinatura aplicada com sucesso!", "", JOptionPane.INFORMATION_MESSAGE);
                                    break;
                                }
                            }
                        }
                    }
                    return;
                } catch (IOException ex) {
                    if (ex instanceof FileNotFoundException) {
                        JOptionPane.showMessageDialog(mainWindow, "Ficheiro Keystore não foi encontrado", "Assinatura falhou", JOptionPane.ERROR_MESSAGE);
                        controller.Logger.getLogger().addEntry(ex);
                    } else {
                        if (ex.getLocalizedMessage().equals("Erro a abrir o ficheiro de saída!")) {
                            JOptionPane.showMessageDialog(mainWindow, "Impossível criar ficheiro de saída (permissões?)", "Assinatura falhou", JOptionPane.ERROR_MESSAGE);
                            controller.Logger.getLogger().addEntry(ex);
                            assinarDocumento(document, ocsp, timestamp);
                        } else {
                            JOptionPane.showMessageDialog(mainWindow, "Erro desconhecido - Ver log", "Assinatura falhou", JOptionPane.ERROR_MESSAGE);
                            controller.Logger.getLogger().addEntry(ex);
                        }
                    }
                } catch (DocumentException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | HeadlessException | CertificateException | KeyStoreException ex) {
                    controller.Logger.getLogger().addEntry(ex);
                } catch (SignatureFailedException ex) {
                    if (ex.getLocalizedMessage().equals("TimeStamp falhou: Não tem ligação à Internet ou o URL de Servidor de TimeStamp é inválido!")) {
                        //JOptionPane.showMessageDialog(mainWindow, "Não tem ligação à Internet ou\no URL de Servidor de TimeStamp é inválido!", "Assinatura falhou", JOptionPane.ERROR_MESSAGE);
                        String msg = "Não aparenta ter uma ligação válida à Internet ou o URL do Servidor de TimeStamp é inválido\nDeseja assinar o documento mesmo assim?\nAtenção: A validação a longo termo não será possível.";
                        Object[] options = {"Sim", "Não"};
                        int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        if (opt == JOptionPane.YES_OPTION) {
                            assinarDocumento(document, false, false);
                        }
                    } else {
                        controller.Logger.getLogger().addEntry(ex);
                    }
                }
                return;
            } else {
                JOptionPane.showMessageDialog(mainWindow, "SmartCard foi retirado ou alterado!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (LibraryNotLoadedException | KeyStoreNotLoadedException | CertificateException | KeyStoreException | LibraryNotFoundException | AliasException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }
        JOptionPane.showMessageDialog(mainWindow, "SmartCard foi retirado ou alterado!", "Erro", JOptionPane.ERROR_MESSAGE);

    }

    public void assinarLote(CCSignatureSettings settings) {
        ArrayList<File> toSignList = mainWindow.getOpenedFiles();
        try {
            if (tempCCAlias.getCertificate().getPublicKey().equals(CCInstance.getInstance().loadKeyStoreAndAliases().get(0).getCertificate().getPublicKey())) {
                String dest = null;
                Object[] options = {"Guardar nas directorias dos documentos originais", "Escolher pasta destino", "Cancelar"};
                int i = JOptionPane.showOptionDialog(null, "Escolha o destino dos ficheiros assinados", "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (i == 1) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new java.io.File("."));
                    chooser.setDialogTitle("Escolha a directoria destino");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setAcceptAllFileFilterUsed(false);

                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        dest = chooser.getSelectedFile().getAbsolutePath();
                    } else {
                        return;
                    }
                } else if (i == 2) {
                    return;
                }

                MultipleSignDialog msd = new MultipleSignDialog(mainWindow, true, toSignList, settings, dest);
                msd.setLocationRelativeTo(null);
                msd.setVisible(true);

                ArrayList<File> signedDocsList = msd.getSignedDocsList();
                if (!signedDocsList.isEmpty()) {
                    removeTempSignature();
                    clearSignatureFields();
                    hideRightPanel();
                    status = Status.READY;
                    mainWindow.closeDocuments(mainWindow.getOpenedFiles(), false);

                    for (File f : signedDocsList) {
                        mainWindow.loadPdf(f, false);
                    }
                }

                return;
            } else {
                JOptionPane.showMessageDialog(mainWindow, "SmartCard foi retirado ou alterado!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (LibraryNotLoadedException | KeyStoreNotLoadedException | CertificateException | KeyStoreException | LibraryNotFoundException | AliasException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }
        JOptionPane.showMessageDialog(mainWindow, "SmartCard foi retirado ou alterado!", "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void btnCheckCertificateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckCertificateActionPerformed
        DefaultMutableTreeNode dtn = (DefaultMutableTreeNode) jtValidation.getLastSelectedPathComponent();
        SignatureValidation sv = null;
        if (dtn.isLeaf()) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) dtn.getParent();
            sv = (SignatureValidation) dmtn.getUserObject();
        } else {
            sv = (SignatureValidation) dtn.getUserObject();
        }
        CertificatePropertiesDialog cpd = new CertificatePropertiesDialog(mainWindow, true, sv.getSignature().getSignCertificateChain());
        cpd.setLocationRelativeTo(null);
        cpd.setVisible(true);
    }//GEN-LAST:event_btnCheckCertificateActionPerformed

    private void jtValidationValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jtValidationValueChanged
        if (jtValidation.getSelectionRows().length == 0) {
            showSignatureValidationDetails(null);
            imagePanel.setSelectedSignature(null);
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
            imagePanel.setSelectedSignature(sv);
            int pageNumber = sv.getPosList().get(0).page - 1;
            if (imagePanel.getPageNumber() != pageNumber) {
                imagePanel.setPageNumber(pageNumber);
                jsPageNumber.setValue(pageNumber + 1);
            }
        }
    }//GEN-LAST:event_jtValidationValueChanged

    private void showSignatureValidationDetails(SignatureValidation sv) {
        if (null == sv) {
            panelSignatureDetails.setVisible(false);
        } else {
            lblRevision.setText("<html><u>" + sv.getRevision() + " de " + sv.getNumRevisions() + " (Clique para extrair a revisão)</u></html>");
            final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("Z");
            if (sv.getSignature().getTimeStampToken() == null) {
                Calendar cal = sv.getSignature().getSignDate();
                String date = df.format(cal.getTime());
                lblDate.setText(date + " " + sdf.format(cal.getTime()) + " (hora do computador do signatário)");
            } else {
                Calendar ts = sv.getSignature().getTimeStampDate();
                String date = df.format(ts.getTime());
                lblDate.setText(date + " " + sdf.format(ts.getTime()));
            }
            boolean ltv = (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK);
            lblLTV.setText(ltv ? "Sim" : "Não");
            String reason = sv.getSignature().getReason();
            if (reason == null) {
                lblReason.setText("Não definida");
            } else if (reason.isEmpty()) {
                lblReason.setText("Não definida");
            } else {
                lblReason.setText(reason);
            }
            String location = sv.getSignature().getLocation();
            if (location == null) {
                lblLocation.setText("Não definido");
            } else if (location.isEmpty()) {
                lblLocation.setText("Não definido");
            } else {
                lblLocation.setText(location);
            }

            try {
                int certLevel = CCInstance.getInstance().getCertificationLevel(document.getDocumentLocation());
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

    private void btnChangeAppearanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeAppearanceActionPerformed
        signatureSettings.setCcAlias(tempCCAlias);
        signatureSettings.setReason(tfReason.getText());
        signatureSettings.setLocation(tfLocation.getText());
        signatureSettings.setText(tfText.getText());

        AppearanceSettingsDialog njd = new AppearanceSettingsDialog(mainWindow, true, signatureSettings);
        njd.setLocationRelativeTo(null);
        njd.setVisible(true);
    }//GEN-LAST:event_btnChangeAppearanceActionPerformed

    private void cbTimestampActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTimestampActionPerformed
        lbTimestamp.setVisible(cbTimestamp.isSelected());
        tfTimestamp.setVisible(cbTimestamp.isSelected());
    }//GEN-LAST:event_cbTimestampActionPerformed

    private void btnImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImageActionPerformed
        if (tempSignature.getImageLocation() == null) {
            JFileChooser jfc = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "png", "jpg");
            File path = new File(System.getProperty("user.home"));
            jfc.setCurrentDirectory(path);
            jfc.setFileFilter(filter);
            int ret = jfc.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                String file = jfc.getSelectedFile().getAbsolutePath();
                try {
                    tempSignature.setImageLocation(file);
                    btnImage.setText("Remover Imagem de fundo");
                } catch (Exception e) {
                    controller.Logger.getLogger().addEntry(e);
                }
            }
        } else {
            tempSignature.setImageLocation(null);
            btnImage.setText("Adicionar Imagem de fundo");
        }
        tempSignature.repaint();
    }//GEN-LAST:event_btnImageActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String msg = "Deseja realmente cancelar a assinatura?";
        Object[] options = {"Sim", "Não"};
        int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (opt == JOptionPane.YES_OPTION) {
            // Cancelar Assinatura
            if (null != exec) {
                if (!exec.isShutdown()) {
                    exec.shutdown();
                    exec = null;
                    //signatureOptionsPanel.setVisible(false);
                }
            }
            removeTempSignature();
            hideRightPanel();
            status = Status.READY;
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnApplySignatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplySignatureActionPerformed
        if (tempCCAlias != null) {
            signatureSettings.setPageNumber(imagePanel.getPageNumber());
            signatureSettings.setCcAlias(tempCCAlias);
            signatureSettings.setReason(tfReason.getText());
            signatureSettings.setLocation(tfLocation.getText());
            if (jRadioButton1.isSelected()) {
                signatureSettings.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
            } else if (jRadioButton2.isSelected()) {
                signatureSettings.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
            } else if (jRadioButton3.isSelected()) {
                signatureSettings.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_FORM_FILLING);
            } else if (jRadioButton4.isSelected()) {
                signatureSettings.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS);
            }
            signatureSettings.setOcspClient(true);
            if (cbTimestamp.isSelected()) {
                signatureSettings.setTimestamp(true);
                if (tfTimestamp.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(mainWindow, "Escolheu aplicar um timestamp na assinatura.\nO campo 'Servidor TimeStamp' não pode estar em branco", "", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    signatureSettings.setTimestampServer(tfTimestamp.getText());
                }
            } else {
                signatureSettings.setTimestamp(false);
                cbTimestamp.setSelected(false);
                tfTimestamp.setVisible(false);
            }
            signatureSettings.setVisibleSignature(cbVisibleSignature.isSelected());

            if (cbVisibleSignature.isSelected()) {
                Point p = tempSignature.getScaledPositionOnDocument();
                Dimension d = tempSignature.getScaledSizeOnDocument();
                float p1 = (float) p.getX();
                float p3 = (float) d.getWidth() + p1;
                float p2 = (float) ((document.getPageDimension(imagePanel.getPageNumber(), 0).getHeight()) - (p.getY() + d.getHeight()));
                float p4 = (float) d.getHeight() + p2;

                signatureSettings.setVisibleSignature(true);
                if (tempSignature.getImageLocation() != null) {
                    signatureSettings.getAppearance().setImageLocation(tempSignature.getImageLocation());
                }
                Rectangle rect = new Rectangle(p1, p2, p3, p4);
                signatureSettings.setSignaturePositionOnDocument(rect);
                signatureSettings.setText(tfText.getText());
            } else {
                signatureSettings.setVisibleSignature(false);
            }
            if (mainWindow.getOpenedFiles().size() > 1) {
                Object[] options = {"Todos os documentos no lote", "Apenas este documento", "Cancelar"};
                int i = JOptionPane.showOptionDialog(null, "Existem múltiplos documentos no lote\nPretende assinar todos os documentos no lote ou apenas o documento actualmente aberto?", "Múltiplos documentos no lote", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (i == 0) {
                    assinarLote(signatureSettings);
                } else if (i == 1) {
                    assinarDocumento(document, true, true);
                }
            } else {
                assinarDocumento(document, true, true);
            }
        } else {
            JOptionPane.showMessageDialog(mainWindow, "SmartCard não encontrado!", "Erro", JOptionPane.ERROR_MESSAGE);
            changeCard(CardEnum.SIGN_PANEL, false);
        }
    }//GEN-LAST:event_btnApplySignatureActionPerformed

    private void cbVisibleSignatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbVisibleSignatureActionPerformed
        btnImage.setVisible(cbVisibleSignature.isSelected());
        lblText.setVisible(cbVisibleSignature.isSelected());
        jScrollPane1.setVisible(cbVisibleSignature.isSelected());
        btnChangeAppearance.setVisible(cbVisibleSignature.isSelected());
        if (cbVisibleSignature.isSelected()) {
            createTempSignature();
        } else {
            removeTempSignature();
        }
    }//GEN-LAST:event_cbVisibleSignatureActionPerformed

    private void btnCheckAliasCertificateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckAliasCertificateActionPerformed
        if (null != tempCCAlias) {
            X509Certificate x509c = (X509Certificate) tempCCAlias.getCertificate();
            if (null != x509c) {
                CertificatePropertiesDialog cpd = new CertificatePropertiesDialog(mainWindow, true, x509c);
                cpd.setLocationRelativeTo(null);
                cpd.setVisible(true);
            }
        }
    }//GEN-LAST:event_btnCheckAliasCertificateActionPerformed

    private void cbAliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAliasActionPerformed
        tempCCAlias = (CCAlias) cbAlias.getSelectedItem();
    }//GEN-LAST:event_cbAliasActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        startSmartcardSearchThread(false);
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        fixTempSignaturePosition(false);
    }//GEN-LAST:event_formComponentResized

    private void jsImagePanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jsImagePanelComponentResized
        fixTempSignaturePosition(false);
    }//GEN-LAST:event_jsImagePanelComponentResized

    private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane1PropertyChange

    }//GEN-LAST:event_jSplitPane1PropertyChange

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jtValidation.clearSelection();
        hideRightPanel();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jsPageNumberStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsPageNumberStateChanged
        if ((Integer) jsPageNumber.getValue() < 1) {
            jsPageNumber.setValue(1);
        } else if ((Integer) jsPageNumber.getValue() > document.getNumberOfPages()) {
            jsPageNumber.setValue(document.getNumberOfPages());
        }
        imagePanel.setPageNumber((Integer) jsPageNumber.getValue() - 1);
    }//GEN-LAST:event_jsPageNumberStateChanged

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        startValidationThread();
    }//GEN-LAST:event_jButton2ActionPerformed

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
                File f = CCInstance.getInstance().extractRevision(document.getDocumentLocation(), sv.getName());
                openPdfReaderFromFile(f);
            } catch (RevisionExtractionException | IOException ex) {
                Logger.getLogger(WorkspacePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_lblRevisionMouseClicked

    private String msg = null;

    private void lblAdditionalInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAdditionalInfoMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg);
            }
        }
    }//GEN-LAST:event_lblAdditionalInfoMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar bottomToolbar;
    private javax.swing.JButton btnApplySignature;
    private javax.swing.JButton btnChangeAppearance;
    private javax.swing.JButton btnCheckAliasCertificate;
    private javax.swing.JButton btnCheckCertificate;
    private javax.swing.JButton btnImage;
    private javax.swing.JButton btnPageBackward;
    private javax.swing.JButton btnPageForward;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSign;
    private javax.swing.JButton btnValidate;
    private javax.swing.JButton btnZoomIn;
    private javax.swing.JButton btnZoomOut;
    private javax.swing.ButtonGroup buttonGroup1;
    private view.WideDropDownComboBox cbAlias;
    private javax.swing.JCheckBox cbTimestamp;
    private javax.swing.JCheckBox cbVisibleSignature;
    private javax.swing.JPanel clSign;
    private javax.swing.JPanel clValidate;
    private view.ImagePanel imagePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane jsImagePanel;
    private javax.swing.JSpinner jsPageNumber;
    private javax.swing.JTree jtValidation;
    private javax.swing.JLabel lbTimestamp;
    private javax.swing.JLabel lblAdditionalInfo;
    private javax.swing.JLabel lblAllowsChanges;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblLTV;
    private javax.swing.JLabel lblLocation;
    private javax.swing.JLabel lblPage;
    private javax.swing.JLabel lblReason;
    private javax.swing.JLabel lblRevision;
    private javax.swing.JLabel lblText;
    private javax.swing.JLabel lblTip;
    private javax.swing.JLabel lblTotalPageNumber;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel panelSignatureDetails;
    private javax.swing.JPanel panelValidationResult;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel tabAppearance;
    private javax.swing.JPanel tabGeneral;
    private javax.swing.JTextField tfLocation;
    private javax.swing.JTextField tfReason;
    private javax.swing.JTextArea tfText;
    private javax.swing.JTextField tfTimestamp;
    private javax.swing.JToolBar topToolbar;
    // End of variables declaration//GEN-END:variables

}
