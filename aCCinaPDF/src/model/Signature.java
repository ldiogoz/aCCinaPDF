/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controller.SignatureHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import org.icepdf.core.pobjects.Document;

/**
 *
 * @author Diogo
 */
public final class Signature extends JPanel {

    private final JLabel lblImage;
    private final JLabel lblSize;

    private boolean dragging = false;
    private final boolean focused = true;
    private boolean placed = false;
    private SignatureHandler cr;
    private final ImagePanel parent;
    private final Document document;
    private final int pageNumber;

    public Document getDocument() {
        return document;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    int locations[] = {
        SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.WEST,
        SwingConstants.EAST, SwingConstants.NORTH_WEST,
        SwingConstants.NORTH_EAST, SwingConstants.SOUTH_WEST,
        SwingConstants.SOUTH_EAST
    };

    @Override
    public ImagePanel getParent() {
        return parent;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public Signature(Document document, int pageNumber, ImagePanel parent, Dimension size) {
        super();
        this.document = document;
        this.pageNumber = pageNumber;
        this.parent = parent;

        setSize(size);
        cr = new SignatureHandler();
        cr.setSnapSize(new Dimension(1, 1));
        cr.registerSignature((Signature) this);
        setBackground(Color.LIGHT_GRAY);
        //setBackground(new Color(110, 110, 110, 150));

        lblImage = new JLabel();
        lblSize = new JLabel();
        add(lblImage);
        add(lblSize);
        lblImage.setLocation(0, 0);
        lblImage.setSize(this.getSize());
        lblImage.setLocation(0, 0);
        lblImage.setSize(this.getSize());
    }

    public void showSize(boolean b) {
        lblSize.setText("[" + getWidth() + ", " + getHeight() + "]");
        lblSize.setVisible(b);
    }

    private String imageLocation;

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
        refreshImage();
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public Image getImage() {
        try {
            if (imageLocation != null) {
                return ImageIO.read(new File(imageLocation));
            }
        } catch (IOException ex) {
        }
        return null;
    }

    private void refreshImage() {
        if (null != getImage()) {
            setBackground(new Color(255, 255, 255, 255));
            lblImage.setIcon(new ImageIcon(getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST)));
            lblImage.setLocation(0, 0);
            setBorder(new LineBorder(Color.BLACK, 1));
        } else {
            setBackground(Color.LIGHT_GRAY);
            lblImage.setIcon(null);
        }
        repaint();
    }

    public Point getPositionOnDocument() {
        Point thisPoint = this.getLocation();
        return parent.toImageContext(thisPoint);
    }

    public Point getScaledPositionOnDocument() {
        int x = (int) getPositionOnDocument().getX();
        int y = (int) getPositionOnDocument().getY();
        return new Point(Math.round(x / parent.getScale()), Math.round(y / parent.getScale()));
    }

    public Dimension getScaledSizeOnDocument() {
        int w = getWidth();
        int h = getHeight();
        return new Dimension(Math.round(w / parent.getScale()), Math.round(h / parent.getScale()));
    }

    public boolean isInsideDocument() {
        return (getPositionOnDocument().getX() >= 0
                && getPositionOnDocument().getY() >= 0
                && getPositionOnDocument().getX() + getWidth() <= getDocument().getPageDimension(getPageNumber(), 0, getParent().getScale()).getWidth()
                && getPositionOnDocument().getY() + getHeight() <= getDocument().getPageDimension(getPageNumber(), 0, getParent().getScale()).getHeight());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        refreshImage();
    }

    public void destroy() {
        cr.deregisterComponent((Signature) this);
        cr = null;
        this.setVisible(false);
        repaint();
    }

    public void place() {
        placed = true;
        setBorder(null);
        cr.deregisterSignature(this);
    }
}
