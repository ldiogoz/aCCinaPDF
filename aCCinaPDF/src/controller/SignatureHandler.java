/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.Signature;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

public final class SignatureHandler extends MouseAdapter {

    private final Map<Integer, Integer> cursors = new HashMap<>();
    private Insets dragInsets;
    private Dimension snapSize;
    private int direction;
    private final int NORTH = 1, WEST = 2, SOUTH = 4, EAST = 8;
    private boolean resizing;
    private Rectangle bounds;
    private Point pressed;
    private boolean autoscrolls;
    private Dimension minimumSize = new Dimension(10, 10);
    private Dimension maximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    public SignatureHandler() {
        cursors.put(1, Cursor.N_RESIZE_CURSOR);
        cursors.put(2, Cursor.W_RESIZE_CURSOR);
        cursors.put(4, Cursor.S_RESIZE_CURSOR);
        cursors.put(8, Cursor.E_RESIZE_CURSOR);
        cursors.put(3, Cursor.NW_RESIZE_CURSOR);
        cursors.put(9, Cursor.NE_RESIZE_CURSOR);
        cursors.put(6, Cursor.SW_RESIZE_CURSOR);
        cursors.put(12, Cursor.SE_RESIZE_CURSOR);

        setDragInsets(new Insets(3, 3, 3, 3));
        setSnapSize(new Dimension(1, 1));
    }

    public Insets getDragInsets() {
        return dragInsets;
    }

    public void setDragInsets(Insets dragInsets) {
        validateMinimumAndInsets(minimumSize, dragInsets);

        this.dragInsets = dragInsets;
    }

    public Dimension getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(Dimension maximumSize) {
        this.maximumSize = maximumSize;
    }

    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public void setMinimumSize(Dimension minimumSize) {
        validateMinimumAndInsets(minimumSize, dragInsets);

        this.minimumSize = minimumSize;
    }

    private Signature s;

    public void registerSignature(Signature s) {
        this.s = s;
        s.addMouseListener(this);
        s.addMouseMotionListener(this);
    }

    public void deregisterSignature(Signature s) {
        s.removeMouseListener(this);
        s.removeMouseMotionListener(this);
    }

    public Dimension getSnapSize() {
        return snapSize;
    }

    public void setSnapSize(Dimension snapSize) {
        this.snapSize = snapSize;
    }

