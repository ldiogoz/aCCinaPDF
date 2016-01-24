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
package controller;

import model.Settings;
import accinapdf.ACCinaPDF;
import com.itextpdf.text.BaseColor;
import model.CCSignatureSettings;
import model.CCAlias;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfEncryption;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.JFileChooser;
import listener.SignatureListener;
import listener.ValidationListener;
import model.CertificateStatus;
import model.SignatureValidation;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
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
import view.MultipleValidationDialog;

/**
 *
 * @author Diogo
 */
public class CCInstance {

    private static final String SIGNATURE_CREATOR = "aCCinaPDF";
    private static final String KEYSTORE_PATH = "/keystore/aCCinaPDF_cacerts";

    private KeyStore ks;
    private KeyStore pkcs11ks;
    private SunPKCS11 pkcs11Provider;
    private final ArrayList<CCAlias> aliasList = new ArrayList<>();

    private static CCInstance instance;

    public CCInstance() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void newIstance() {
        instance = new CCInstance();
    }

    public static final CCInstance getInstance() {
        return instance;
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
            throw new LibraryNotLoadedException(Bundle.getBundle().getString("unknownOS"));
        } else if (new File(path).exists()) {
            pkcs11config += path;
        } else {
            String res = userLoadLibraryPKCS11();
            if (null != res) {
                pkcs11config += res;
            }
            throw new LibraryNotFoundException(Bundle.getBundle().getString("libraryNotFound"));
        }
        final byte[] pkcs11configBytes;
        try {
            pkcs11configBytes = pkcs11config.getBytes();
        } catch (Exception eiie) {
            throw new LibraryNotFoundException(Bundle.getBundle().getString("libraryDoesNotExist"));
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
            throw new LibraryNotLoadedException(Bundle.getBundle().getString("libraryNotLoaded"));
        }

        Security.addProvider(pkcs11Provider);

        try {
            pkcs11ks = KeyStore.getInstance("PKCS11");
            pkcs11ks.load(null, null);
        } catch (Exception e) {
            throw new KeyStoreNotLoadedException(Bundle.getBundle().getString("keystoreNotLoaded"));
        }

