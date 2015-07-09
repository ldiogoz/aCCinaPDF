/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accinapdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
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

        if (GraphicsEnvironment.isHeadless()) {
            // Headless
            // Erro 
            String fich;
            if (args.length != 1) {
                System.err.println("Args inválidos! > java -jar AssinaturasDigitaisCC.jar <ficheiro a validar>");
                return;
            } else {
                fich = args[0];
            }

            CCInstance.newIstance();

            try {
                System.out.println("A validar as assinaturas no documento: " + fich);
                ArrayList<SignatureValidation> alSv = CCInstance.getInstance().validatePDF(fich, null);
                if (alSv.isEmpty()) {
                    System.out.println("O documento não está assinado.");
                } else {
                    String newLine = System.getProperty("line.separator");
                    String toWrite = "(";
                    int numSigs = alSv.size();
                    if (numSigs == 1) {
                        toWrite += "1 assinatura";
                    } else {
                        toWrite += numSigs + " assinaturas";
                    }
                    toWrite += ")" + newLine;
                    for (SignatureValidation sv : alSv) {
                        toWrite += "\t" + sv.getName() + " - ";
                        toWrite += (sv.isCertification() ? "Certificado" : "Assinado") + " por " + sv.getSignerName();
                        toWrite += newLine + "\t\t";
                        if (sv.isChanged()) {
                            toWrite += "O Documento foi alterado ou corrompido desde que foi certificado";
                        } else {
                            if (sv.isCertification()) {
                                if (sv.isValid()) {
                                    if (sv.isChanged() || !sv.isCoversEntireDocument()) {
                                        toWrite += "A revisão do documento que é coberto pela certificação não foi alterada. No entanto, ocorreram alterações posteriores ao documento";
                                    } else {
                                        toWrite += "O Documento está certificado e não foi modificado";
                                    }
                                } else {
                                    toWrite += "O Documento foi alterado ou corrompido desde que foi aplicada esta certificação";
                                }
                            } else {
                                if (sv.isValid()) {
                                    if (sv.isChanged()) {
                                        toWrite += "A revisão do documento que é coberto pela assinatura não foi alterada. No entanto, ocorreram alterações posteriores ao documento";
                                    } else {
                                        toWrite += "O Documento está assinado e não foi modificado";
                                    }
                                } else {
                                    toWrite += "O Documento foi alterado ou corrompido desde que foi aplicada esta assinatura";
                                }
                            }
                        }
                        toWrite += newLine + "\t\t";
                        if (sv.getOcspCertificateStatus().equals(CertificateStatus.OK) || sv.getCrlCertificateStatus().equals(CertificateStatus.OK)) {
                            toWrite += "O Certificado inerente a esta assinatura foi verificado e é válido";
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.REVOKED) || sv.getCrlCertificateStatus().equals(CertificateStatus.REVOKED)) {
                            toWrite += "O Certificado inerente a esta assinatura foi revogado";
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && sv.getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED)) {
                            toWrite += "Não foi feita a verificação da revogação de certificados durante a assinatura";
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.UNCHAINED)) {
                            toWrite += "O Certificado não está encadeado a um certificado designado como âncora confiável";
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.EXPIRED)) {
                            toWrite += "O Certificado expirou";
                        } else if (sv.getOcspCertificateStatus().equals(CertificateStatus.CHAINED_LOCALLY)) {
                            toWrite += "A assinatura não contém a âncora completa nem verificações de revogação. No entanto, o certificado do assinante foi emitido por um certificado na âncora confiável";
                        }
                        toWrite += newLine + "\t\t";
                        if (sv.isValidTimeStamp()) {
                            toWrite += "A Assinatura inclui um carimbo de Data e Hora válido";
                        } else {
                            toWrite += "A Data e Hora da assinatura são do relógio do computador do signatário";
                        }
                        toWrite += newLine + "\t\t";

                        toWrite += "Revisão: " + sv.getRevision() + " de " + sv.getNumRevisions();
                        toWrite += newLine + "\t\t";
                        final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        if (sv.getSignature().getTimeStampToken() == null) {
                            Calendar cal = sv.getSignature().getSignDate();
                            String date = df.format(cal.getTime());
                            toWrite += (date + " (hora do computador do signatário)");
                        } else {
                            Calendar ts = sv.getSignature().getTimeStampDate();
                            String date = df.format(ts.getTime());
                            toWrite += "Data: " + (date + " +" + (ts.getTimeZone().getRawOffset() < 10 ? "0" : "") + ts.getTimeZone().getRawOffset());
                        }
                        toWrite += newLine + "\t\t";
                        boolean ltv = (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK);
                        toWrite += "Habilitada para validação a longo termo: " + (ltv ? "Sim" : "Não");
                        String reason = sv.getSignature().getReason();
                        toWrite += newLine + "\t\t";
                        toWrite += "Razão: ";
                        if (reason == null) {
                            toWrite += "Não definida";
                        } else if (reason.equals("")) {
                            toWrite += "Não definida";
                        } else {
                            toWrite += reason;
                        }
                        String location = sv.getSignature().getLocation();
                        toWrite += newLine + "\t\t";
                        toWrite += "Localização: : ";
                        if (location == null) {
                            toWrite += "Não definido";
                        } else if (location.equals("")) {
                            toWrite += "Não definido";
                        } else {
                            toWrite += location;
                        }
                        toWrite += newLine + "\t\t";
                        toWrite += "Permite alterações: ";
                        try {
                            int certLevel = CCInstance.getInstance().getCertificationLevel(sv.getFilename());
                            if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING) {
                                toWrite += "Apenas anotações";
                            } else if (certLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS) {
                                toWrite += "Preenchimento de formulário e anotações";
                            } else if (certLevel == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
                                toWrite += "Não";
                            } else {
                                toWrite += "Sim";
                            }
                        } catch (IOException ex) {
                            controller.Logger.getLogger().addEntry(ex);
                        }
                        toWrite += newLine + "\t\t";
                        if (sv.getOcspCertificateStatus() == CertificateStatus.OK || sv.getCrlCertificateStatus() == CertificateStatus.OK) {
                            toWrite += ("O estado de revogação do certificado inerente a esta assinatura foi verificado com recurso a "
                                    + (sv.getOcspCertificateStatus() == CertificateStatus.OK ? "OCSP pela entidade: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getOcsp().getCerts()[0].getSubject(), "CN") + " em " + df.format(sv.getSignature().getOcsp().getProducedAt()) : (sv.getCrlCertificateStatus() == CertificateStatus.OK ? "CRL" : ""))
                                    + (sv.getSignature().getTimeStampToken() != null ? "O carimbo de data e hora é válido e foi assinado por: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O") : ""));
                        } else if (sv.getSignature().getTimeStampToken() != null) {
                            toWrite += ("O carimbo de data e hora é válido e foi assinado por: " + CCInstance.getInstance().getCertificateProperty(sv.getSignature().getTimeStampToken().getSID().getIssuer(), "O"));
                        }
                        toWrite += newLine;
                    }

                    System.out.println(toWrite);
                    System.out.println("Validação concluída");
                }
            } catch (IOException | DocumentException | GeneralSecurityException ex) {
                System.err.println("Ocorreu um erro durante a validação!");
                Logger.getLogger(ACCinaPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            // GUI
            CCSignatureSettings defaultSettings = new CCSignatureSettings();
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
