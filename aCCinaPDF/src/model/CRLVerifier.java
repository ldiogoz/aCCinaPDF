/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;

public final class CRLVerifier {

    /**
     * Extracts the CRL distribution points from the certificate (if available)
     * and checks the certificate revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP and LDAP based URLs.
     * 
     * @param cert
     *            the certificate to be checked for revocation
     * @throws CertificateVerificationException
     *             if the certificate is revoked
     */
    
    private CRLVerifier() {
        
    }
    
    public static void verifyCertificateCRLs(X509Certificate cert) throws Exception {
        try {
            List<String> crlDistPoints = getCrlDistributionPoints(cert);
            for (String crlDP : crlDistPoints) {
                X509CRL crl = downloadCRL(crlDP);
                if (crl.isRevoked(cert)) {
                    System.err.println("revoked");
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Downloads CRL from given URL. Supports http, https, ftp and ldap based
     * URLs.
     */
    private static X509CRL downloadCRL(String crlURL) throws IOException,
            CertificateException, CRLException,
            NamingException, Exception {
        if (crlURL.startsWith("http://") || crlURL.startsWith("https://")
                || crlURL.startsWith("ftp://")) {
            return downloadCRLFromWeb(crlURL);
        } else if (crlURL.startsWith("ldap://")) {
            return downloadCRLFromLDAP(crlURL);
        } else {
            throw new Exception("cant download");
        }
    }

    /**
     * Downloads a CRL from given LDAP url, e.g.
     * ldap://ldap.infonotary.com/dc=identity-ca,dc=infonotary,dc=com
     */
    private static X509CRL downloadCRLFromLDAP(String ldapURL) throws CertificateException, 
    NamingException, CRLException, Exception {
        Map<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);

        DirContext ctx = new InitialDirContext((Hashtable)env);
        Attributes avals = ctx.getAttributes("");
        Attribute aval = avals.get("certificateRevocationList;binary");
        byte[] val = (byte[]) aval.get();
        if ((val == null) || (val.length == 0)) {
            throw new Exception(
                    "Can not download CRL from: " + ldapURL);
        } else {
            InputStream inStream = new ByteArrayInputStream(val);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509CRL) cf.generateCRL(inStream);
        }
    }

    /**
     * Downloads a CRL from given HTTP/HTTPS/FTP URL, e.g.
     * http://crl.infonotary.com/crl/identity-ca.crl
     */
    private static X509CRL downloadCRLFromWeb(String crlURL) throws MalformedURLException,
    IOException, CertificateException,
            CRLException {
        URL url = new URL(crlURL);
        InputStream crlStream = url.openStream();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509CRL) cf.generateCRL(crlStream);
        } finally {
            crlStream.close();
        }
    }

    /**
     * Extracts all CRL distribution point URLs from the
     * "CRL Distribution Point" extension in a X.509 certificate. If CRL
     * distribution point extension is unavailable, returns an empty list.
     * @param cert
     * @return 
     * @throws java.security.cert.CertificateParsingException 
     * @throws java.io.IOException 
     */
    public static List<String> 
    getCrlDistributionPoints(X509Certificate cert) throws CertificateParsingException, IOException {
        byte[] crldpExt = cert
                .getExtensionValue(X509Extensions.CRLDistributionPoints.getId());
        if (crldpExt == null) {
            return new ArrayList<>();
        }
        ASN1InputStream oAsnInStream = new ASN1InputStream(
                new ByteArrayInputStream(crldpExt));
        ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
        byte[] crldpExtOctets = dosCrlDP.getOctets();
        ASN1InputStream oAsnInStream2 = new ASN1InputStream(
                new ByteArrayInputStream(crldpExtOctets));
        ASN1Primitive derObj2 = oAsnInStream2.readObject();
        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        List<String> crlUrls = new ArrayList<>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null
                && dpn.getType() == DistributionPointName.FULL_NAME) {
                GeneralName[] genNames = GeneralNames.getInstance(
                        dpn.getName()).getNames();
                // Look for an URI
                for (int j = 0; j < genNames.length; j++) {
                    if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                        String url = DERIA5String.getInstance(
                                genNames[j].getName()).getString();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }

}