        final Enumeration aliasesEnum = pkcs11ks.aliases();
        aliasList.clear();
        try {
            // show the list of available terminals
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals: " + terminals);
            // get the first terminal
            CardTerminal terminal = terminals.get(0);
            // establish a connection with the card
            Card card = terminal.connect("T=0");
            System.out.println("card: " + card.getProtocol());
            CardChannel channel = card.getBasicChannel();
            byte[] c1 = new byte[]{0x00, (byte) 0xA4, 0x00, 0x0C};
            ResponseAPDU r = channel.transmit(new CommandAPDU(c1));
            if (r.getSW() == 0x9000) {
                System.out.println("response: " + (Arrays.toString(r.getBytes())));
            } else {
                System.err.println("response error");
            }

            // disconnect
            card.disconnect(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (aliasesEnum.hasMoreElements()) {
            final String alias = (String) aliasesEnum.nextElement();
            if (null != alias) {
                if (alias.isEmpty()) {
                    throw new AliasException(Bundle.getBundle().getString("blankAlias"));
                } else {
                    final Certificate[] certChain = pkcs11ks.getCertificateChain(alias);
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
                                    throw new CertificateException(Bundle.getBundle().getString("aliasCertificate") + " " + alias + " " + Bundle.getBundle().getString("expired") + "!");
                                } catch (CertificateNotYetValidException cee) {
                                    throw new CertificateException(Bundle.getBundle().getString("aliasCertificate") + " " + alias + " " + Bundle.getBundle().getString("notYetValid") + "!");
                                }
                            } else {
                                throw new CertificateException(Bundle.getBundle().getString("chainInvalidFormat"));
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
            final PrivateKey pkey = (PrivateKey) pkcs11ks.getKey(alias, null);
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

    public final Certificate[] getCompleteTrustedCertificateChain(final X509Certificate x509c) throws KeyStoreException, IOException, FileNotFoundException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException {
        final ArrayList<X509Certificate> certChainList = new ArrayList<>();
        certChainList.add(x509c);
        X509Certificate temp = x509c;
        while (true) {
            X509Certificate issuer = (X509Certificate) hasTrustedIssuerCertificate(temp);
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
        pk = getPrivateKeyFromAlias(settings.getCcAlias().getAlias());

        if (getCertificationLevel(pdfPath) == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED) {
            String message = Bundle.getBundle().getString("fileDoesNotAllowChanges");
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new SignatureFailedException(message);
        }

        if (reader.getNumberOfPages() - 1 < settings.getPageNumber()) {
            settings.setPageNumber(reader.getNumberOfPages() - 1);
        }

        if (null == pk) {
            String message = Bundle.getBundle().getString("noSmartcardFound");
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new CertificateException(message);
        }

        final Certificate[] certChain;
        if (null == pkcs11ks.getCertificateChain(settings.getCcAlias().getAlias())) {
            String message = Bundle.getBundle().getString("certificateNullChain");
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new CertificateException(message);
        }
        final Certificate owner = pkcs11ks.getCertificateChain(settings.getCcAlias().getAlias())[0];
        if (null == owner || 1 < pkcs11ks.getCertificateChain(settings.getCcAlias().getAlias()).length) {
            String message = Bundle.getBundle().getString("certificateNameUnknown");
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new CertificateException(message);
        }

        final X509Certificate X509C = ((X509Certificate) owner);
        final Calendar now = Calendar.getInstance();
        certChain = getCompleteTrustedCertificateChain(X509C);

        // Leitor e Stamper
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(destination);
        } catch (FileNotFoundException e) {
            String message = Bundle.getBundle().getString("outputFileError");
            if (sl != null) {
                sl.onSignatureComplete(pdfPath, false, message);
            }
            throw new IOException(message);
        }

        // Aparência da Assinatura
        final char pdfVersion;
        switch (Settings.getSettings().getPdfVersion()) {
            case "/1.2":
                pdfVersion = PdfWriter.VERSION_1_2;
                break;
            case "/1.3":
                pdfVersion = PdfWriter.VERSION_1_3;
                break;
            case "/1.4":
                pdfVersion = PdfWriter.VERSION_1_4;
                break;
            case "/1.5":
                pdfVersion = PdfWriter.VERSION_1_5;
                break;
            case "/1.6":
                pdfVersion = PdfWriter.VERSION_1_6;
                break;
            case "/1.7":
                pdfVersion = PdfWriter.VERSION_1_7;
                break;
            default:
                pdfVersion = PdfWriter.VERSION_1_7;
        }

        final PdfStamper stamper = (getNumberOfSignatures(pdfPath) == 0 ? PdfStamper.createSignature(reader, os, pdfVersion) : PdfStamper.createSignature(reader, os, pdfVersion, null, true));

        final PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setSignDate(now);
        appearance.setReason(settings.getReason());
        appearance.setLocation(settings.getLocation());
        appearance.setCertificationLevel(settings.getCertificationLevel());
        appearance.setSignatureCreator(SIGNATURE_CREATOR);
        appearance.setCertificate(owner);

        final String fieldName = settings.getPrefix() + " " + (1 + getNumberOfSignatures(pdfPath));
        if (settings.isVisibleSignature()) {
            appearance.setVisibleSignature(settings.getPositionOnDocument(), settings.getPageNumber() + 1, fieldName);
            appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
            if (null != settings.getAppearance().getImageLocation()) {
                appearance.setImage(Image.getInstance(settings.getAppearance().getImageLocation()));
            }

            com.itextpdf.text.Font font = new com.itextpdf.text.Font(FontFactory.getFont(settings.getAppearance().getFontLocation(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0).getBaseFont());

            font.setColor(new BaseColor(settings.getAppearance().getFontColor().getRGB()));
            if (settings.getAppearance().isBold() && settings.getAppearance().isItalic()) {
                font.setStyle(Font.BOLD + Font.ITALIC);
            } else if (settings.getAppearance().isBold()) {
                font.setStyle(Font.BOLD);
            } else if (settings.getAppearance().isItalic()) {
                font.setStyle(Font.ITALIC);
            } else {
                font.setStyle(Font.PLAIN);
            }

            appearance.setLayer2Font(font);
            String text = "";
            if (settings.getAppearance().isShowName()) {
                if (!settings.getCcAlias().getName().isEmpty()) {
                    text += settings.getCcAlias().getName() + "\n";
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
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("Z");
                text += df.format(now.getTime()) + " " + sdf.format(now.getTime()) + "\n";
            }
            if (!settings.getText().isEmpty()) {
                text += settings.getText();
            }

            PdfTemplate layer2 = appearance.getLayer(2);
            Rectangle rect = settings.getPositionOnDocument();
            Rectangle sr = new Rectangle(rect.getWidth(), rect.getHeight());
            float size = ColumnText.fitText(font, text, sr, 1024, PdfWriter.RUN_DIRECTION_DEFAULT);
            ColumnText ct = new ColumnText(layer2);
            ct.setRunDirection(PdfWriter.RUN_DIRECTION_DEFAULT);
            ct.setAlignment(Element.ALIGN_MIDDLE);
            int align;
            switch (settings.getAppearance().getAlign()) {
                case 0:
                    align = Element.ALIGN_LEFT;
                    break;
                case 1:
                    align = Element.ALIGN_CENTER;
                    break;
                case 2:
                    align = Element.ALIGN_RIGHT;
                    break;
                default:
                    align = Element.ALIGN_LEFT;
            }

            ct.setSimpleColumn(new Phrase(text, font), sr.getLeft(), sr.getBottom(), sr.getRight(), sr.getTop(), size, align);
            ct.go();
        } else {
            appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, fieldName);
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
                throw new SignatureFailedException(Bundle.getBundle().getString("userCanceled"));
            } else if ("sun.security.pkcs11.wrapper.PKCS11Exception: CKR_GENERAL_ERROR".equals(e.getMessage())) {
                throw new SignatureFailedException(Bundle.getBundle().getString("noPermissions"));
            } else if (e instanceof ExceptionConverter) {
                String message = Bundle.getBundle().getString("timestampFailed");
                if (sl != null) {
                    sl.onSignatureComplete(pdfPath, false, message);
                }
                throw new SignatureFailedException(message);
            } else {
                if (sl != null) {
                    sl.onSignatureComplete(pdfPath, false, Bundle.getBundle().getString("unknownErrorLog"));
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
            final PdfPKCS7 pk = af.verifySignature(name, "BC");
            final Certificate pkc[] = pk.getCertificates();
            x509c = (X509Certificate) pkc[pkc.length - 1];

            final Certificate[] aL = pkc;//getCompleteCertificateChain(x509c);

            if (null == aL || 0 == aL.length) {
                return null;
            }

            CertificateStatus ocspCertificateStatus = CertificateStatus.UNCHECKED;

            BasicOCSPResp ocspResp = pk.getOcsp();
            if (null != ocspResp && pk.isRevocationValid()) {
                for (SingleResp singleResp : ocspResp.getResponses()) {
                    if (null == singleResp.getCertStatus()) {
                        ocspCertificateStatus = CertificateStatus.OK;
                    } else if (singleResp.getCertStatus() instanceof RevokedStatus) {
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
                    Certificate[] completeChain = getCompleteTrustedCertificateChain(x509c);
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
                validTimestamp = hasTimestamp && CertificateVerification.verifyTimestampCertificates(tst, ks, null);
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
        final File fout = File.createTempFile("temp", " - " + WordUtils.capitalize(Bundle.getBundle().getString("revision")) + ": " + revision + ".pdf");
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
        fileChooser.setDialogTitle(Bundle.getBundle().getString("openLibrary"));
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

    private KeyStore defaultKs;

    public KeyStore getDefaultKeystore() {
        if (null == defaultKs) {
            final InputStream fis = CCInstance.class.getResourceAsStream(KEYSTORE_PATH);
            defaultKs = null;
            try {
                defaultKs = KeyStore.getInstance(KeyStore.getDefaultType());
                defaultKs.load(fis, null);
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            }
        }
        return defaultKs;
    }

    public KeyStore getKeystore() {
        return this.ks;
    }

    public void setKeystore(KeyStore ks) {
        this.ks = ks;
    }

    public ArrayList<Certificate> getTrustedCertificatesFromKeystore(KeyStore keystore) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException {

        final PKIXParameters params = new PKIXParameters(keystore);
        final ArrayList<Certificate> alTrustedCertificates = new ArrayList<>();

        for (final TrustAnchor ta : params.getTrustAnchors()) {
            Certificate cert = (Certificate) ta.getTrustedCert();
            alTrustedCertificates.add(cert);
        }

        return alTrustedCertificates;
    }

    public Certificate hasTrustedIssuerCertificate(final X509Certificate x509c) {
        ArrayList<Certificate> alTrustedCertificates;
        try {
            alTrustedCertificates = getTrustedCertificatesFromKeystore(getKeystore());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException ex) {
            return null;
        }

        if (alTrustedCertificates.isEmpty()) {
            return null;
        }

        for (final Certificate c : alTrustedCertificates) {
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

    public boolean isTrustedCertificate(final X509Certificate x509c) {
        ArrayList<Certificate> alTrustedCertificates;
        try {
            alTrustedCertificates = getTrustedCertificatesFromKeystore(getKeystore());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException ex) {
            return false;
        }

        if (alTrustedCertificates.isEmpty()) {
            return false;
        }

        for (final Certificate c : alTrustedCertificates) {
            try {
                final X509Certificate x509cc = (X509Certificate) c;
                if (x509c.getSubjectX500Principal().equals(x509cc.getSubjectX500Principal())) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
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
                if ("SHA1".equalsIgnoreCase(parts[0])) {
                    return "SHA-1";
                } else if ("SHA2".equalsIgnoreCase(parts[0])) {
                    return "SHA-2";
                } else if ("SHA3".equalsIgnoreCase(parts[0])) {
                    return "SHA-3";
                } else if ("SHA128".equalsIgnoreCase(parts[0])) {
                    return "SHA-128";
                } else if ("SHA256".equalsIgnoreCase(parts[0])) {
                    return "SHA-256";
                } else if ("SHA384".equalsIgnoreCase(parts[0])) {
                    return "SHA-384";
                } else if ("SHA512".equalsIgnoreCase(parts[0])) {
                    return "SHA-512";
                } else {
                    return parts[0];
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

    public String getCertificateProperty(X500Name x500name, String property) {
        String cn = "";
        LdapName ldapDN = null;
        try {
            ldapDN = new LdapName(x500name.toString());
        } catch (InvalidNameException ex) {
            java.util.logging.Logger.getLogger(MultipleValidationDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Rdn rdn : ldapDN.getRdns()) {
            if (rdn.getType().equals(property)) {
                cn = rdn.getValue().toString();
            }
        }
        return cn;
    }
}
