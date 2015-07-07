/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JComboBox;

/**
 *
 * @author Diogo
 */
public class WideDropDownComboBox extends JComboBox {

    private static final long serialVersionUID = -2694382778237570550L;
    private boolean layingOut = false;
    private int dropDownMenuWidth = 0;

// Setting the JComboBox width
    public void adjustDropDownMenuWidth() {
        dropDownMenuWidth = computeMaxItemWidth();
    }

    @Override
    public Dimension getSize() {
        Dimension dim = super.getSize();
        if (!layingOut) {
            dim.width = Math.max(dropDownMenuWidth, dim.width);
        }
        return dim;
    }

    public int computeMaxItemWidth() {

        int numOfItems = this.getItemCount();
        Font font = this.getFont();
        FontMetrics metrics = this.getFontMetrics(font);
        int widest = getSize().width; // The drop down menu must not be less wide than the combo box
        for (int i = 0; i < numOfItems; i++) {
            Object item = this.getItemAt(i);
            int lineWidth = metrics.stringWidth(item.toString());
            widest = Math.max(widest, lineWidth);
        }

        if (numOfItems > 0) {
            //int scrollbarWidth = ((Integer) UIManager.get("ScrollBar.width"));
            return widest + 10;// + scrollbarWidth;
        } else {
            return 0;
        }

    }

    @Override
    public void doLayout() {
        try {
            layingOut = true;
            super.doLayout();
        } finally {
            layingOut = false;
        }
    }
}
