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
    private int align;
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
            if (showName && !aliasName.isEmpty()) {
                int charCountNome = m.stringWidth(aliasName)/2;
                if (m.stringWidth(aliasName) < (getWidth())) {
                    drawLine(charCountNome, aliasName, x, y, g, m);
                } else {
                    String[] words = aliasName.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth()) {
                            currentLine += " " + words[i];
                        } else {
                            charCountNome = m.stringWidth(currentLine);
                            drawLine(charCountNome, currentLine, x, y, g, m);
                            y += m.getHeight();
                            currentLine = words[i];
                        }
                    }
                    if (currentLine.trim().length() > 0) {
                        charCountNome = m.stringWidth(currentLine)/2;
                        drawLine(charCountNome, currentLine, x, y, g, m);
                    }
                }
                y += 15;
            }
        }
        if (reason != null) {
            if (showReason && !reason.isEmpty()) {
                int reasonCharCount = m.stringWidth(reason)/2;
                if (m.stringWidth(reason) < (getWidth())) {
                    drawLine(reasonCharCount, reason, x, y, g, m);
                } else {
                    String[] words = reason.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth()) {
                            currentLine += " " + words[i];
                        } else {
                            reasonCharCount = m.stringWidth(currentLine);
                            drawLine(reasonCharCount, currentLine, x, y, g, m);
                            y += m.getHeight();
                            currentLine = words[i];
                        }
                    }
                    if (currentLine.trim().length() > 0) {
                        reasonCharCount = m.stringWidth(currentLine)/2;
                        drawLine(reasonCharCount, currentLine, x, y, g, m);
                    }
                }
                y += 15;
            }
        }
        if (location != null) {
            if (showLocation && !location.isEmpty()) {
                int locationCharCount = m.stringWidth(location)/2;
                if (m.stringWidth(location) < (getWidth())) {
                    drawLine(locationCharCount, location, x, y, g, m);
                } else {
                    String[] words = location.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        
                        if (m.stringWidth(currentLine + words[i]) < getWidth()) {
                            currentLine += " " + words[i];
                        } else {
                            locationCharCount = m.stringWidth(currentLine);
                            drawLine(locationCharCount, currentLine, x, y, g, m);
                            y += m.getHeight();
                            currentLine = words[i];
                        }
                    }
                    if (currentLine.trim().length() > 0) {
                        locationCharCount = m.stringWidth(currentLine)/2;
                        drawLine(locationCharCount, currentLine, x, y, g, m);
                    }
                }
                y += 15;
            }
        }
        if (showDate) {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss ");
            String timestamp = sdf.format(date) + "+" + TimeZone.SHORT;
            int timestampCharCount = m.stringWidth(timestamp)/2;
            if (m.stringWidth(timestamp) < (getWidth())) {
                   drawLine(timestampCharCount, timestamp, x, y, g, m);
            } else {
                String[] words = timestamp.split(" ");
                String currentLine = words[0];
                for (int i = 1; i < words.length; i++) {
                    if (m.stringWidth(currentLine + words[i]) < getWidth()) {
                        currentLine += " " + words[i];
                    } else {
                        timestampCharCount = m.stringWidth(currentLine);
                        drawLine(timestampCharCount, currentLine, x, y, g, m);
                        y += m.getHeight();
                        currentLine = words[i];
                    }
                }
                if (currentLine.trim().length() > 0) {
                    timestampCharCount = m.stringWidth(currentLine)/2;
                    drawLine(timestampCharCount, currentLine, x, y, g, m);
                }
            }
            y += 15;
        }

        if (text != null) {
            if (!text.isEmpty()) {
                int textCharCount = m.stringWidth(text)/2;
                if (m.stringWidth(text) < (getWidth())) {
                    drawLine(textCharCount, text, x, y, g, m);
                } else {
                    String[] words = text.split(" ");
                    String currentLine = words[0];
                    for (int i = 1; i < words.length; i++) {
                        if (m.stringWidth(currentLine + words[i]) < getWidth()) {
                            currentLine += " " + words[i];
                        } else {
                            textCharCount = m.stringWidth(currentLine);
                            drawLine(textCharCount, currentLine, x, y, g, m);
                            y += m.getHeight(); 
                            currentLine = words[i];
                        }
                    }
                    if (currentLine.trim().length() > 0) {
                        textCharCount = m.stringWidth(currentLine)/2;
                        drawLine(textCharCount, currentLine, x, y, g, m);
                    }
                }
            }
        }
    }
    
    private void drawLine(int numChars, String text, int x, int y, Graphics g, FontMetrics m){
                   if(align == 0 || align > 2 || align < 0){
                        g.drawString(text, x, y);
                    }else if(align == 1){
                        if(numChars >= 280){
                           g.drawString(text, x, y); 
                        }else{
                           g.drawString(text, 280 - numChars, y); 
                        }
                    }else if(align == 2){
                        if(numChars >= 560){
                            g.drawString(text, x, y);
                        }else{
                            numChars = m.stringWidth(text);
                            g.drawString(text, 560-numChars, y);
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

    public void setAlign(int align) {
        this.align = align;
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
