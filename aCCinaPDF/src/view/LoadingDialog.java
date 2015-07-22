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

import com.madgag.gif.fmsware.GifDecoder;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 *
 * @author Diogo
 */
public class LoadingDialog extends javax.swing.JDialog {

    private boolean done;
    private final MainWindow parent;
    private boolean canceled;
    private int fileCounter;
    private volatile boolean running = false;

    public enum LoadingType {

        FILE_SEARCHING,
        SMARTCARD_SEARCHING,
        SIGNING,
        VALIDATING
    };

    /**
     * Creates new form LoadingDialog
     *
     * @param parent
     * @param modal
     */
    public LoadingDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.parent = (MainWindow) parent;
        setupListeners();
        initComponents();
        this.getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void showDialog(LoadingType type) {
        switch (type) {
            case FILE_SEARCHING:
                setTitle("A procurar");
                setText("Documentos encontrados: 0"); 
                setIcon("/image/search.jpg");
                btn.setEnabled(true);
                break;
            case SMARTCARD_SEARCHING:
                setTitle("A procurar");
                setText("A obter informações do CC...");
                setIcon("/image/insertCC.jpg");
                btn.setEnabled(true);
                break;
            case SIGNING:
                setTitle("A aplicar assinatura");
                setText("Aguarde...");
                setIcon("/image/signature.jpg");
                btn.setEnabled(false);
                break;
            case VALIDATING:
                setTitle("A validar");
                setText("Aguarde...");
                setIcon("/image/validate.jpg");
                btn.setEnabled(true);
                break;
        }
        this.setVisible(true);
        done = false;
        fileCounter = 0;
        parent.setLoading(true);
    }

    public void setText(String text) {
        lblText.setText(text);
    }

    private void setIcon(String path) {
        ImageIcon ii = new ImageIcon(LoadingDialog.class.getResource(path));
        lblImage.setIcon(ii);
    }

    public void incrementFileCounter() {
        fileCounter++;
        lblText.setText("Documentos encontrados: " + fileCounter);
    }

    private void setupListeners() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent we) {
                canceled = false;
                if (null == t) {
                    startThread();
                }
                done = false;
                parent.setLoading(true);
            }

            @Override
            public void windowDeactivated(WindowEvent we) {
                // Protege o consumo de recursos do while() loop após a janela ser fechada ou enquanto estiver inactiva
                done = true;
                t = null;
            }

            @Override
            public void windowClosed(WindowEvent we) {
                parent.setLoading(false);
                done = true;
                t = null;
            }
        });
    }

    private Thread t;

    private void startThread() {
        if (null == t) {
            done = false;
            final GifDecoder gd = new GifDecoder();
            gd.read(LoadingDialog.class.getResourceAsStream("/image/loading_bar.gif"));
            final int n = gd.getFrameCount();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    running = true;
                    while (!done) {
                        for (int i = 0; i < n; i++) {
                            try {
                                BufferedImage iframe = gd.getFrame(i);
                                Thread.sleep(gd.getDelay(i));
                                lblGif.setIcon(new ImageIcon(iframe));
                            } catch (InterruptedException ex) {
                                controller.Logger.getLogger().addEntry(ex);
                            }
                        }
                    }
                }
            };
            t = new Thread(r);
            t.start();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btn = new javax.swing.JButton();
        lblGif = new javax.swing.JLabel();
        lblText = new javax.swing.JLabel();
        lblImage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        btn.setText("Cancelar");
        btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActionPerformed(evt);
            }
        });

        lblText.setText("A carregar...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGif, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblText)
                            .addComponent(lblImage))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                .addComponent(lblGif, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActionPerformed
        canceled = true;
        parent.setLoading(false);
        done = true;
        t = null;
        parent.setLoading(false);
        this.setVisible(false);
    }//GEN-LAST:event_btnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoadingDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoadingDialog dialog = new LoadingDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn;
    private javax.swing.JLabel lblGif;
    private javax.swing.JLabel lblImage;
    private javax.swing.JLabel lblText;
    // End of variables declaration//GEN-END:variables

    public boolean isCanceled() {
        return canceled;
    }
}