    private void validateMinimumAndInsets(Dimension minimum, Insets drag) {
        int minimumWidth = drag.left + drag.right;
        int minimumHeight = drag.top + drag.bottom;

        if (minimum.width < minimumWidth || minimum.height < minimumHeight) {
            String message = "Minimum size cannot be less than drag insets";
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Component source = e.getComponent();
        Point location = e.getPoint();
        direction = 0;

        if (location.x < dragInsets.left) {
            direction += WEST;
        }

        if (location.x > source.getWidth() - dragInsets.right - 1) {
            direction += EAST;
        }

        if (location.y < dragInsets.top) {
            direction += NORTH;
        }

        if (location.y > source.getHeight() - dragInsets.bottom - 1) {
            direction += SOUTH;
        }

        if (direction == 0) {
            source.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            int cursorType = cursors.get(direction);
            Cursor cursor = Cursor.getPredefinedCursor(cursorType);
            source.setCursor(cursor);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (!resizing) {
            if (null == s.getImageLocation()) {
                Component source = e.getComponent();
                ((JPanel) source).setBorder(new LineBorder(Color.BLACK, 1));
            }
        }
        s.showSize(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (!resizing) {
            if (null == s.getImageLocation()) {
                Component source = e.getComponent();
                ((JPanel) source).setBorder(null);
            }
        }
        s.showSize(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.me = e;

        if (direction == 0) {
            return;
        }

        resizing = true;
        Component source = e.getComponent();
        pressed = e.getPoint();
        SwingUtilities.convertPointToScreen(pressed, source);
        bounds = source.getBounds();

        if (source instanceof JComponent) {
            JComponent jc = (JComponent) source;
            autoscrolls = jc.getAutoscrolls();
            jc.setAutoscrolls(false);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        resizing = false;

        Component source = e.getComponent();
        source.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        if (source instanceof JComponent) {
            ((JComponent) source).setAutoscrolls(autoscrolls);
        }
    }

    private MouseEvent me;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (resizing == false) {
            Point p = s.getLocation();
            int x = p.x - this.me.getX() + e.getX();
            int y = p.y - this.me.getY() + e.getY();

            Point panelPoint = new Point(x, y);
            Point realPoint = s.getParent().toImageContext(panelPoint);

            int newX;
            int newY;

            if (realPoint.getX() < 0) {
                newX = (int) s.getParent().getImageLocation().getX();
            } else {
                int maxX = (int) s.getDocument().getPageDimension(s.getPageNumber(), 0, s.getParent().getScale()).getWidth() - s.getWidth();
                int realX = (int) (realPoint.getX());
                if (realX > maxX) {
                    newX = (int) (maxX + s.getParent().getImageLocation().getX());
                } else {
                    newX = x;
                }
            }
            if (realPoint.getY() < 0) {
                newY = (int) s.getParent().getImageLocation().getY();
            } else {
                int maxY = (int) s.getDocument().getPageDimension(s.getPageNumber(), 0, s.getParent().getScale()).getHeight() - s.getHeight();
                int realY = (int) (realPoint.getY());
                if (realY > maxY) {
                    newY = (int) (maxY + s.getParent().getImageLocation().getY());
                } else {
                    newY = y;
                }
            }
            s.setLocation(newX, newY);

            return;
        }

        Component source = e.getComponent();
        Point dragged = e.getPoint();
        SwingUtilities.convertPointToScreen(dragged, source);
        changeBounds(source, direction, bounds, pressed, dragged);
        s.showSize(true);
    }

    protected void changeBounds(Component source, int direction, Rectangle bounds, Point pressed, Point current) {
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        if (WEST == (direction & WEST)) {
            int drag = getDragDistance(pressed.x, current.x, snapSize.width);
            int maximum = Math.min(width + x, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, minimumSize.width, maximum);

            x -= drag;
            width += drag;
        }

        if (NORTH == (direction & NORTH)) {
            int drag = getDragDistance(pressed.y, current.y, snapSize.height);
            int maximum = Math.min(height + y, maximumSize.height);
            drag = getDragBounded(drag, snapSize.height, height, minimumSize.height, maximum);

            y -= drag;
            height += drag;
        }

        if (EAST == (direction & EAST)) {
            int drag = getDragDistance(current.x, pressed.x, snapSize.width);
            Dimension boundingSize = getBoundingSize(source);
            int maximum = Math.min(boundingSize.width - x, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, minimumSize.width, maximum);
            width += drag;
        }

        if (SOUTH == (direction & SOUTH)) {
            int drag = getDragDistance(current.y, pressed.y, snapSize.height);
            Dimension boundingSize = getBoundingSize(source);
            int maximum = Math.min(boundingSize.height - y, maximumSize.height);
            drag = getDragBounded(drag, snapSize.height, height, minimumSize.height, maximum);
            height += drag;
        }

        Point panelPoint = new Point(x, y);
        Point realPoint = s.getParent().toImageContext(panelPoint);
        if (realPoint.getX() >= 0 && realPoint.getY() >= 0 && realPoint.getX() + width <= s.getDocument().getPageDimension(s.getPageNumber(), 0, s.getParent().getScale()).getWidth() && realPoint.getY() + height <= s.getDocument().getPageDimension(s.getPageNumber(), 0, s.getParent().getScale()).getHeight()) {
            source.setBounds(x, y, width, height);
            source.validate();
        }

        if (s.getImageLocation() != null) {
            s.refreshImage();
        }
    }

    private int getDragDistance(int larger, int smaller, int snapSize) {
        int halfway = snapSize / 2;
        int drag = larger - smaller;
        drag += (drag < 0) ? -halfway : halfway;
        drag = (drag / snapSize) * snapSize;

        return drag;
    }

    private int getDragBounded(int drag, int snapSize, int dimension, int minimum, int maximum) {
        while (dimension + drag < minimum) {
            drag += snapSize;
        }

        while (dimension + drag > maximum) {
            drag -= snapSize;
        }

        return drag;
    }

    private Dimension getBoundingSize(Component source) {
        if (source instanceof Window) {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle r = env.getMaximumWindowBounds();
            return new Dimension(r.width, r.height);
        } else {
            return source.getParent().getSize();
        }
    }
}
