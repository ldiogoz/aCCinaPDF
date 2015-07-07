/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import com.itextpdf.text.pdf.PdfReader;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import model.FileListTreeCellRenderer;
import model.FileSystemModel;
import model.TooltipTreeCellRenderer;
import org.apache.commons.lang3.SystemUtils;
import org.icepdf.ri.common.SwingController;

/**
 *
 * @author Diogo
 */
public class MainWindow extends javax.swing.JFrame implements KeyEventDispatcher {

    private File openedFile;
    private File openedFolder;
    private boolean loading;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        List<Image> icons = new ArrayList<>();
        icons.add(new ImageIcon(getClass().getResource("/image/aCCinaPDF_logo_icon32.png")).getImage());
        this.setIconImages(icons);

        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        workspacePanel.setParent((MainWindow) this);
        populateDriveComboBox();

        dmtn = new DefaultMutableTreeNode("0 Documentos em lote");
        TreeModel tm = new DefaultTreeModel(dmtn);
        jtOpenedDocuments.setModel(tm);
        setupTreePopups();
        setupDropListener();

        DefaultTreeCellRenderer renderer1 = (DefaultTreeCellRenderer) jtOpenedDocuments.getCellRenderer();
        Icon closedIcon = new ImageIcon(MainWindow.class.getResource("/image/pdf_ico.jpg"));
        renderer1.setLeafIcon(closedIcon);
        DefaultTreeCellRenderer renderer2 = (DefaultTreeCellRenderer) jtExplorer.getCellRenderer();
        renderer2.setLeafIcon(closedIcon);
        tfProcurar.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTree(tfProcurar.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTree(tfProcurar.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher((MainWindow) this);
        refreshTree(null);

        ctrl.setPrintMenuItem(jMenuItem2);
    }

    private void setupTreePopups() {
        jtOpenedDocuments.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popup = new JPopupMenu();
                    if (null != jtOpenedDocuments.getSelectionRows()) {
                        if (1 >= jtOpenedDocuments.getSelectionRows().length) {
                            int row = jtOpenedDocuments.getClosestRowForLocation(e.getX(), e.getY());
                            jtOpenedDocuments.setSelectionRow(row);
                        } else {
                            int row = jtOpenedDocuments.getClosestRowForLocation(e.getX(), e.getY());
                            boolean selected = false;
                            for (int i : jtOpenedDocuments.getSelectionRows()) {
                                if (i == row) {
                                    selected = true;
                                    break;
                                }
                            }
                            if (!selected) {
                                jtOpenedDocuments.setSelectionRow(row);
                            }
                        }

                        JMenuItem m = null;
                        ActionListener open = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                loadPdf(getSelectedFile(jtOpenedDocuments), true);
                            }
                        };

                        ActionListener remove = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                closeDocument(true);
                            }
                        };

                        ActionListener show = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    Desktop.getDesktop().open(getSelectedFile(jtOpenedDocuments).getParentFile());
                                } catch (IOException ex) {
                                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        };

                        if (!openedFile.equals(getSelectedFile(jtOpenedDocuments))) {
                            m = new JMenuItem("Abrir");
                            m.addActionListener(open);
                            popup.add(m);
                        }
                        m = new JMenuItem("Remover do lote");
                        m.addActionListener(remove);
                        popup.add(m);
                        m = new JMenuItem("Mostrar localização no explorador");
                        m.addActionListener(show);
                        popup.add(m);

                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        jtExplorer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popup = new JPopupMenu();
                    if (null != jtExplorer.getSelectionRows()) {
                        if (1 >= jtExplorer.getSelectionRows().length) {
                            int row = jtExplorer.getClosestRowForLocation(e.getX(), e.getY());
                            jtExplorer.setSelectionRow(row);
                        } else {
                            int row = jtExplorer.getClosestRowForLocation(e.getX(), e.getY());
                            boolean selected = false;
                            for (int i : jtExplorer.getSelectionRows()) {
                                if (i == row) {
                                    selected = true;
                                    break;
                                }
                            }
                            if (!selected) {
                                jtExplorer.setSelectionRow(row);
                            }
                        }

                        JMenuItem m = null;
                        ActionListener loadFile = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (jtExplorer.getSelectionRows().length == 1) {
                                    loadPdf(getSelectedFile(jtExplorer), true);
                                } else {
                                    ArrayList<File> sl = getMultipleSelectedFiles(jtExplorer);
                                    for (File f : sl) {
                                        loadPdf(f, false);
                                    }
                                }
                            }
                        };
                        ActionListener loadFolder = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (jtExplorer.getSelectionRows().length == 1) {
                                    File f = getSelectedFile(jtExplorer);
                                    loadFolder(f, false);
                                } else {
                                    ArrayList<File> sl = getMultipleSelectedFiles(jtExplorer);
                                    for (File f : sl) {
                                        loadFolder(f, false);
                                    }
                                }
                            }
                        };
                        ActionListener loadFolderRecursively = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (jtExplorer.getSelectionRows().length == 1) {
                                    File f = getSelectedFile(jtExplorer);
                                    loadFolder(f, true);
                                } else {
                                    ArrayList<File> sl = getMultipleSelectedFiles(jtExplorer);
                                    for (File s : sl) {
                                        loadFolder(s, true);
                                    }
                                }
                            }
                        };
                        ActionListener remove = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ArrayList<File> listaF = getMultipleSelectedFiles(jtExplorer);
                                closeDocuments(listaF, true);
                            }
                        };

                        ActionListener show = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    Desktop.getDesktop().open(getSelectedFile(jtExplorer).isFile() ? getSelectedFile(jtExplorer).getParentFile() : getSelectedFile(jtExplorer));
                                } catch (IOException ex) {
                                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        };

                        if (files.isEmpty()) {
                            if (jtExplorer.getSelectionRows().length <= 1) {
                                File f = getSelectedFile(jtExplorer);
                                if (f.isDirectory()) {
                                    m = new JMenuItem("Abrir Documentos nesta Pasta");
                                    m.addActionListener(loadFolder);
                                    popup.add(m);
                                    m = new JMenuItem("Abrir Documentos nesta Pasta e nas Subpastas");
                                    m.addActionListener(loadFolderRecursively);
                                    popup.add(m);
                                    m = new JMenuItem("Mostrar no explorador");
                                    m.addActionListener(show);
                                    popup.add(m);
                                } else {
                                    m = new JMenuItem("Abrir Documento");
                                    m.addActionListener(loadFile);
                                    popup.add(m);
                                    m = new JMenuItem("Mostrar localização no explorador");
                                    m.addActionListener(show);
                                    popup.add(m);
                                }

                            } else {
                                ArrayList<File> l = getMultipleSelectedFiles(jtExplorer);
                                boolean file = l.get(0).isFile();
                                boolean allSame = true;
                                for (File f : l) {
                                    if (file != f.isFile()) {
                                        allSame = false;
                                        break;
                                    }
                                }
                                if (allSame) {
                                    if (file) {
                                        m = new JMenuItem("Abrir Documentos");
                                        m.addActionListener(loadFile);
                                        popup.add(m);
                                    } else {
                                        m = new JMenuItem("Abrir Documentos nestas Pastas");
                                        m.addActionListener(loadFolder);
                                        popup.add(m);
                                    }
                                }
                            }
                        } else {
                            File f = getSelectedFile(jtExplorer);
                            if (f.isDirectory()) {
                                m = new JMenuItem("Abrir Documentos nesta Pasta");
                                m.addActionListener(loadFolder);
                                popup.add(m);
                                m = new JMenuItem("Abrir Documentos nesta Pasta e nas Subpastas");
                                m.addActionListener(loadFolderRecursively);
                                popup.add(m);
                            } else {
                                if (files.contains(getSelectedFile(jtExplorer))) {
                                    m = new JMenuItem("Remover do lote");
                                    m.addActionListener(remove);
                                    popup.add(m);
                                } else {
                                    m = new JMenuItem("Juntar ao lote");
                                    m.addActionListener(loadFile);
                                    popup.add(m);
                                }
                            }
                        }
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        jtExplorer.setCellRenderer(new TooltipTreeCellRenderer());
        ToolTipManager.sharedInstance().registerComponent(jtExplorer);

        FileListTreeCellRenderer renderer1 = new FileListTreeCellRenderer((MainWindow) this, jtOpenedDocuments);
        jtOpenedDocuments.setCellRenderer(renderer1);
        ToolTipManager.sharedInstance().registerComponent(jtOpenedDocuments);
    }

    public File getOpenedFile() {
        return openedFile;
    }

    private void setupDropListener() {
        this.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        for (File file : droppedFiles) {
                            if (file.exists() && file.isFile() && file.getAbsolutePath().endsWith(".pdf")) {
                                if (loadPdf(file, false)) {
                                    ctrl.openDocument(file.getAbsolutePath());
                                    workspacePanel.setDocument(ctrl.getDocument());
                                } else {
                                    errorList.add(file.getAbsolutePath());
                                }
                            } else if (file.exists() && file.isDirectory()) {
                                loadFolder(file, true);
                            }
                        }
                        showErrors();
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    controller.Logger.getLogger().addEntry(ex);
                }
            }
        });
    }

    private void populateDriveComboBox() {
        DefaultComboBoxModel dcbm = new DefaultComboBoxModel();

        if (SystemUtils.IS_OS_WINDOWS) {
            File[] roots;
            roots = File.listRoots();
            if (roots.length > 1) {
                for (File root : roots) {
                    dcbm.addElement(root.getAbsolutePath());
                }
                dcbm.addElement(System.getProperty("user.name"));
                dcbm.addElement("Ambiente de Trabalho");
                jtExplorer.setModel(new FileSystemModel(new File(roots[0].getAbsolutePath()), null));
            }
        } else {
            String userDir = System.getProperty("user.home");
            dcbm.addElement("/");
            dcbm.addElement(userDir);
            jtExplorer.setModel(new FileSystemModel(new File(userDir), null));
        }
        cbVolume.setModel(dcbm);
        cbVolume.adjustDropDownMenuWidth();
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
        leftPanel = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        btnRefreshTree = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtExplorer = new javax.swing.JTree();
        tfProcurar = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        cbVolume = new model.WideDropDownComboBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtOpenedDocuments = new javax.swing.JTree();
        lblOpenedDocuments = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel();
        workspacePanel = new view.WorkspacePanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("aCCinaPDF");
        setIconImages(null);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jSplitPane1MouseReleased(evt);
            }
        });
        jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPane1PropertyChange(evt);
            }
        });

        jSplitPane2.setDividerLocation(200);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setMinimumSize(new java.awt.Dimension(0, 7));

        btnRefreshTree.setText("Actualizar");
        btnRefreshTree.setMinimumSize(new java.awt.Dimension(0, 25));
        btnRefreshTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshTreeActionPerformed(evt);
            }
        });

        jtExplorer.setFont(new java.awt.Font("SansSerif", 0, 15)); // NOI18N
        jtExplorer.setRowHeight(20);
        jtExplorer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jtExplorerMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jtExplorer);

        tfProcurar.setMinimumSize(new java.awt.Dimension(0, 22));

        jLabel1.setText("Procurar por:");
        jLabel1.setMinimumSize(new java.awt.Dimension(0, 16));

        cbVolume.setMinimumSize(new java.awt.Dimension(0, 22));
        cbVolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbVolumeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cbVolume, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefreshTree, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfProcurar, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefreshTree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfProcurar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setBottomComponent(jPanel2);

        jtOpenedDocuments.setFont(new java.awt.Font("SansSerif", 0, 15)); // NOI18N
        jtOpenedDocuments.setModel(null);
        jtOpenedDocuments.setRootVisible(false);
        jtOpenedDocuments.setRowHeight(20);
        jtOpenedDocuments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jtOpenedDocumentsMousePressed(evt);
            }
        });
        jtOpenedDocuments.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jtOpenedDocumentsComponentResized(evt);
            }
        });
        jtOpenedDocuments.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jtOpenedDocumentsValueChanged(evt);
            }
        });
        jtOpenedDocuments.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtOpenedDocumentsKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jtOpenedDocuments);

        lblOpenedDocuments.setText("0 Documentos em lote:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(lblOpenedDocuments)
                        .addGap(0, 140, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblOpenedDocuments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setLeftComponent(jPanel3);

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(leftPanel);

        rightPanel.setPreferredSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(workspacePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 953, Short.MAX_VALUE)
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(workspacePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 895, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(rightPanel);

        jMenu1.setText("Ficheiro");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Imprimir");
        jMenu1.add(jMenuItem2);

        jMenuItem1.setText("Sair");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu3.setText("Opções");

        jMenuItem3.setText("Preferências");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Ajuda");

        jMenuItem6.setText("Ver Log");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem6);

        jMenuItem7.setText("Teclas de Atalho");
        jMenu4.add(jMenuItem7);

        jMenuItem8.setText("Sobre");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem8);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1259, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void refreshTree(String match) {
        String volume = (String) cbVolume.getSelectedItem();
        String path;
        if (SystemUtils.IS_OS_WINDOWS) {
            if (volume.equals(System.getProperty("user.name"))) {
                path = System.getProperty("user.home");
            } else if (volume.equals("Ambiente de Trabalho")) {
                path = System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop";
            } else {
                path = volume;
            }
        } else {
            path = volume;
        }
        TreePath[] tempTP = null;
        if (volume.equals(tempVolumeLabel)) {
            tempTP = jtExplorer.getSelectionPaths();
        } else {
            tempVolumeLabel = volume;
        }
        Enumeration<TreePath> e = jtExplorer.getExpandedDescendants(jtExplorer.getPathForRow(0));
        jtExplorer.setModel(new FileSystemModel(new File(path), match));
        while (null != e && e.hasMoreElements()) {
            jtExplorer.expandPath(e.nextElement());
        }
        if (volume.equals(tempVolumeLabel)) {
            jtExplorer.setSelectionPaths(tempTP);
        }
        if (jtExplorer.getSelectionRows() != null) {
            if (jtExplorer.getSelectionRows().length == 1) {
                jtExplorer.scrollRowToVisible(jtExplorer.getSelectionRows()[0]);
            }
        }
    }

    private String tempVolumeLabel;

    private void btnRefreshTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshTreeActionPerformed
        refreshTree(null);
    }//GEN-LAST:event_btnRefreshTreeActionPerformed

    private void jtExplorerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtExplorerMouseClicked
        if (2 == evt.getClickCount()) {
            if (jtExplorer.getSelectionRows().length != 0) {
                if (1 == jtExplorer.getSelectionRows().length && jtExplorer.getSelectionRows()[0] != 0) {
                    // 1 ficheiro seleccionado
                    File file = getSelectedFile(jtExplorer);
                    if (file.exists() && file.isFile() && file.getAbsolutePath().endsWith(".pdf")) {
                        loadPdf(file, true);
                    }
                }
            }
        }
    }//GEN-LAST:event_jtExplorerMouseClicked

    private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane1PropertyChange
        String propertyName = evt.getPropertyName();
        if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
            workspacePanel.fixTempSignaturePosition(false);
        }
    }//GEN-LAST:event_jSplitPane1PropertyChange

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        workspacePanel.fixTempSignaturePosition(true);
    }//GEN-LAST:event_formComponentResized

    private void jtOpenedDocumentsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jtOpenedDocumentsValueChanged

    }//GEN-LAST:event_jtOpenedDocumentsValueChanged

    public void closeDocument(boolean showCloseDialog) {
        if (!workspacePanel.getStatus().equals(WorkspacePanel.Status.READY)) {
            return;
        }

        if (!files.isEmpty()) {
            int opt = -1;
            if (showCloseDialog) {
                String msg;
                if (jtOpenedDocuments.getSelectionRows().length == 1) {
                    msg = "Tem a certeza que quer fechar o documento seleccionado?";
                } else {
                    msg = "Tem a certeza que quer fechar todos os documentos seleccionados do lote?";
                }
                Object[] options = {"Sim", "Não"};
                opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            }
            boolean closedOpenedDocument = false;
            if (!showCloseDialog || opt == JOptionPane.YES_OPTION) {
                // Apagar da lista de documentos abertos
                for (int i = jtOpenedDocuments.getSelectionRows().length - 1; i >= 0; i--) {
                    int index = jtOpenedDocuments.getSelectionRows()[i];
                    String path = jtOpenedDocuments.getPathForRow(index).getPathComponent(1).toString();
                    // if (index < dmtn.getChildCount()) {
                    //dmtn.remove(index);
                    files.remove(new File(path));
                    // }
                    if (null != openedFile) {
                        if (openedFile.equals(new File(path))) {
                            System.err.println("yeees");
                            closedOpenedDocument = true;
                            clearOpenedDocument();
                            setTitle(null);
                        }
                    }
                }
                dmtn.removeAllChildren();
                for (File file : files) {
                    dmtn.insert(new DefaultMutableTreeNode(file), 0);
                }
                int numOpened = dmtn.getChildCount();
                if (1 == numOpened) {
                    lblOpenedDocuments.setText("1 Documento em lote:");
                    dmtn.setUserObject(null);
                } else {
                    lblOpenedDocuments.setText(numOpened + " Documentos em lote:");
                    dmtn.setUserObject(null);
                }
                TreeModel tm = new DefaultTreeModel(dmtn);
                jtOpenedDocuments.setModel(tm);
                if (closedOpenedDocument && !files.isEmpty()) {
                    File first = files.get(files.size() - 1);
                    jtOpenedDocuments.setSelectionRow(0);
                    ctrl.openDocument(first.getAbsolutePath());
                    workspacePanel.setDocument(ctrl.getDocument());
                    openedFile = first;
                }
            }
        }
    }

    public void closeDocuments(ArrayList<File> listaD, boolean showCloseDialog) {
        if (!workspacePanel.getStatus().equals(WorkspacePanel.Status.READY)) {
            return;
        }

        if (!listaD.isEmpty()) {
            int opt = -1;
            if (showCloseDialog) {
                String msg;
                if (listaD.size() == 1) {
                    msg = "Tem a certeza que quer fechar o documento seleccionado?";
                } else {
                    msg = "Tem a certeza que quer fechar todos os documentos seleccionados do lote?";
                }

                Object[] options = {"Sim", "Não"};
                opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            }
            if (!showCloseDialog || opt == JOptionPane.YES_OPTION) {
                // Apagar da lista de documentos abertos
                boolean closedOpenedDocument = false;
                for (int r = jtOpenedDocuments.getRowCount() - 1; r >= 0; r--) {
                    String str = "";
                    Object[] paths = jtOpenedDocuments.getPathForRow(r).getPath();
                    for (int i = 1; i < paths.length; i++) {
                        str += paths[i];
                        if (i + 1 < paths.length) {
                            str += File.separator;
                        }
                    }
                    File currFile = new File(str.replaceAll("\\\\+", "\\\\"));
                    for (File f : listaD) {
                        if (f.getAbsolutePath().replaceAll("\\\\+", "\\\\").equals(currFile.getAbsolutePath())) {
                            dmtn.remove(r);
                            files.remove(currFile);
                            if (null != openedFile) {
                                if (openedFile.equals(currFile)) {
                                    closedOpenedDocument = true;
                                    clearOpenedDocument();
                                    setTitle(null);
                                }
                            }
                            break;
                        }
                    }

                }
                int numOpened = files.size();
                if (1 == numOpened) {
                    lblOpenedDocuments.setText("1 Documento em lote:");
                    dmtn.setUserObject(null);
                } else {
                    lblOpenedDocuments.setText(numOpened + " Documentos em lote:");
                    dmtn.setUserObject(null);
                }
                TreeModel tm = new DefaultTreeModel(dmtn);
                jtOpenedDocuments.setModel(tm);

                if (closedOpenedDocument && !files.isEmpty()) {
                    File first = files.get(files.size() - 1);
                    jtOpenedDocuments.setSelectionRow(0);
                    ctrl.openDocument(first.getAbsolutePath());
                    workspacePanel.setDocument(ctrl.getDocument());
                    openedFile = first;
                }
            }
        }
    }

    private void jtOpenedDocumentsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtOpenedDocumentsKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            closeDocument(true);
        }
    }//GEN-LAST:event_jtOpenedDocumentsKeyPressed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed

    }//GEN-LAST:event_formKeyPressed

    private void cbVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbVolumeActionPerformed
        refreshTree(null);
    }//GEN-LAST:event_cbVolumeActionPerformed

    private void jSplitPane1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSplitPane1MouseReleased

    }//GEN-LAST:event_jSplitPane1MouseReleased

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        LogWindow lw = new LogWindow();
        lw.setLocationRelativeTo(null);
        lw.setVisible(true);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jtOpenedDocumentsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtOpenedDocumentsMousePressed
        if (evt.getClickCount() == 2) {
            if (openedFile != null && jtOpenedDocuments.getRowCount() > 0 && jtOpenedDocuments.getSelectionRows().length != 1) {
                //TreePath treePath = evt.getOldLeadSelectionPath();
                //jtOpenedDocuments.setSelectionPath(treePath);
            } else {
                if (null == jtOpenedDocuments.getSelectionPath()) {
                    return;
                }
                // 1 ficheiro seleccionado
                String selectedFile = "";
                Object[] paths = jtOpenedDocuments.getSelectionPath().getPath();
                for (int i = 1; i < paths.length; i++) {
                    selectedFile += paths[i];
                    if (i + 1 < paths.length) {
                        selectedFile += File.separator;
                    }
                }
                File file = new File(selectedFile);
                if (file.exists() && file.isFile() && selectedFile.endsWith(".pdf")) {
                    ctrl.openDocument(file.getAbsolutePath());
                    workspacePanel.setDocument(ctrl.getDocument());
                    openedFile = file;
                } else if (file.exists() && file.isDirectory() && null != file.list()) {
                    if (0 < file.list().length) {
                        loadFolder(file, true);
                    }
                }
            }
        }
    }//GEN-LAST:event_jtOpenedDocumentsMousePressed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        AboutDialog ad = new AboutDialog(this, true);
        ad.setLocationRelativeTo(null);
        ad.setVisible(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jtOpenedDocumentsComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jtOpenedDocumentsComponentResized
        jtOpenedDocuments.updateUI();
    }//GEN-LAST:event_jtOpenedDocumentsComponentResized

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        SettingsDialog sd = new SettingsDialog(this, true);
        sd.setLocationRelativeTo(null);
        sd.setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private LoadingDialog loadingDialog;

    public LoadingDialog getLoadingDialog() {
        return loadingDialog;
    }

    public LoadingDialog createLoadingWindow() {
        if (null == loadingDialog) {
            loadingDialog = new LoadingDialog(this, true);
            loadingDialog.setLocationRelativeTo(null);
        }
        return this.loadingDialog;
    }

    public boolean hideLoadingDialog() {
        setLoading(false);
        if (null != loadingDialog) {
            loadingDialog.dispose();
            this.setVisible(true);
            return (!loadingDialog.isCanceled());
        }
        return true;
    }

    private void clearOpenedDocument() {
        workspacePanel.setDocument(null);
        openedFile = null;
    }

    private File getSelectedFile(JTree jtree) {
        String selectedFile = "";
        if (jtree.getSelectionPath() != null) {
            Object[] paths = jtree.getSelectionPath().getPath();
            for (int i = 0; i < paths.length; i++) {
                selectedFile += paths[i];
                if (!paths[i].toString().isEmpty() && i + 1 < paths.length) {
                    selectedFile += File.separator;
                }
            }
            return new File(selectedFile);
        } else {
            return null;
        }
    }

    private ArrayList<File> getMultipleSelectedFiles(JTree jtree) {
        ArrayList<File> aLSelectedFiles = new ArrayList<>();
        for (TreePath tp : jtree.getSelectionPaths()) {
            String strSelectedFile = "";
            Object[] paths = tp.getPath();
            for (int i = 0; i < paths.length; i++) {
                strSelectedFile += paths[i];
                if (i + 1 < paths.length) {
                    strSelectedFile += File.separator;
                }
            }
            aLSelectedFiles.add(new File(strSelectedFile));
        }
        return aLSelectedFiles;
    }

    public ArrayList<File> getSelectedOpenedFiles() {
        return getMultipleSelectedFiles(jtOpenedDocuments);
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
            controller.Logger.getLogger().addEntry(ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefreshTree;
    private model.WideDropDownComboBox cbVolume;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTree jtExplorer;
    private javax.swing.JTree jtOpenedDocuments;
    private javax.swing.JLabel lblOpenedDocuments;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JTextField tfProcurar;
    private view.WorkspacePanel workspacePanel;
    // End of variables declaration//GEN-END:variables

    private final SwingController ctrl = new SwingController();

    public boolean loadPdf(File selectedFile, boolean showOpenDialog) {
        if (null == openedFile || !openedFile.equals(selectedFile)) {
            if (selectedFile.isFile() && selectedFile.exists()) {
                try {
                    if (null == dmtn) {
                        lblOpenedDocuments.setText("0 Documentos em lote");
                        dmtn = new DefaultMutableTreeNode(null);
                    }
                    boolean exists = false;
                    for (int i = 0; i < dmtn.getChildCount(); i++) {
                        if (String.valueOf(dmtn.getChildAt(i)).equals(selectedFile.getAbsolutePath())) {
                            exists = true;

                            jtOpenedDocuments.setSelectionRow(i);
                            ctrl.openDocument(selectedFile.getAbsolutePath());
                            workspacePanel.setDocument(ctrl.getDocument());
                            openedFile = selectedFile;
                            break;
                        }
                    }
                    if (!exists) {
                        if (testPdf(selectedFile)) {
                            dmtn.insert(new DefaultMutableTreeNode(selectedFile), 0);
                            TreeModel tm = new DefaultTreeModel(dmtn);
                            jtOpenedDocuments.setModel(tm);
                            // jtOpenedDocuments.setSelectionRow(1);
                            int numOpened = dmtn.getChildCount();
                            if (1 == numOpened) {
                                lblOpenedDocuments.setText("1 Documento em lote:");
                                dmtn.setUserObject(null);
                            } else {
                                lblOpenedDocuments.setText(numOpened + " Documentos em lote:");
                                dmtn.setUserObject(null);
                            }

                            if (null == openedFile) {
                                jtOpenedDocuments.setSelectionRow(0);
                                jtOpenedDocuments.scrollRowToVisible(0);
                                ctrl.openDocument(selectedFile.getAbsolutePath());
                                workspacePanel.setDocument(ctrl.getDocument());
                                files.add(new File(selectedFile.getAbsolutePath()));
                                openedFile = selectedFile;
                                return true;

                            } else {
                                jtOpenedDocuments.setSelectionRow(1);
                                jtOpenedDocuments.scrollRowToVisible(1);
                                if (showOpenDialog) {
                                    String msg = "Deseja abrir o documento carregado ou apenas adicioná-lo ao lote e manter o que está actualmente aberto?";
                                    Object[] options = {"Sim", "Apenas adicionar ao lote"};
                                    int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                                    if (opt == JOptionPane.YES_OPTION) {
                                        jtOpenedDocuments.setSelectionRow(0);
                                        ctrl.openDocument(selectedFile.getAbsolutePath());
                                        workspacePanel.setDocument(ctrl.getDocument());
                                        openedFile = selectedFile;
                                    }
                                }
                                files.add(new File(selectedFile.getAbsolutePath()));
                                return true;
                            }
                        } else {
                            errorList.add(selectedFile.getAbsolutePath());
                            showErrors();
                        }
                    }
                } catch (Exception e) {
                    controller.Logger.getLogger().addEntry(e);
                }
            }
        }
        return false;
    }

    private void loadFolder(File selectedFolder, boolean recursively) {
        if (selectedFolder.isDirectory() && selectedFolder.exists()) {
            try {
                openedFolder = selectedFolder;
                createLoadingWindow();
                listFilesFolder(openedFolder, recursively);
                loadingDialog.showDialog(LoadingDialog.LoadingType.FILE_SEARCHING);
                if (hideLoadingDialog()) {
                    for (File f : tempFiles) {
                        files.add(f);
                    }
                    showErrors();
                    refreshOpenedDocumentsList();
                } else {
                    tempFiles.clear();
                }
            } catch (Exception e) {
                controller.Logger.getLogger().addEntry(e);
            }
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

    private void listFilesFolder(final File folder, final boolean recursively) {
        tempFiles.clear();
        Runnable r = new Runnable() {

            @Override
            public void run() {
                loading = true;
                errorList.clear();
                iterateFolder(folder, recursively);
                loading = false;
                if (null != loadingDialog) {
                    if (!loadingDialog.isCanceled()) {
                        hideLoadingDialog();
                    }
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    private final ArrayList<String> errorList = new ArrayList<>();

    private void iterateFolder(final File folder, final boolean recursively) {
        if (!loading) {
            return;
        }
        if (null != folder.listFiles()) {
            for (final File fileEntry : folder.listFiles()) {
                if (loading) {
                    if (fileEntry.isDirectory()) {
                        if (recursively) {
                            iterateFolder(fileEntry, true);
                        }
                    } else {
                        if (fileEntry.getAbsolutePath().endsWith(".pdf")) {
                            if (testPdf(fileEntry)) {
                                if (!files.contains(fileEntry)) {
                                    tempFiles.add(new File(fileEntry.getAbsolutePath()));
                                    loadingDialog.incrementFileCounter();
                                }
                            } else {
                                errorList.add(fileEntry.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
    }

    private final ArrayList<File> files = new ArrayList<>();
    private final ArrayList<File> tempFiles = new ArrayList<>();
    private DefaultMutableTreeNode dmtn;

    public ArrayList<File> getOpenedFiles() {
        return files;
    }

    private void refreshOpenedDocumentsList() {
        int tempCount = dmtn.getChildCount();
        lblOpenedDocuments.setText("0 Documentos em lote");
        dmtn = new DefaultMutableTreeNode(null);
        int num = 0;
        if (!files.isEmpty()) {
            File last = null;
            for (File file : files) {
                dmtn.insert(new DefaultMutableTreeNode(file), 0);
                num++;
                if (files.size() == num) {
                    last = file;
                }
            }

            if (null == openedFile) {
                ctrl.openDocument(last.getAbsolutePath());
                workspacePanel.setDocument(ctrl.getDocument());
                openedFile = last;

                int opened = dmtn.getChildCount();
                if (opened == 1) {
                    lblOpenedDocuments.setText("1 Documento em lote:");
                    dmtn.setUserObject(null);
                } else {
                    lblOpenedDocuments.setText(opened + " Documentos em lote:");
                    dmtn.setUserObject(null);
                }

                TreeModel tm = new DefaultTreeModel(dmtn);
                jtOpenedDocuments.setModel(tm);
                jtOpenedDocuments.setSelectionRow(0);
                jtOpenedDocuments.scrollRowToVisible(0);
            } else {
                if (tempCount != dmtn.getChildCount()) {
                    String msg = "Deseja abrir o primeiro dos documentos carregados ou apenas adicioná-los ao lote e manter o que está actualmente aberto?";
                    Object[] options = {"Sim", "Apenas adicionar ao lote"};
                    int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (opt == JOptionPane.YES_OPTION) {
                        ctrl.openDocument(last.getAbsolutePath());
                        workspacePanel.setDocument(ctrl.getDocument());
                        openedFile = last;
                        int opened = dmtn.getChildCount();
                        if (opened == 1) {
                            lblOpenedDocuments.setText("1 Documento em lote:");
                            dmtn.setUserObject(null);
                        } else {
                            lblOpenedDocuments.setText(opened + " Documentos em lote:");
                            dmtn.setUserObject(null);
                        }
                        TreeModel tm = new DefaultTreeModel(dmtn);
                        jtOpenedDocuments.setModel(tm);
                        jtOpenedDocuments.setSelectionRow(0);
                        jtOpenedDocuments.scrollRowToVisible(0);
                    } else {
                        int opened = dmtn.getChildCount();
                        if (opened == 1) {
                            lblOpenedDocuments.setText("1 Documento em lote:");
                            dmtn.setUserObject(null);
                        } else {
                            lblOpenedDocuments.setText(opened + " Documentos em lote:");
                            dmtn.setUserObject(null);
                        }
                        TreeModel tm = new DefaultTreeModel(dmtn);
                        jtOpenedDocuments.setModel(tm);
                        jtOpenedDocuments.setSelectionRow(num);
                        jtOpenedDocuments.scrollRowToVisible(num);
                    }
                }
            }

        }
    }

    public void setLoading(boolean b) {
        loading = b;
    }

    private void showErrors() {
        if (!errorList.isEmpty()) {
            if (1 == errorList.size()) {
                JOptionPane.showMessageDialog(this, "O seguinte ficheiro não foi carregado pois não é suportado ou está corrompido:\n" + errorList.get(0));
            } else {
                String errors = "";
                for (String error : errorList) {
                    errors += error + "\n";
                }
                JOptionPane.showMessageDialog(this, "Os seguintes ficheiros não foram carregados pois não são suportados ou estão corrompidos:\n" + errors);
            }
            errorList.clear();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.isControlDown()) {
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    closeDocument(true);
                } else if (e.getKeyCode() == KeyEvent.VK_ADD) {
                    workspacePanel.zoomIn();
                } else if (e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
                    workspacePanel.zoomOut();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    workspacePanel.pageUp();
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_LEFT) {
                    workspacePanel.pageDown();
                }
                if (e.isShiftDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_S) {
                        workspacePanel.changeCard(WorkspacePanel.CardEnum.SIGN_PANEL, true);
                    } else if (e.getKeyCode() == KeyEvent.VK_V) {
                        workspacePanel.changeCard(WorkspacePanel.CardEnum.VALIDATE_PANEL, true);
                    }
                }
            }
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
        } else if (e.getID() == KeyEvent.KEY_TYPED) {
        }
        return false;
    }
}
