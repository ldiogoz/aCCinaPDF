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

import com.itextpdf.text.pdf.AcroFields;
import controller.Bundle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import model.SignatureValidation;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import model.Settings;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.icepdf.core.pobjects.Document;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

public class ImagePanel extends JPanel {

    private MainWindow mainWindow;
    private PDDocument pdfDocument;
    private PDFRenderer pdfRenderer;
    private String documentLocation;
    private int pageNumber, numberOfPages;
    private float scale;

    private JScrollPane parent;
    private JButton btnPageBackward;
    private JButton btnPageForward;

    private Status status;

    public void clear() {
        bi = null;
        if (pdfDocument != null) {
            try {
                pdfDocument.close();
            } catch (IOException ex) {
            }
        }
        pdfDocument = null;
        pdfRenderer = null;
        documentLocation = null;
    }

    public enum DocumentPageControl {

        PAGE_UP,
        PAGE_DOWN
    };

    public enum Status {

        READY,
        RENDERING
    };

    public ImagePanel() {
        super();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public synchronized void componentResized(ComponentEvent e) {
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

    private BufferedImage buf;

    public void setDocumentAndComponents(final MainWindow mainWindow, JScrollPane parent, Document document, final JButton btnBackward, final JButton btnForward) {
        this.mainWindow = mainWindow;
        this.parent = parent;
        try {
            this.documentLocation = document.getDocumentLocation();
            this.pdfDocument = PDDocument.load(new File(documentLocation));
            this.pdfRenderer = new PDFRenderer(pdfDocument);
            numberOfPages = pdfDocument.getPages().getCount();

            if (buf != null) {
                buf.flush();
                buf = null;
            }

            System.gc();

            Runnable r = new Runnable() {

                @Override
                public void run() {
                    status = Status.RENDERING;
                    ImagePanel.this.btnPageBackward = btnBackward;
                    ImagePanel.this.btnPageForward = btnForward;
                    ImagePanel.this.pageNumber = 0;
                    ImagePanel.this.scale = 1f;
                    ImagePanel.this.svList = null;
                    setBorder(null);
                    JLabel lblDocName = new JLabel(documentLocation);
                    lblDocName.setLocation(0, 0);
                    add(lblDocName);
                    lblDocName.setVisible(true);
                    try {
                        buf = pdfRenderer.renderImage(0, 2f, ImageType.RGB);
                        mainWindow.getWorkspacePanel().showPanelComponents();
                        status = Status.READY;
                    } catch (IOException ex) {
                        return;
                    }
                    refreshParent();
                    refreshTitle();
                }
            };

            Thread t = new Thread(r);
            t.start();

            if (pdfDocument == null) {
                mainWindow.getWorkspacePanel().setDocument(null);
                return;
            }

        } catch (IOException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }
    }

    final private ArrayList<JPanel> panelList = new ArrayList<>();

    public void refreshSignatureValidationListPanels() {
        for (JPanel jp : panelList) {
            remove(jp);
        }

        if (buf == null) {
            return;
        }

        if (pdfDocument != null) {
            if (svList != null) {
                Point p = getImageLocation();
                for (final SignatureValidation sv : svList) {
                    try {
                        int pgNumber = sv.getPosList().get(0).page - 1;
                        if (this.pageNumber == pgNumber) {
                            for (AcroFields.FieldPosition pos : sv.getPosList()) {
                                int p1 = (int) (p.x + (pos.position.getLeft() * scale));
                                int p2 = (int) (p.y + Math.floor((pdfDocument.getPage(pageNumber).getCropBox().getHeight() - pos.position.getTop()) * scale));
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
                                        if (mainWindow.getWorkspacePanel().getStatus() != WorkspacePanel.Status.SIGNING) {
                                            jp1.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                            jp1.setBackground(new Color(0, 0, 0, 45));
                                            jp1.setBorder(new LineBorder(Color.BLACK, 1));
                                            repaint();
                                        } else {
                                            jp1.setCursor(null);
                                        }
                                    }

                                    @Override
                                    public void mouseExited(java.awt.event.MouseEvent evt) {
                                        if (mainWindow.getWorkspacePanel().getStatus() != WorkspacePanel.Status.SIGNING) {
                                            if (selectedSignature == null) {
                                                jp1.setBackground(new Color(0, 0, 0, 0));
                                                jp1.setBorder(null);
                                                repaint();
                                            } else if (!selectedSignature.equals(sv)) {
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
                    } catch (Exception e) {
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
        return bi == null ? super.getPreferredSize() : new Dimension(bi.getWidth(), bi.getHeight());
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        bi = fitDocument();
    }

    public float scaleUp() {
        if (scale < 3f) {
            scale += 0.2f;
            refreshParent();
            refreshSignatureValidationListPanels();
        }
        return scale;
    }

    public float scaleDown() {
        if (scale > 0.4f) {
            scale -= 0.2f;
            refreshParent();
            refreshSignatureValidationListPanels();
        }
        return scale;
    }

    public boolean setPageNumberControl(DocumentPageControl dpc) {
        boolean changed = false;
        if (dpc.equals(DocumentPageControl.PAGE_UP)) {
            if (null != pdfDocument && pageNumber < (numberOfPages - 1)) {
                pageNumber++;
                changed = true;
                refreshSignatureValidationListPanels();
            }
        } else if (dpc.equals(DocumentPageControl.PAGE_DOWN)) {
            if (null != pdfDocument && pageNumber > 0) {
                pageNumber--;
                changed = true;
                refreshSignatureValidationListPanels();
            }
        }

        if (0 == pageNumber) {
            btnPageBackward.setEnabled(false);
            if (numberOfPages > 1) {
                btnPageForward.setEnabled(true);
            }
        } else if (numberOfPages == (pageNumber + 1)) {
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
        render();
        refreshParent();
        refreshTitle();
        refreshSignatureValidationListPanels();
    }

    private void render() {
        buf = null;
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    buf = pdfRenderer.renderImage(pageNumber, 2f, ImageType.RGB);
                } catch (IOException ex) {
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    private void refreshParent() {
        while (true) {
            try {
                bi = fitDocument();
                if (bi != null) {
                    repaint();
                    parent.setViewportView(ImagePanel.this);
                    break;
                }
                Thread.sleep(100);
            } catch (Exception e) {

            }
        }
    }

    public void refreshTitle() {
        if (mainWindow != null) {
            if (documentLocation != null) {
                mainWindow.setTitle(Bundle.getBundle().getString("app.name") + " - " + documentLocation + " - " + WordUtils.capitalize(Bundle.getBundle().getString("page")) + " " + (pageNumber + 1) + " " + Bundle.getBundle().getString("of") + " " + numberOfPages);
            }
        }
    }

    public Point getImageLocation() {

        Point p = null;
        if (bi != null) {
            int x = (getWidth() - bi.getWidth()) / 2;
            int y = (getHeight() - bi.getHeight()) / 2;
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
        if (bi != null) {
            try {
                Point p = getImageLocation();
                g.drawImage(bi, p.x, p.y, this);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(p.x, p.y, bi.getWidth(), bi.getHeight());
            } catch (Exception e) {
            }
        }
    }

    private BufferedImage bi;

    private BufferedImage fitDocument() {
        bi = null;

        if (null != pdfDocument) {
            status = Status.RENDERING;
            Method method;
            if (Settings.getSettings().getRenderImageQuality() == 3) {
                method = Scalr.Method.QUALITY;
            } else if (Settings.getSettings().getRenderImageQuality() == 1) {
                method = Scalr.Method.SPEED;
            } else {
                method = Scalr.Method.BALANCED;
            }
            try {
                bi = Scalr.resize(buf, method, (int) (pdfDocument.getPage(pageNumber).getCropBox().getWidth() * scale), (int) (pdfDocument.getPage(pageNumber).getCropBox().getHeight() * scale));
                refreshSignatureValidationListPanels();
                status = Status.READY;
                System.gc();
                return bi;
            } catch (Exception e) {
                status = Status.READY;
            }

        }
        return null;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public Status getStatus() {
        return status;
    }
}
