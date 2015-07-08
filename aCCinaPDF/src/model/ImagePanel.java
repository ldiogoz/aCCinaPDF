/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.itextpdf.text.pdf.AcroFields;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import view.MainWindow;

public class ImagePanel extends JPanel {

    private MainWindow mainWindow;
    private BufferedImage img;
    private Document document;
    private int pageNumber;
    private float scale;

    private JScrollPane parent;
    private JButton btnPageBackward;
    private JButton btnPageForward;

    public void clear() {
        img = null;
        document = null;
    }

    public enum DocumentPageControl {

        PAGE_UP,
        PAGE_DOWN
    };

    public ImagePanel() {
        super();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (svList != null) {
                    refreshSignatureValidationListPanels();
                }
            }
        });
    }

    private SignatureValidation selectedSignature = null;

    public void setSelectedSignature(final SignatureValidation selectedSignature) {
        this.selectedSignature = selectedSignature;
        refreshSignatureValidationListPanels();
    }

    public void setDocumentAndComponents(MainWindow mainWindow, JScrollPane parent, Document document, JButton btnBackward, JButton btnForward) {
        this.mainWindow = mainWindow;
        this.parent = parent;
        this.document = document;
        this.btnPageBackward = btnBackward;
        this.btnPageForward = btnForward;

        this.pageNumber = 0;
        this.scale = 1f;
        this.svList = null;
        setBorder(null);
        refreshParent();
        JLabel lblDocName = new JLabel(document.getDocumentLocation());
        lblDocName.setLocation(0, 0);
        add(lblDocName);
        lblDocName.setVisible(true);
        refreshTitle();
        setSelectedSignature(null);
    }

    final private ArrayList<JPanel> panelList = new ArrayList<>();

    public synchronized void refreshSignatureValidationListPanels() {
        for (JPanel jp : panelList) {
            remove(jp);
        }

        if (document != null) {
            Point p = getImageLocation();
            if (svList != null) {
                for (final SignatureValidation sv : svList) {
                    int pgNumber = sv.getPosList().get(0).page - 1;
                    if (this.pageNumber == pgNumber) {
                        for (AcroFields.FieldPosition pos : sv.getPosList()) {
                            int p1 = (int) (p.x + (pos.position.getLeft() * scale));
                            int p2 = (int) (p.y + Math.floor((document.getPageDimension(pageNumber, scale).getHeight() - pos.position.getTop() - scale * 10) * scale));
                            int p3 = (int) (pos.position.getWidth() * scale);
                            int p4 = (int) (pos.position.getHeight() * scale);

                            final JPanel jp1 = sv.getPanel();
                            jp1.setLocation(p1, p2);
                            jp1.setSize(p3, p4);

                            if (sv.equals(selectedSignature)) {
                                jp1.setBackground(new Color(0, 0, 0, 45));
                                jp1.setBorder(new LineBorder(Color.BLACK, 1));
                            } else {
                                jp1.setBackground(new Color(0, 0, 0, 0));
                                jp1.setBorder(null);
                            }

                            jp1.setVisible(true);
                            jp1.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseEntered(java.awt.event.MouseEvent evt) {
                                    jp1.setBackground(new Color(0, 0, 0, 45));
                                    jp1.setBorder(new LineBorder(Color.BLACK, 1));
                                    repaint();
                                }

                                @Override
                                public void mouseExited(java.awt.event.MouseEvent evt) {
                                    if (selectedSignature == null) {
                                        jp1.setBackground(new Color(0, 0, 0, 0));
                                        jp1.setBorder(null);
                                        repaint();
                                    } else {
                                        if (!selectedSignature.equals(sv)) {
                                            jp1.setBackground(new Color(0, 0, 0, 0));
                                            jp1.setBorder(null);
                                            repaint();
                                        }
                                    }
                                }
                            });
                            panelList.add(jp1);
                            add(jp1);
                            repaint();
                        }
                    }
                }
            }
        }
    }

    private ArrayList<SignatureValidation> svList;

    public void setSignatureValidationList(ArrayList<SignatureValidation> svList) {
        for (JPanel jp : panelList) {
            remove(jp);
        }
        if (this.svList == null) {
            this.svList = svList;
            panelList.clear();
            refreshSignatureValidationListPanels();
            repaint();
        } else {
            this.svList = svList;
            panelList.clear();
            refreshSignatureValidationListPanels();
            repaint();
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return img == null ? super.getPreferredSize() : new Dimension(img.getWidth(), img.getHeight());
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        img = fitDocument();
    }

    public float scaleUp() {
        if (scale < 3f) {
            scale += 0.2f;
            refreshParent();
        }
        return scale;
    }

    public float scaleDown() {
        if (scale > 0.4f) {
            scale -= 0.2f;
            refreshParent();
        }
        return scale;
    }

    public boolean setPageNumberControl(DocumentPageControl dpc) {
        boolean changed = false;
        if (dpc.equals(DocumentPageControl.PAGE_UP)) {
            if (null != document && pageNumber < (document.getNumberOfPages() - 1)) {
                pageNumber++;
                changed = true;
                refreshSignatureValidationListPanels();
            }
        } else if (dpc.equals(DocumentPageControl.PAGE_DOWN)) {
            if (null != document && pageNumber > 0) {
                pageNumber--;
                changed = true;
                refreshSignatureValidationListPanels();
            }
        }

        if (0 == pageNumber) {
            btnPageBackward.setEnabled(false);
            if (document.getNumberOfPages() > 1) {
                btnPageForward.setEnabled(true);
            }
        } else if (document.getNumberOfPages() == (pageNumber + 1)) {
            btnPageBackward.setEnabled(true);
            btnPageForward.setEnabled(false);
        } else {
            btnPageBackward.setEnabled(true);
            btnPageForward.setEnabled(true);
        }
        refreshParent();
        refreshTitle();
        return changed;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        refreshParent();
        refreshTitle();
        refreshSignatureValidationListPanels();
    }

    private void refreshParent() {
        img = fitDocument();
        repaint();
        parent.setViewportView(this);
    }

    private void refreshTitle() {
        mainWindow.setTitle("aCCinaPDF - " + document.getDocumentLocation() + " - Página " + (pageNumber + 1) + " de " + document.getNumberOfPages());
    }

    public Point getImageLocation() {

        Point p = null;
        if (img != null) {
            int x = (getWidth() - img.getWidth()) / 2;
            int y = (getHeight() - img.getHeight()) / 2;
            p = new Point(x, y);
        }
        return p;
    }

    public Point toImageContext(Point p) {
        Point imgLocation = getImageLocation();
        Point relative = new Point(p);
        relative.x -= imgLocation.x;
        relative.y -= imgLocation.y;
        return relative;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            Point p = getImageLocation();
            g.drawImage(img, p.x, p.y, this);
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(p.x, p.y, img.getWidth(), img.getHeight());
        }
    }

    private BufferedImage dimg;
    private Image tmp;
    private Image image;

    private BufferedImage fitDocument() {
        if (null != document) {
            int w = (int) document.getPageDimension(pageNumber, 0, scale).toDimension().getWidth();
            int h = (int) document.getPageDimension(pageNumber, 0, scale).toDimension().getHeight();

            image = document.getPageImage(pageNumber, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX, 0f, java.awt.Image.SCALE_SMOOTH);
            tmp = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            dimg = new BufferedImage(w, h, BufferedImage.SCALE_FAST);

            Graphics2D g2d = dimg.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();

            return dimg;
        }
        return null;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
