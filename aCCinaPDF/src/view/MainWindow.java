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

import com.itextpdf.text.pdf.PdfReader;
import controller.Bundle;
import controller.CCInstance;
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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import model.FileListTreeCellRenderer;
import model.FileSystemModel;
import model.Settings;
import model.TooltipTreeCellRenderer;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;
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

        updateText();

        List<Image> icons = new ArrayList<>();
        icons.add(new ImageIcon(getClass().getResource("/image/aCCinaPDF_logo_icon32.png")).getImage());
        this.setIconImages(icons);

        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        workspacePanel.setParent((MainWindow) this);
        populateDriveComboBox();

        dmtn = new DefaultMutableTreeNode("0 " + Bundle.getBundle().getString("tn.documentsLoaded"));
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

        ctrl.setPrintMenuItem(menuItemPrint);
    }

    private void updateText() {
        lblOpenedDocuments.setText("0 " + Bundle.getBundle().getString("tn.documentsLoaded"));
        menuFile.setText(Bundle.getBundle().getString("menu.file"));
        menuOptions.setText(Bundle.getBundle().getString("menu.options"));
        menuHelp.setText(Bundle.getBundle().getString("menu.help"));
        menuItemOpen.setText(Bundle.getBundle().getString("menuItem.open"));
        menuItemPrint.setText(Bundle.getBundle().getString("menuItem.print"));
        menuItemExit.setText(Bundle.getBundle().getString("menuItem.exit"));
        menuItemCertificateManagement.setText(Bundle.getBundle().getString("menuItem.certificateManagement"));
        menuItemPreferences.setText(Bundle.getBundle().getString("menuItem.preferences"));
        menuItemViewLog.setText(Bundle.getBundle().getString("menuItem.viewLog"));
        menuItemShortcuts.setText(Bundle.getBundle().getString("menuItem.shortcuts"));
        menuItemAbout.setText(Bundle.getBundle().getString("menuItem.about"));
        btnRefreshTree.setText(Bundle.getBundle().getString("btn.refresh"));
        lblLookFor.setText(Bundle.getBundle().getString("label.lookFor"));
    }

    public WorkspacePanel getWorkspacePanel() {
        return workspacePanel;
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

                        ActionListener removeOthers = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                final ArrayList<File> alFilesToClose = new ArrayList<>();
                                boolean showDialog = false;
                                for (File file : getOpenedFiles()) {
                                    if (!getSelectedOpenedFiles().contains(file)) {
                                        alFilesToClose.add(file);
                                        if (file.equals(openedFile)) {
                                            showDialog = true;
                                        }
                                    }
                                }
                                closeDocuments(alFilesToClose, showDialog);
                            }
                        };

                        ActionListener removeAll = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                closeDocuments(getOpenedFiles(), true);
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
                            m = new JMenuItem(Bundle.getBundle().getString("menuItem.open"));
                            m.addActionListener(open);
                            popup.add(m);
                        }
                        m = new JMenuItem(getSelectedOpenedFiles().size() > 1 ? Bundle.getBundle().getString("menuItem.removeTheseLoadedDocuments") : Bundle.getBundle().getString("menuItem.removeThisLoadedDocument"));
                        m.addActionListener(remove);
                        popup.add(m);
                        if (getOpenedFiles().size() > 1) {
                            m = new JMenuItem(Bundle.getBundle().getString("menuItem.removeOtherLoadedDocuments"));
                            m.addActionListener(removeOthers);
                            popup.add(m);
                            m = new JMenuItem(Bundle.getBundle().getString("menuItem.removeAllLoadedDocuments"));
                            m.addActionListener(removeAll);
                            popup.add(m);
                        }
                        if (getSelectedOpenedFiles().size() == 1) {
                            m = new JMenuItem(Bundle.getBundle().getString("menuItem.showInExplorer"));
                            m.addActionListener(show);
                            popup.add(m);
                        }

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
                                    m = new JMenuItem(Bundle.getBundle().getString("menuItem.openDocumentsInThisFolder"));
                                    m.addActionListener(loadFolder);
                                    popup.add(m);
                                    m = new JMenuItem(Bundle.getBundle().getString("menuItem.openDocumentsThisFolderRecursive"));
                                    m.addActionListener(loadFolderRecursively);
                                    popup.add(m);
                                    m = new JMenuItem(Bundle.getBundle().getString("menuItem.showInExplorer"));
                                    m.addActionListener(show);
                                    popup.add(m);
                                } else {
                                    m = new JMenuItem(Bundle.getBundle().getString("menuItem.openDocument"));
                                    m.addActionListener(loadFile);
                                    popup.add(m);
                                    m = new JMenuItem(Bundle.getBundle().getString("menuItem.showInExplorer"));
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
                                        m = new JMenuItem(Bundle.getBundle().getString("menuItem.openDocuments"));
                                        m.addActionListener(loadFile);
                                        popup.add(m);
                                    } else {
                                        m = new JMenuItem(Bundle.getBundle().getString("menuItem.openDocumentsInTheseFolders"));
                                        m.addActionListener(loadFolder);
                                        popup.add(m);
                                    }
                                }
                            }
                        } else {
                            File f = getSelectedFile(jtExplorer);
                            if (f.isDirectory()) {
                                m = new JMenuItem(Bundle.getBundle().getString("menuItem.openDocumentsInThisFolder"));
                                m.addActionListener(loadFolder);
                                popup.add(m);
                                m = new JMenuItem(Bundle.getBundle().getString("menuItem.openDocumentsThisFolderRecursive"));
                                m.addActionListener(loadFolderRecursively);
                                popup.add(m);
                            } else if (files.contains(getSelectedFile(jtExplorer))) {
                                m = new JMenuItem(Bundle.getBundle().getString("menuItem.removeFromLoadedDocuments"));
                                m.addActionListener(remove);
                                popup.add(m);
                            } else {
                                m = new JMenuItem(Bundle.getBundle().getString("menuItem.addToLoadedDocuments"));
                                m.addActionListener(loadFile);
                                popup.add(m);
                            }
                        }
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        jtExplorer.setCellRenderer(new TooltipTreeCellRenderer());
        ToolTipManager.sharedInstance().registerComponent(jtExplorer);

        FileListTreeCellRenderer renderer1 = new FileListTreeCellRenderer((MainWindow) this);
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
                dcbm.addElement(Bundle.getBundle().getString("desktop"));
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
        lblLookFor = new javax.swing.JLabel();
        cbVolume = new view.WideDropDownComboBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtOpenedDocuments = new javax.swing.JTree();
        lblOpenedDocuments = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel();
        workspacePanel = new view.WorkspacePanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuItemOpen = new javax.swing.JMenuItem();
        menuItemPrint = new javax.swing.JMenuItem();
        menuItemExit = new javax.swing.JMenuItem();
        menuOptions = new javax.swing.JMenu();
        menuItemCertificateManagement = new javax.swing.JMenuItem();
        menuItemPreferences = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        menuItemViewLog = new javax.swing.JMenuItem();
        menuItemShortcuts = new javax.swing.JMenuItem();
        menuItemAbout = new javax.swing.JMenuItem();

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
        btnRefreshTree.setToolTipText("Actualiza os ficheiros no explorador. Útil quando são criados, eliminados ou movidos ficheiros");
        btnRefreshTree.setMinimumSize(new java.awt.Dimension(0, 25));
        btnRefreshTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshTreeActionPerformed(evt);
            }
        });

        jtExplorer.setFont(new java.awt.Font("SansSerif", 0, 15)); // NOI18N
        jtExplorer.setToolTipText("");
        jtExplorer.setRowHeight(20);
        jtExplorer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jtExplorerMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jtExplorer);

        tfProcurar.setToolTipText("Pesquisar por pastas ou documentos por nome");
        tfProcurar.setMinimumSize(new java.awt.Dimension(0, 22));

        lblLookFor.setText("Procurar por:");
        lblLookFor.setToolTipText("Pesquisar por pastas ou documentos por nome");
        lblLookFor.setMinimumSize(new java.awt.Dimension(0, 16));

        cbVolume.setToolTipText("");
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
                        .addComponent(lblLookFor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(lblLookFor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setBottomComponent(jPanel2);

        jtOpenedDocuments.setFont(new java.awt.Font("SansSerif", 0, 15)); // NOI18N
        jtOpenedDocuments.setModel(null);
        jtOpenedDocuments.setToolTipText("");
        jtOpenedDocuments.setRootVisible(false);
        jtOpenedDocuments.setRowHeight(20);
        jtOpenedDocuments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jtOpenedDocumentsMousePressed(evt);
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
        lblOpenedDocuments.setToolTipText("");

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

        menuFile.setText("Ficheiro");

        menuItemOpen.setText("Abrir");
        menuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOpenActionPerformed(evt);
            }
        });
        menuFile.add(menuItemOpen);

        menuItemPrint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        menuItemPrint.setText("Imprimir");
        menuFile.add(menuItemPrint);

        menuItemExit.setText("Sair");
        menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExitActionPerformed(evt);
            }
        });
        menuFile.add(menuItemExit);

        jMenuBar1.add(menuFile);

        menuOptions.setText("Opções");

        menuItemCertificateManagement.setText("Gestão de Certificados");
        menuItemCertificateManagement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemCertificateManagementActionPerformed(evt);
            }
        });
        menuOptions.add(menuItemCertificateManagement);

        menuItemPreferences.setText("Preferências");
        menuItemPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemPreferencesActionPerformed(evt);
            }
        });
        menuOptions.add(menuItemPreferences);

        jMenuBar1.add(menuOptions);

        menuHelp.setText("Ajuda");

        menuItemViewLog.setText("Ver Log");
        menuItemViewLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemViewLogActionPerformed(evt);
            }
        });
        menuHelp.add(menuItemViewLog);

        menuItemShortcuts.setText("Teclas de Atalho");
        menuItemShortcuts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemShortcutsActionPerformed(evt);
            }
        });
        menuHelp.add(menuItemShortcuts);

        menuItemAbout.setText("Sobre");
        menuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemAboutActionPerformed(evt);
            }
        });
        menuHelp.add(menuItemAbout);

        jMenuBar1.add(menuHelp);

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
            } else if (volume.equals(Bundle.getBundle().getString("desktop"))) {
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
                    msg = Bundle.getBundle().getString("msg.closeSelectedDocument");
                } else {
                    msg = Bundle.getBundle().getString("msg.closeSelectedDocuments");
                }
                Object[] options = {Bundle.getBundle().getString("yes"), Bundle.getBundle().getString("no")};
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
                    lblOpenedDocuments.setText("1 " + Bundle.getBundle().getString("tn.documentLoaded") + " :");
                    dmtn.setUserObject(null);
                } else {
                    lblOpenedDocuments.setText(numOpened + " " + Bundle.getBundle().getString("tn.documentsLoaded") + ":");
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
        if (workspacePanel.getStatus().equals(WorkspacePanel.Status.SIGNING)) {
            if (listaD.contains(openedFile)) {
                String msg = Bundle.getBundle().getString("msg.cancelSignatureAndClose1") + " " + (listaD.size() == 1 ? Bundle.getBundle().getString("msg.cancelSignatureAndClose2") : Bundle.getBundle().getString("msg.cancelSignatureAndClose3"));
                Object[] options = {Bundle.getBundle().getString("yes"), Bundle.getBundle().getString("no")};
                int opt = JOptionPane.showOptionDialog(this, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (opt != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        }

        if (!listaD.isEmpty()) {
            int opt = -1;
            if (showCloseDialog) {
                String msg;
                if (listaD.size() == 1) {
                    msg = Bundle.getBundle().getString("msg.closeSelectedDocument");
                } else {
                    msg = Bundle.getBundle().getString("msg.closeSelectedDocuments");
                }

                Object[] options = {Bundle.getBundle().getString("yes"), Bundle.getBundle().getString("no")};
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
                    lblOpenedDocuments.setText("1 " + Bundle.getBundle().getString("tn.documentLoaded") + ":");
                    dmtn.setUserObject(null);
                } else {
                    lblOpenedDocuments.setText(numOpened + " " + Bundle.getBundle().getString("tn.documentsLoaded") + ":");
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

    private void menuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_menuItemExitActionPerformed

    private void menuItemViewLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemViewLogActionPerformed
        LogWindow lw = new LogWindow();
        lw.setLocationRelativeTo(null);
        lw.setVisible(true);
    }//GEN-LAST:event_menuItemViewLogActionPerformed

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

    private void menuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemAboutActionPerformed
        AboutDialog ad = new AboutDialog(this, true);
        ad.setLocationRelativeTo(null);
        ad.setVisible(true);
    }//GEN-LAST:event_menuItemAboutActionPerformed

    private void menuItemPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemPreferencesActionPerformed
        Locale tempLocale = Bundle.getBundle().getCurrentLocale();
        SettingsDialog sd = new SettingsDialog(this, true);
        sd.setLocationRelativeTo(null);
        sd.setVisible(true);
        if (!Bundle.getBundle().getCurrentLocale().equals(tempLocale)) {
            updateText();
            workspacePanel.updateText();
        }
    }//GEN-LAST:event_menuItemPreferencesActionPerformed

    private File lastOpenedFilePath;

    private void menuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOpenActionPerformed
        JFileChooser jfc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(Bundle.getBundle().getString("filter.pdfDocuments") + " (*.pdf)", "pdf");
        File path;
        if (lastOpenedFilePath == null) {
            path = new File(System.getProperty("user.home"));
        } else {
            path = lastOpenedFilePath;
        }
        jfc.setCurrentDirectory(path);
        jfc.setFileFilter(filter);
        int ret = jfc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            try {
                if (loadPdf(file, true)) {
                    lastOpenedFilePath = file;
                }
            } catch (Exception e) {
                controller.Logger.getLogger().addEntry(e);
            }
        }
    }//GEN-LAST:event_menuItemOpenActionPerformed

    private void menuItemShortcutsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemShortcutsActionPerformed
        JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("help.shortcuts"), Bundle.getBundle().getString("shortcuts"), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_menuItemShortcutsActionPerformed

    private void menuItemCertificateManagementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemCertificateManagementActionPerformed
        CertificateManagementDialog cmd = new CertificateManagementDialog(this, true);
        cmd.setLocationRelativeTo(null);
        cmd.setVisible(true);
    }//GEN-LAST:event_menuItemCertificateManagementActionPerformed

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
        if (jtree == null) {
            return aLSelectedFiles;
        }
        if (jtree.getSelectionPath() == null) {
            return aLSelectedFiles;
        }
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
    private view.WideDropDownComboBox cbVolume;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTree jtExplorer;
    private javax.swing.JTree jtOpenedDocuments;
    private javax.swing.JLabel lblLookFor;
    private javax.swing.JLabel lblOpenedDocuments;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem menuItemAbout;
    private javax.swing.JMenuItem menuItemCertificateManagement;
    private javax.swing.JMenuItem menuItemExit;
    private javax.swing.JMenuItem menuItemOpen;
    private javax.swing.JMenuItem menuItemPreferences;
    private javax.swing.JMenuItem menuItemPrint;
    private javax.swing.JMenuItem menuItemShortcuts;
    private javax.swing.JMenuItem menuItemViewLog;
    private javax.swing.JMenu menuOptions;
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
                        lblOpenedDocuments.setText("0 " + Bundle.getBundle().getString("tn.documentsLoaded"));
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
                                lblOpenedDocuments.setText("1 " + Bundle.getBundle().getString("tn.documentLoaded") + ":");
                                dmtn.setUserObject(null);
                            } else {
                                lblOpenedDocuments.setText(numOpened + " " + Bundle.getBundle().getString("tn.documentsLoaded") + ":");
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
                                    String msg = Bundle.getBundle().getString("msg.openNewOrKeepCurrent");
                                    Object[] options = {Bundle.getBundle().getString("yes"), Bundle.getBundle().getString("opt.justAdd")};
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
                    } else if (fileEntry.getAbsolutePath().endsWith(".pdf")) {
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

    private final ArrayList<File> files = new ArrayList<>();
    private final ArrayList<File> tempFiles = new ArrayList<>();
    private DefaultMutableTreeNode dmtn;

    public ArrayList<File> getOpenedFiles() {
        return files;
    }

    private void refreshOpenedDocumentsList() {
        int tempCount = dmtn.getChildCount();
        lblOpenedDocuments.setText("0 " + Bundle.getBundle().getString("tn.documentsLoaded"));
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
                    lblOpenedDocuments.setText("1 " + Bundle.getBundle().getString("tn.documentLoaded") + ":");
                    dmtn.setUserObject(null);
                } else {
                    lblOpenedDocuments.setText(opened + " " + Bundle.getBundle().getString("tn.documentsLoaded") + ":");
                    dmtn.setUserObject(null);
                }

                TreeModel tm = new DefaultTreeModel(dmtn);
                jtOpenedDocuments.setModel(tm);
                jtOpenedDocuments.setSelectionRow(0);
                jtOpenedDocuments.scrollRowToVisible(0);
            } else if (tempCount != dmtn.getChildCount()) {
                String msg = Bundle.getBundle().getString("msg.openNewOrKeepCurrent2");
                Object[] options = {Bundle.getBundle().getString("yes"), Bundle.getBundle().getString("opt.justAdd")};
                int opt = JOptionPane.showOptionDialog(null, msg, "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (opt == JOptionPane.YES_OPTION) {
                    ctrl.openDocument(last.getAbsolutePath());
                    workspacePanel.setDocument(ctrl.getDocument());
                    openedFile = last;
                    int opened = dmtn.getChildCount();
                    if (opened == 1) {
                        lblOpenedDocuments.setText("1 " + Bundle.getBundle().getString("tn.documentLoaded") + ":");
                        dmtn.setUserObject(null);
                    } else {
                        lblOpenedDocuments.setText(opened + " " + Bundle.getBundle().getString("tn.documentsLoaded") + ":");
                        dmtn.setUserObject(null);
                    }
                    TreeModel tm = new DefaultTreeModel(dmtn);
                    jtOpenedDocuments.setModel(tm);
                    jtOpenedDocuments.setSelectionRow(0);
                    jtOpenedDocuments.scrollRowToVisible(0);
                } else {
                    int opened = dmtn.getChildCount();
                    if (opened == 1) {
                        lblOpenedDocuments.setText("1 " + Bundle.getBundle().getString("tn.documentLoaded") + ":");
                        dmtn.setUserObject(null);
                    } else {
                        lblOpenedDocuments.setText(opened + " " + Bundle.getBundle().getString("tn.documentsLoaded") + ":");
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

    public void setLoading(boolean b) {
        loading = b;
    }

    private void showErrors() {
        if (!errorList.isEmpty()) {
            if (1 == errorList.size()) {
                JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("msg.errorFollowingFile") + "\n" + errorList.get(0));
            } else {
                String errors = "";
                for (String error : errorList) {
                    errors += error + "\n";
                }
                JOptionPane.showMessageDialog(this, Bundle.getBundle().getString("msg.errorFollowingFiles") + "\n" + errors);
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
