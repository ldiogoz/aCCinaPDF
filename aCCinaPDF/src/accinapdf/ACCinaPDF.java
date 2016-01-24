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
package accinapdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import controller.Bundle;
import controller.CCInstance;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import model.CCSignatureSettings;
import model.CertificateStatus;
import model.SignatureValidation;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;
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
        controller.Bundle.getBundle();

        if (GraphicsEnvironment.isHeadless()) {
            // Headless
            // Erro 
            String fich;
            if (args.length != 1) {
                System.err.println(Bundle.getBundle().getString("invalidArgs"));
                return;
            } else {
                fich = args[0];
            }

            try {
                System.out.println(Bundle.getBundle().getString("validating") + " " + fich);
                ArrayList<SignatureValidation> alSv = CCInstance.getInstance().validatePDF(fich, null);
                if (alSv.isEmpty()) {
                    System.out.println(Bundle.getBundle().getString("notSigned"));
                } else {
                    String newLine = System.getProperty("line.separator");
                    String toWrite = "(";
                    int numSigs = alSv.size();
                    if (numSigs == 1) {
                        toWrite += "1 " + Bundle.getBundle().getString("signature");
                    } else {
                        toWrite += numSigs + " " + Bundle.getBundle().getString("signatures");
                    }
                    toWrite += ")" + newLine;
                    for (SignatureValidation sv : alSv) {
                        toWrite += "\t" + sv.getName() + " - ";
                        toWrite += (sv.isCertification() ? WordUtils.capitalize(Bundle.getBundle().getString("certificate")) : WordUtils.capitalize(Bundle.getBundle().getString("signed"))) + " " + Bundle.getBundle().getString("by") + " " + sv.getSignerName();
                        toWrite += newLine + "\t\t";
                        if (sv.isChanged()) {
                            toWrite += Bundle.getBundle().getString("certifiedChangedOrCorrupted");
                        } else {
                            if (sv.isCertification()) {
                                if (sv.isValid()) {
                                    if (sv.isChanged() || !sv.isCoversEntireDocument()) {
                                        toWrite += Bundle.getBundle().getString("certifiedButChanged");
                                    } else {
                                        toWrite += Bundle.getBundle().getString("certifiedOk");
                                    }
                                } else {
                                    toWrite += Bundle.getBundle().getString("changedAfterCertified");
                                }
                            } else {
                                if (sv.isValid()) {
                                    if (sv.isChanged()) {
                                        toWrite += Bundle.getBundle().getString("signedButChanged");
                                    } else {
                                        toWrite += Bundle.getBundle().getString("signedOk");
                                    }
                                } else {
                                    toWrite += Bundle.getBundle().getString("signedChangedOrCorrupted");
                                }
                            }
                        }
                        toWrite += newLine + "\t\t";
                        if (sv.getOcspCertificateStatus().equals(CertificateStatus.OK) || sv.getCrlCertificateStatus().equals(CertificateStatus.OK)) {
                            toWrite += Bundle.getBundle().getString("certOK");
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.REVOKED) || sv.getCrlCertificateStatus().equals(CertificateStatus.REVOKED)) {
                            toWrite += Bundle.getBundle().getString("certRevoked");
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && sv.getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED)) {
                            toWrite += Bundle.getBundle().getString("certNotVerified");
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHAINED)) {
                            toWrite += Bundle.getBundle().getString("certNotChained");
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.EXPIRED)) {
                            toWrite += Bundle.getBundle().getString("certExpired");
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.CHAINED_LOCALLY)) {
                            toWrite += Bundle.getBundle().getString("certChainedLocally");
                        }
                        toWrite += newLine + "\t\t";
                        if (sv.isValidTimeStamp()) {
                            toWrite += Bundle.getBundle().getString("validTimestamp");
                        } else {
                            toWrite += Bundle.getBundle().getString("signerDateTime");
                        }
                        toWrite += newLine + "\t\t";

                        toWrite += WordUtils.capitalize(Bundle.getBundle().getString("revision")) + ": " + sv.getRevision() + " " + Bundle.getBundle().getString("of") + " " + sv.getNumRevisions();
                        toWrite += newLine + "\t\t";
                        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        final SimpleDateFormat sdf = new SimpleDateFormat("Z");
                        if (sv.getSignature().getTimeStampToken() == null) {
                            Calendar cal = sv.getSignature().getSignDate();
                            String date = sdf.format(cal.getTime().toLocaleString());
                            toWrite += date + " " + sdf.format(cal.getTime()) + " (" + Bundle.getBundle().getString("signerDateTimeSmall") + ")";
                        } else {
                            Calendar ts = sv.getSignature().getTimeStampDate();
                            String date = df.format(ts.getTime());
                            toWrite += Bundle.getBundle().getString("date") + " " + date + " " + sdf.format(ts.getTime());
                        }
                        toWrite += newLine + "\t\t";
                        boolean ltv = (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK);
                        toWrite += Bundle.getBundle().getString("isLtv") + ": " + (ltv ? Bundle.getBundle().getString("yes") : Bundle.getBundle().getString("no"));
                        String reason = sv.getSignature().getReason();
                        toWrite += newLine + "\t\t";
                        toWrite += Bundle.getBundle().getString("reason") + ": ";
                        if (reason == null) {
                            toWrite += Bundle.getBundle().getString("notDefined");
                        } else if (reason.isEmpty()) {
                            toWrite += Bundle.getBundle().getString("notDefined");
                        } else {
                            toWrite += reason;
                        }
                        String location = sv.getSignature().getLocation();
                        toWrite += newLine + "\t\t";
                        toWrite += Bundle.getBundle().getString("location") + ": ";
                        if (location == null) {
                            toWrite += Bundle.getBundle().getString("notDefined");
                        } else if (location.isEmpty()) {
                            toWrite += Bundle.getBundle().getString("notDefined");
                        } else {
                            toWrite += location;
                        }
                        toWrite += newLine + "\t\t";
                        toWrite += Bundle.getBundle().getString("allowsChanges") + ": ";
                        try {
                            int certLevel = CCInstance.getInstance().getCertificationLevel(sv.getFilename());
                            if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING) {
                                toWrite += Bundle.getBundle().getString("onlyAnnotations");
                            } else if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS) {
                                toWrite += Bundle.getBundle().getString("annotationsFormFilling");
                            } else if (certLevel == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
                                toWrite += Bundle.getBundle().getString("no");
                            } else {
                                toWrite += Bundle.getBundle().getString("yes");
                            }
                        } catch (IOException ex) {
                            controller.Logger.getLogger().addEntry(ex);
                        }
                        toWrite += newLine + "\t\t";
                        if (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK) {
                            toWrite += (Bundle.getBundle().getString("validationCheck1") + " "
                                    + (sv.getOcspCertificateStatus() == CertificateStatus.OK ? Bundle.getBundle().getString("validationCheck2") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getOcsp().getCerts()[0].getSubject(), "CN") + " " + Bundle.getBundle().getString("at") + " " + df.format(sv.getSignature().getOcsp().getProducedAt()) : (sv.getCrlCertificateStatus() == CertificateStatus.OK ? "CRL" : ""))
                                    + (sv.getSignature().getTimeStampToken() != null ? Bundle.getBundle().getString("validationCheck3") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O") : ""));
                        } else if (sv.getSignature().getTimeStampToken() != null) {
                            toWrite += (Bundle.getBundle().getString("validationCheck3") + ": " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O"));
                        }
                        toWrite += newLine;
                    }

                    System.out.println(toWrite);
                    System.out.println(Bundle.getBundle().getString("validationFinished"));
                }
            } catch (IOException | DocumentException | GeneralSecurityException ex) {
                System.err.println(Bundle.getBundle().getString("validationError"));
                Logger.getLogger(ACCinaPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            // GUI
            CCSignatureSettings defaultSettings = new CCSignatureSettings(false);
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

            new SplashScreen().setVisible(true);
        }
    }
}
