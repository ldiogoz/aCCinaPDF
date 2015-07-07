/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.JPanel;

/**
 *
 * @author admin
 */
public class PreviewPanel extends JPanel {

    private String reason;
    private String location;
    private boolean showReason;
    private boolean showLocation;
    private String text;
    private boolean showDate;

    public PreviewPanel() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setFont(new Font("Verdana", Font.PLAIN, 11));

        FontMetrics m = g.getFontMetrics();

        g.drawString("Nome", 100, (getHeight() / 2) + 5);

        int x = (getWidth() / 2) + 5;
        int y = 15;
        if (reason != null) {
            if (showReason) {
                if (m.stringWidth(reason) < (getWidth() / 2)) {
                    g.drawString(reason, x, y);
                } else {
                    String[] words = reason.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth() / 2) {
                            currentLine += " " + words[i];
                        } else {
                            g.drawString(currentLine, x, y);
                            y += m.getHeight();
                            currentLine = words[i];
                        }
                    }
                    if (currentLine.trim().length() > 0) {
                        g.drawString(currentLine, x, y);
                    }
                }
                y += 15;
            }
        }
        if (location != null) {
            if (showLocation) {
                if (m.stringWidth(location) < (getWidth() / 2)) {
                    g.drawString(location, x, y);
                } else {
                    String[] words = location.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth() / 2) {
                            currentLine += " " + words[i];
                        } else {
                            g.drawString(currentLine, x, y);
                            y += m.getHeight();
                            currentLine = words[i];
                        }
                    }
                    if (currentLine.trim().length() > 0) {
                        g.drawString(currentLine, x, y);
                    }
                }
                y += 15;
            }
        }
        if (showDate) {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss ");
            String timestamp = sdf.format(date) + "+" + TimeZone.SHORT;
            if (m.stringWidth(timestamp) < (getWidth() / 2)) {
                g.drawString(timestamp, x, y);
            } else {
                String[] words = timestamp.split(" ");
                String currentLine = words[0];
                for (int i = 1; i < words.length; i++) {
                    if (m.stringWidth(currentLine + words[i]) < getWidth() / 2) {
                        currentLine += " " + words[i];
                    } else {
                        g.drawString(currentLine, x, y);
                        y += m.getHeight();
                        currentLine = words[i];
                    }
                }
                if (currentLine.trim().length() > 0) {
                    g.drawString(currentLine, x, y);
                }
            }
            y += 15;
        }

        if (text != null) {
            if (m.stringWidth(text) < (getWidth() / 2)) {
                g.drawString(text, x, y);
            } else {
                String[] words = text.split(" ");
                String currentLine = words[0];
                for (int i = 1; i < words.length; i++) {
                    if (m.stringWidth(currentLine + words[i]) < getWidth() / 2) {
                        currentLine += " " + words[i];
                    } else {
                        g.drawString(currentLine, x, y);
                        y += m.getHeight();
                        currentLine = words[i];
                    }
                }
                if (currentLine.trim().length() > 0) {
                    g.drawString(currentLine, x, y);
                }
            }
        }
    }

    public void setShowReason(boolean showReason) {
        this.showReason = showReason;
        repaint();
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
        repaint();
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setShowLocation(boolean showLocation) {
        this.showLocation = showLocation;
        repaint();
    }
}
