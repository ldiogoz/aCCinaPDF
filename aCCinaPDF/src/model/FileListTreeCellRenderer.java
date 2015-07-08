/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.Map;
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

    public FileListTreeCellRenderer() {
    }

    public FileListTreeCellRenderer(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
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
                Font font = getFont();
                Map attributes = font.getAttributes();
                if (file.equals(openedFile)) {
                    setForeground(Color.BLACK);
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    setFont(font.deriveFont(attributes));
                } else {
                    setForeground(Color.GRAY);
                    attributes.put(TextAttribute.UNDERLINE, false);
                    setFont(font.deriveFont(attributes));
                }
                tree.repaint();
            }
        }

        this.setToolTipText(getText());

        return this;
    }
}
