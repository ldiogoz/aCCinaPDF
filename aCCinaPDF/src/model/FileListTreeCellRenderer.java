/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import view.MainWindow;

/**
 *
 * @author Toshiba
 */
public class FileListTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final Icon icon = new ImageIcon(FileListTreeCellRenderer.class.getResource("/image/pdf_ico.jpg"));
    private MainWindow mainWindow;
    private final JTree tree;

    public FileListTreeCellRenderer(JTree tree) {
        this.tree = tree;
    }

    public FileListTreeCellRenderer(MainWindow mainWindow, JTree tree) {
        this.mainWindow = mainWindow;
        this.tree = tree;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ValidationFileListEntry) {
            setIcon(icon);
            ValidationFileListEntry vfle = (ValidationFileListEntry) (node.getUserObject());
            if (vfle.getValidationStatus().equals(ValidationFileListEntry.ValidationStatus.ALL_OK)) {
                if (!sel) {
                    setForeground(new Color(0, 170, 20));
                }
            } else if (vfle.getValidationStatus().equals(ValidationFileListEntry.ValidationStatus.CERTIFIED)) {
                if (!sel) {
                    setForeground(Color.BLUE);
                }
            } else if (vfle.getValidationStatus().equals(ValidationFileListEntry.ValidationStatus.INVALID)) {
                if (!sel) {
                    setForeground(Color.RED);
                }
            } else if (vfle.getValidationStatus().equals(ValidationFileListEntry.ValidationStatus.WARNING)) {
                if (!sel) {
                    setForeground(Color.YELLOW);
                }
            } else {
                setForeground(Color.BLACK);
            }
        } else if (node.getUserObject() instanceof File) {
            File openedFile = mainWindow.getOpenedFile();
            if (openedFile != null) {
                setIcon(icon);
                File file = (File) (node.getUserObject());
                if (file.equals(openedFile)) {
                    setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
                } else {
                    setFont(new Font(Font.SANS_SERIF, 0, 15));
                }
            }
        }

        this.setToolTipText(getText());

        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return tree.getSize();
    }
}
