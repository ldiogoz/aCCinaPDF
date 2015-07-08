/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import accinapdf.ACCinaPDF;
import com.itextpdf.text.BaseColor;
import model.CCSignatureSettings;
import model.CCAlias;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfEncryption;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.CertificateUtil;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.KeyStoreUtil;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.ProviderDigest;
import com.itextpdf.text.pdf.security.SignaturePermissions;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import exception.AliasException;
import exception.KeyStoreNotLoadedException;
import exception.LibraryNotFoundException;
import exception.LibraryNotLoadedException;
import exception.RevisionExtractionException;
import exception.SignatureFailedException;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JFileChooser;
import listener.SignatureListener;
import listener.ValidationListener;
import model.CertificateStatus;
import model.SignatureValidation;
import org.apache.commons.lang3.SystemUtils;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.RevokedStatus;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.ocsp.UnknownStatus;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TimeStampToken;
import sun.security.pkcs11.SunPKCS11;

/**
 *
 * @author Diogo
 */
public class CCInstance {

    private static final String SIGNATURE_CREATOR = "aCCinaPDF";
    private final String keystoreFile = getCurrentFolder() + System.getProperty("file.separator") + "keystore" + System.getProperty("file.separator") + "aCCinaPDF_cacerts";
    //private final String keystoreFile = "C:\\aCCinaPDF_cacerts";

    private static CCInstance instance;

    public static void newIstance() {
        instance = new CCInstance();
    }

    public static final CCInstance getInstance() {
        return instance;
    }

    private KeyStore ks;
    private SunPKCS11 pkcs11Provider;
    private final ArrayList<CCAlias> aliasList = new ArrayList<>();

    public CCInstance() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public final ArrayList<CCAlias> loadKeyStoreAndAliases() throws LibraryNotLoadedException, KeyStoreNotLoadedException, CertificateException, KeyStoreException, LibraryNotFoundException, AliasException {
        String pkcs11config = "name = SmartCard\n library = ";
        String path = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            path = System.getenv("HOMEDRIVE") + "\\windows\\system32\\pteidpkcs11.dll";
        } else if (SystemUtils.IS_OS_LINUX) {
            path = "/usr/local/lib/libpteidpkcs11.so";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            path = "/usr/local/lib/pteidpkcs11.bundle";
        }

        if (null == path) {
            throw new LibraryNotLoadedException("Sistema Operativo desconhecido!");
        } else {
            if (new File(path).exists()) {
                pkcs11config += path;
            } else {
                String res = userLoadLibraryPKCS11();
                if (null != res) {
                    pkcs11config += res;
                }
                throw new LibraryNotFoundException("A biblioteca não foi encontrada!");
            }
        }
        final byte[] pkcs11configBytes;
        try {
            pkcs11configBytes = pkcs11config.getBytes();
        } catch (Exception eiie) {
            throw new LibraryNotFoundException("A biblioteca não existe!");
        }
        final ByteArrayInputStream configStream = new ByteArrayInputStream(pkcs11configBytes);
        try {
            pkcs11Provider = new sun.security.pkcs11.SunPKCS11(configStream);
            pkcs11Provider.setCallbackHandler(new CallbackHandler() {

                @Override
                public void handle(javax.security.auth.callback.Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (javax.security.auth.callback.Callback c : callbacks) {
                        if (c instanceof PasswordCallback) {
                            ((PasswordCallback) c).setPassword(null);
                        }
                    }
                }
            });
        } catch (Exception eiie) {
            throw new LibraryNotLoadedException("Não foi possível carregar a biblioteca!");
        }

        Security.addProvider(pkcs11Provider);

        try {
            ks = KeyStore.getInstance("PKCS11");
            ks.load(null, null);
            //String filename = this.keystoreFile;
            //FileInputStream fis = new FileInputStream(filename);
            //this.ks.load(fis, null);
        } catch (Exception e) {
            throw new KeyStoreNotLoadedException("Keystore não foi carregada com sucesso!");
        }

