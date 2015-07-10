/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
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
    private boolean showName;
    private String aliasName;

    public PreviewPanel() {
        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setFont(new Font("Verdana", Font.PLAIN, 11));
        FontMetrics m = g.getFontMetrics();

        int x = 5, y = 15;

        if (aliasName != null) {
            if (showName) {
                if (m.stringWidth(aliasName) < (getWidth())) {
                    g.drawString(aliasName, x, y);
                } else {
                    String[] words = aliasName.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth()) {
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
        if (reason != null) {
            if (showReason) {
                if (m.stringWidth(reason) < (getWidth())) {
                    g.drawString(reason, x, y);
                } else {
                    String[] words = reason.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth()) {
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
                if (m.stringWidth(location) < (getWidth())) {
                    g.drawString(location, x, y);
                } else {
                    String[] words = location.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth()) {
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
            if (m.stringWidth(timestamp) < (getWidth())) {
                g.drawString(timestamp, x, y);
            } else {
                String[] words = timestamp.split(" ");
                String currentLine = words[0];
                for (int i = 1; i < words.length; i++) {
                    if (m.stringWidth(currentLine + words[i]) < getWidth()) {
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
            if (m.stringWidth(text) < (getWidth())) {
                g.drawString(text, x, y);
            } else {
                String[] words = text.split(" ");
                String currentLine = words[0];
                for (int i = 1; i < words.length; i++) {
                    if (m.stringWidth(currentLine + words[i]) < getWidth()) {
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

    public boolean isShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
        repaint();
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }
}
