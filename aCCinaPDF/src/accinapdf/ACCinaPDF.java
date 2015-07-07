/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accinapdf;

import com.itextpdf.text.DocumentException;
import controller.CCInstance;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import model.CCSignatureSettings;
import org.apache.commons.lang3.SystemUtils;
import view.SplashScreen;

/**
 *
 * @author Diogo
 */
public class ACCinaPDF {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        controller.Logger.create();

        CCSignatureSettings a = new CCSignatureSettings();
        if (GraphicsEnvironment.isHeadless()) {
            // Headless
            // Erro 
            if (args.length != 1) {
                System.err.println("Args inválidos! > java -jar AssinaturasDigitaisCC.jar <ficheiro a validar>");
                return;
            }

            String fich = args[0];
            CCInstance.newIstance();
            try {
                CCInstance.getInstance().validatePDF(fich, null);
            } catch (IOException | DocumentException | GeneralSecurityException ex) {
                Logger.getLogger(ACCinaPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            // GUI

            if (SystemUtils.IS_OS_WINDOWS) {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Windows".equals(info.getName())) {
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                            Logger.getLogger(ACCinaPDF.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                }
            } else if (SystemUtils.IS_OS_LINUX) {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                            Logger.getLogger(ACCinaPDF.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                }
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.mac.MacLookAndFeel");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(ACCinaPDF.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            CCInstance.newIstance();
            new SplashScreen().setVisible(true);
        }
    }
}