        final Enumeration aliasesEnum = ks.aliases();
        aliasList.clear();

        while (aliasesEnum.hasMoreElements()) {
            final String alias = (String) aliasesEnum.nextElement();
            if (null != alias) {
                if ("".equals(alias)) {
                    throw new AliasException("Alias está em branco");
                } else {
                    final Certificate[] certChain = ks.getCertificateChain(alias);
                    if (null != certChain) {
                        if (CCAlias.ASSINATURA.equals(alias)) {
                            if (1 == certChain.length) {
                                final Certificate cert = certChain[0];
                                try {
                                    ((X509Certificate) cert).checkValidity();
                                    if (1 <= certChain.length) {
                                        final CCAlias ccAliasTemp = new CCAlias(alias, cert);
                                        aliasList.add(ccAliasTemp);
                                    }
                                } catch (CertificateExpiredException cee) {
                                    throw new CertificateException("O Certificado do alias " + alias + " expirou!");
                                } catch (CertificateNotYetValidException cee) {
                                    throw new CertificateException("O Certificado do alias " + alias + " ainda não é válido!");
                                }
                            } else {
                                throw new CertificateException("A Cadeia de Certificados tem um formato inválido!");
                            }
                        }
                    }
                }
            }
        }
        return aliasList;
    }

    public final ArrayList<CCAlias> getAliasList() {
        return aliasList;
    }

    private PrivateKey getPrivateKeyFromAlias(final String alias) {
        try {
            final PrivateKey pkey = (PrivateKey) ks.getKey(alias, null);
            return pkey;
        } catch (KeyStoreException e) {
            controller.Logger.getLogger().addEntry(e);
        } catch (NoSuchAlgorithmException e) {
            controller.Logger.getLogger().addEntry(e);
        } catch (UnrecoverableKeyException e) {
            controller.Logger.getLogger().addEntry(e);
        } catch (Exception e) {
            controller.Logger.getLogger().addEntry(e);
        }
        return null;
    }

    public final Certificate[] getCompleteCertificateChain(final X509Certificate x509c) throws KeyStoreException, IOException, FileNotFoundException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException {
        final ArrayList<X509Certificate> certChainList = new ArrayList<X509Certificate>();
        certChainList.add(x509c);
        X509Certificate temp = x509c;
        while (true) {
            X509Certificate issuer = (X509Certificate) hasTrustedCertificate(temp);
            if (null != issuer) {
                if (temp.equals(issuer)) {
                    break;
                }
                certChainList.add(issuer);
                temp = issuer;
            } else {
                break;
            }
        }
        final Certificate[] certificateChain = new Certificate[certChainList.size()];
        for (int i = 0; i < certChainList.size(); i++) {
            certificateChain[i] = certChainList.get(i);
        }

        return certificateChain;
    }

    public final boolean signPdf(final String pdfPath, final String destination, final CCSignatureSettings settings, final SignatureListener sl) throws CertificateException, IOException, DocumentException, KeyStoreException, SignatureFailedException, FileNotFoundException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        PrivateKey pk;

        final PdfReader reader = new PdfReader(pdfPath);
        pk = getPrivateKeyFromAlias(settings.getCCAlias().getAlias());

        if (getCertificationLevel(pdfPath) == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
            String message = "O ficheiro não permite alterações!";
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new SignatureFailedException(message);
        }

        if (null == pk) {
            String message = "Erro! Não foi encontrado nenhum SmartCard!";
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new CertificateException(message);
        }

        final Certificate[] certChain;
        if (null == ks.getCertificateChain(settings.getCCAlias().getAlias())) {
            String message = "O Certificado contém uma cadeia nula!";
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new CertificateException(message);
        }
        final Certificate owner = ks.getCertificateChain(settings.getCCAlias().getAlias())[0];
        if (null == owner || 1 < ks.getCertificateChain(settings.getCCAlias().getAlias()).length) {
            String message = "Não foi possível obter o nome do titular do certificado!";
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new CertificateException(message);
        }

        final X509Certificate X509C = ((X509Certificate) owner);
        final Calendar now = Calendar.getInstance();
        certChain = getCompleteCertificateChain(X509C);

        // Leitor e Stamper
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(destination);
        } catch (FileNotFoundException e) {
            String message = "Erro a abrir o ficheiro de saída!";
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new IOException(message);
        }

        // Aparência da Assinatura
        final PdfStamper stamper;
        if (getNumberOfSignatures(pdfPath) == 0) {
            stamper = PdfStamper.createSignature(reader, os, PdfWriter.VERSION_1_2);
        } else {
            stamper = PdfStamper.createSignature(reader, os, PdfWriter.VERSION_1_2, null, true);
            PdfContentByte t = stamper.getOverContent(settings.getPageNumber());
        }

        final PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setSignDate(now);
        appearance.setReason(settings.getReason());
        appearance.setLocation(settings.getLocation());
        appearance.setCertificationLevel(settings.getCertificationLevel());
        appearance.setSignatureCreator(SIGNATURE_CREATOR);
        appearance.setCertificate(owner);

        if (settings.isVisibleSignature()) {
            final String fieldName = "aCCinatura " + (1 + getNumberOfSignatures(pdfPath));
            appearance.setVisibleSignature(settings.getPositionOnDocument(), settings.getPageNumber() + 1, fieldName);
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
            if (null != settings.getAppearance().getImageLocation()) {
                appearance.setImage(Image.getInstance(settings.getAppearance().getImageLocation()));
            }
            com.itextpdf.text.Font font = null;
            if (settings.getAppearance().isItalic() && settings.getAppearance().isBold()) {
                font = FontFactory.getFont(settings.getAppearance().getFontLocation(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0, Font.BOLD + Font.ITALIC, new BaseColor(settings.getAppearance().getFontColor().getRGB()));
            } else if (settings.getAppearance().isBold()) {
                font = FontFactory.getFont(settings.getAppearance().getFontLocation(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0, Font.BOLD, new BaseColor(settings.getAppearance().getFontColor().getRGB()));
            } else if (settings.getAppearance().isItalic()) {
                font = FontFactory.getFont(settings.getAppearance().getFontLocation(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0, Font.ITALIC, new BaseColor(settings.getAppearance().getFontColor().getRGB()));
            } else {
                font = FontFactory.getFont(settings.getAppearance().getFontLocation(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0, Font.PLAIN, new BaseColor(settings.getAppearance().getFontColor().getRGB()));
            }
            appearance.setLayer2Font(font);
            String text = "";
            if (settings.getAppearance().isShowName()) {
                if (!settings.getCCAlias().getName().isEmpty()) {
                    text += settings.getCCAlias().getName() + "\n";
                }
            }
            if (settings.getAppearance().isShowReason()) {
                if (!settings.getReason().isEmpty()) {
                    text += settings.getReason() + "\n";
                }
            }
            if (settings.getAppearance().isShowLocation()) {
                if (!settings.getLocation().isEmpty()) {
                    text += settings.getLocation() + "\n";
                }
            }
            if (settings.getAppearance().isShowDate()) {
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                text += df.format(now.getTime()) + " +" + (now.getTimeZone().getRawOffset() < 10 ? "0" : "") + now.getTimeZone().getRawOffset() + "\n";
            }
            if (!settings.getText().isEmpty()) {
                text += settings.getText();
            }

            appearance.setLayer2Text(text);
            //appearance.getCryptoDictionary().put(PdfName.P, new PdfNumber(settings.getCertificationLevel()));
        }

        // CRL <- Pesado!
        final ArrayList<CrlClient> crlList = null;

        // OCSP
        OcspClient ocspClient = new OcspClientBouncyCastle();

        // TimeStamp
        TSAClient tsaClient = null;
        if (settings.isTimestamp()) {
            tsaClient = new TSAClientBouncyCastle(settings.getTimestampServer(), null, null);
        }

        final String hashAlg = getHashAlgorithm(X509C.getSigAlgName());

        final ExternalSignature es = new PrivateKeySignature(pk, hashAlg, pkcs11Provider.getName());
        final ExternalDigest digest = new ProviderDigest(pkcs11Provider.getName());

        try {
            MakeSignature.signDetached(appearance, digest, es, certChain, crlList, ocspClient, tsaClient, 0, MakeSignature.CryptoStandard.CMS);
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, true, "");
            }
            return true;
        } catch (Exception e) {
            os.flush();
            os.close();
            new File(destination).delete();
            if ("sun.security.pkcs11.wrapper.PKCS11Exception: CKR_FUNCTION_CANCELED".equals(e.getMessage())) {
                throw new SignatureFailedException("Acção cancelada pelo utilizador!");
            } else if (e instanceof ExceptionConverter) {
                String message = "TimeStamp falhou: Não tem ligação à Internet ou o URL de Servidor de TimeStamp é inválido!";
                if (sl != null) {
                    sl.onSignatureComplete(pdfPath, false, message);
                }
                throw new SignatureFailedException(message);
            } else {
                if (sl != null) {
                    sl.onSignatureComplete(pdfPath, false, "Erro desconhecido - Ver log");
                }
                controller.Logger.getLogger().addEntry(e);
            }
            return false;
        }
    }

    public final int getNumberOfSignatures(final String pdfPath) {
        final KeyStore keystore = KeyStoreUtil.loadCacertsKeyStore();
        try {
            keystore.load(null, null);
            final PdfReader reader = new PdfReader(pdfPath);
            final int numSigs = reader.getAcroFields().getSignatureNames().size();
            reader.close();
            return numSigs;
        } catch (IOException ex) {
            controller.Logger.getLogger().addEntry(ex);
        } catch (NoSuchAlgorithmException ex) {
            controller.Logger.getLogger().addEntry(ex);
        } catch (CertificateException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }
        return -1;
    }

    public final ArrayList<SignatureValidation> validatePDF(final String file, final ValidationListener vl) throws IOException, DocumentException, GeneralSecurityException {
        this.validating = true;
        final KeyStore keystore = KeyStoreUtil.loadCacertsKeyStore();
        keystore.load(null, null);

        final PdfReader reader = new PdfReader(file);
        final AcroFields af = reader.getAcroFields();
        final ArrayList names = af.getSignatureNames();
        final ArrayList<SignatureValidation> validateList = new ArrayList<>();
        X509Certificate x509c = null;

        Security.setProperty("ocsp.enable", "true");
        System.setProperty("com.sun.security.enableCRLDP", "true");

        boolean nextValid = true;

        for (Object o : names) {
            if (!validating) {
                return null;
            }

            final String name = (String) o;
            final PdfPKCS7 pk = af.verifySignature(name);
            final Certificate pkc[] = pk.getCertificates();
            x509c = (X509Certificate) pkc[pkc.length - 1];

            final Certificate[] aL = getCompleteCertificateChain(x509c);
            if (null == aL || 0 == aL.length) {
                return null;
            }

            CertificateStatus ocspCertificateStatus = CertificateStatus.UNCHECKED;

            BasicOCSPResp ocspResp = pk.getOcsp();
            if (null != ocspResp && pk.isRevocationValid()) {
                for (SingleResp singleResp : ocspResp.getResponses()) {
                    if (null == singleResp.getCertStatus()) {
                        ocspCertificateStatus = CertificateStatus.OK;
                    } else {
                        if (singleResp.getCertStatus() instanceof RevokedStatus) {
                            if (ocspResp.getProducedAt().before(((RevokedStatus) singleResp.getCertStatus()).getRevocationTime())) {
                                ocspCertificateStatus = CertificateStatus.OK;
                            } else {
                                ocspCertificateStatus = CertificateStatus.REVOKED;
                            }
                        } else if (singleResp.getCertStatus() instanceof UnknownStatus) {
                            ocspCertificateStatus = CertificateStatus.UNKNOWN;
                        }
                    }
                }
            }

            CertificateStatus crlCertificateStatus = CertificateStatus.UNCHECKED;
            Collection<CRL> crlResp = pk.getCRLs();
            if (null != crlResp) {
                boolean revoked = false;
                for (CRL crl : crlResp) {
                    if (crl.isRevoked(x509c)) {
                        revoked = true;
                    }
                }
                crlCertificateStatus = revoked ? CertificateStatus.REVOKED : CertificateStatus.OK;
            }

            if (ocspCertificateStatus.equals(CertificateStatus.UNCHECKED) && crlCertificateStatus.equals(CertificateStatus.UNCHECKED)) {
                if (pkc.length == 1) {
                    Certificate[] completeChain = getCompleteCertificateChain(x509c);
                    if (completeChain.length == 1) {
                        ocspCertificateStatus = CertificateStatus.UNCHAINED;
                    } else {
                        ocspCertificateStatus = CertificateStatus.CHAINED_LOCALLY;
                    }
                }
            }

            final TimeStampToken tst = pk.getTimeStampToken();
            boolean validTimestamp = false;
            if (null != tst) {
                final boolean hasTimestamp = pk.verifyTimestampImprint();
                validTimestamp = hasTimestamp && CertificateVerification.verifyTimestampCertificates(tst, keystore, null);
            }

            PdfDictionary pdfDic = reader.getAcroFields().getSignatureDictionary(name);
            SignaturePermissions sp = new SignaturePermissions(pdfDic, null);

            boolean isValid;
            if (nextValid) {
                isValid = pk.verify();
            } else {
                isValid = false;
            }

            List<AcroFields.FieldPosition> posList = af.getFieldPositions(name);
            final SignatureValidation signature = new SignatureValidation(file, name, pk, !pk.verify(), af.signatureCoversWholeDocument(name), af.getRevision(name), af.getTotalRevisions(), reader.getCertificationLevel(), ocspCertificateStatus, crlCertificateStatus, validTimestamp, posList, sp, isValid);
            validateList.add(signature);

            if (null != vl) {
                vl.onValidationComplete(signature);
            }
            if (!sp.isFillInAllowed()) {
                nextValid = false;
            }
        }
        return validateList;
    }

    public File extractRevision(final String filePath, final String revision) throws IOException, RevisionExtractionException {
        final PdfReader reader = new PdfReader(filePath);
        final AcroFields af = reader.getAcroFields();
        final File fout = File.createTempFile("temp", " - Revisão: " + revision + ".pdf");
        final FileOutputStream os = new FileOutputStream(fout);
        final byte bb[] = new byte[1028];
        final InputStream ip = af.extractRevision(revision);
        if (null == ip) {
            throw new RevisionExtractionException();
        }
        int n = 0;
        while ((n = ip.read(bb)) > 0) {
            os.write(bb, 0, n);
        }
        os.close();
        ip.close();
        return fout;
    }

    private String userLoadLibraryPKCS11() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir Biblioteca");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String dest = fileChooser.getSelectedFile().getAbsolutePath();
            File file = new File(dest);
            if (file.exists()) {
                return dest;
            }
        }
        return null;
    }

    private Certificate hasTrustedCertificate(final X509Certificate x509c) throws KeyStoreException, IOException, FileNotFoundException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException {
        final FileInputStream fis = new FileInputStream(keystoreFile);
        final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(fis, null);

        final PKIXParameters params = new PKIXParameters(keystore);
        final ArrayList<Certificate> trustedCertList = new ArrayList<Certificate>();

        for (final TrustAnchor ta : params.getTrustAnchors()) {
            Certificate cert = (Certificate) ta.getTrustedCert();
            trustedCertList.add(cert);
        }

        if (trustedCertList.isEmpty()) {
            return null;
        }
        for (final Certificate c : trustedCertList) {
            try {
                final X509Certificate x509cc = (X509Certificate) c;
                if (x509c.getIssuerX500Principal().equals(x509cc.getSubjectX500Principal())) {
                    return c;
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public final String getCurrentFolder() {
        final CodeSource cs = ACCinaPDF.class.getProtectionDomain().getCodeSource();
        File f = null;
        try {
            f = new File(cs.getLocation().toURI().getPath());
        } catch (URISyntaxException ex) {
            return null;
        }
        return f.getParentFile().getPath();
    }

    private String getHashAlgorithm(String sigAlgName) {
        if (sigAlgName.toLowerCase().contains("with")) {
            final String[] parts;
            parts = sigAlgName.split("with");
            if (2 == parts.length) {
                if ("SHA1".equals(parts[0])) {
                    return "SHA-1";
                } else if ("SHA2".equals(parts[0])) {
                    return "SHA-2";
                } else if ("SHA3".equals(parts[0])) {
                    return "SHA-3";
                } else if ("SHA128".equals(parts[0])) {
                    return "SHA-128";
                } else if ("SHA256".equals(parts[0])) {
                    return "SHA-256";
                } else if ("SHA384".equals(parts[0])) {
                    return "SHA-384";
                } else if ("SHA512".equals(parts[0])) {
                    return "SHA-512";
                }
            }
        }

        return sigAlgName;
    }

    public final int getCertificationLevel(final String filename) throws IOException {
        final PdfReader reader = new PdfReader(filename);
        final int certLevel = reader.getCertificationLevel();
        reader.close();
        return certLevel;
    }

    private boolean validating;

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    private OCSPResp getOcspResponse(X509Certificate checkCert, X509Certificate rootCert) throws GeneralSecurityException, OCSPException, IOException, OperatorException {
        if (checkCert == null || rootCert == null) {
            return null;
        }
        String url = CertificateUtil.getOCSPURL(checkCert);

        if (url == null) {
            return null;
        }
        try {
            OCSPReq request = generateOCSPRequest(rootCert, checkCert.getSerialNumber());
            byte[] array = request.getEncoded();
            URL urlt = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlt.openConnection();
            con.setRequestProperty("Content-Type", "application/ocsp-request");
            con.setRequestProperty("Accept", "application/ocsp-response");
            con.setDoOutput(true);

            OutputStream out = con.getOutputStream();
            try (DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out))) {
                dataOut.write(array);
                dataOut.flush();
            }

            if (con.getResponseCode() / 100 != 2) {
                throw new IOException(MessageLocalization.getComposedMessage("invalid.http.response.1", con.getResponseCode()));
            }
            //Get Response
            InputStream in = (InputStream) con.getContent();
            return new OCSPResp(in);
        } catch (Exception e) {
            return null;
        }
    }

    private static OCSPReq generateOCSPRequest(X509Certificate issuerCert, BigInteger serialNumber) throws OCSPException, IOException, OperatorException, CertificateEncodingException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        CertificateID id = new CertificateID(new JcaDigestCalculatorProviderBuilder().build().get(CertificateID.HASH_SHA1), new JcaX509CertificateHolder(issuerCert), serialNumber);
        OCSPReqBuilder gen = new OCSPReqBuilder();
        gen.addRequest(id);
        Extension ext = new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false, new DEROctetString(new DEROctetString(PdfEncryption.createDocumentId()).getEncoded()));
        gen.setRequestExtensions(new Extensions(new Extension[]{ext}));
        return gen.build();
    }
}
