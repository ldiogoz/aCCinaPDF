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
package model;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Toshiba
 */
public class ValidationTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final Icon iconValid = new ImageIcon(ValidationTreeCellRenderer.class.getResource("/image/success.png"));
    private static final Icon iconValidWarning = new ImageIcon(ValidationTreeCellRenderer.class.getResource("/image/valid_warning.png"));
    private static final Icon iconCertifiedWarning = new ImageIcon(ValidationTreeCellRenderer.class.getResource("/image/certified_warning.png"));
    private static final Icon iconInvalid = new ImageIcon(ValidationTreeCellRenderer.class.getResource("/image/fail.png"));
    private static final Icon iconCertified = new ImageIcon(ValidationTreeCellRenderer.class.getResource("/image/certified.png"));
    private static final Icon iconWarning = new ImageIcon(ValidationTreeCellRenderer.class.getResource("/image/warning.png"));

    public ValidationTreeCellRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (!leaf) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() instanceof SignatureValidation) {
                SignatureValidation sv = (SignatureValidation) (node.getUserObject());

                if (sv.isCertification()) {
                    if (sv.isValid()) {
                        if (sv.isChanged() || !sv.isCoversEntireDocument()) {
                            setIcon(iconCertifiedWarning);
                        } else {
                            setIcon(iconCertified);
                        }
                        setText(getText() + " - Certificado por " + sv.getSignerName());
                        if (!sel) {
                            setForeground(Color.BLUE);
                        }
                    } else {
                        setIcon(iconInvalid);
                        setText(getText() + " - Certificação inválida por " + sv.getSignerName());
                        if (!sel) {
                            setForeground(Color.RED);
                        }
                    }
                } else {
                    if (sv.isValid()) {
                        if (sv.isChanged() || !sv.isCoversEntireDocument()) {
                            setIcon(iconValidWarning);
                        } else {
                            setIcon(iconValid);
                        }
                        if (!sel) {
                            setForeground(new Color(0, 170, 20));
                        }
                        setText(getText() + " - Assinado por " + sv.getSignerName());
                    } else {
                        setIcon(iconInvalid);
                        if (!sel) {
                            setForeground(Color.RED);
                        }
                        setText(getText() + " - Assinatura inválida por " + sv.getSignerName());
                    }
                }
            }
        } else {
            if (value instanceof TreeNodeWithState) {
                TreeNodeWithState node = (TreeNodeWithState) value;
                switch (node.getState()) {
                    case INVALID:
                        setIcon(iconInvalid);
                        break;
                    case WARNING:
                        setIcon(iconWarning);
                        break;
                    case VALID_WARNING:
                        setIcon(iconValidWarning);
                        break;
                    case CERTIFIED_WARNING:
                        setIcon(iconCertifiedWarning);
                        break;
                    case VALID:
                        setIcon(iconValid);
                        break;
                    case CERTIFIED:
                        setIcon(iconCertified);
                        break;
                    case NOT_SIGNED:
                        setIcon(null);
                        break;
                }
            }
        }
        this.setToolTipText(getText());
        return this;
    }
}
