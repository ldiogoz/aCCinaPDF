/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Toshiba
 */
public class TooltipTreeCellRenderer extends DefaultTreeCellRenderer {

    public TooltipTreeCellRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        final Component rc = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        String tooltip = value.toString();
        this.setToolTipText(tooltip);
        return rc;
    }
}
