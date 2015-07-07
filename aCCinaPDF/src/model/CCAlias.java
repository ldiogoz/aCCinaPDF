/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

/**
 *
 * @author Diogo
 */
public class CCAlias {

    public static final String ASSINATURA = "CITIZEN SIGNATURE CERTIFICATE";

    private final String alias;
    private final Certificate certificate;

    public CCAlias(String alias, Certificate certificate) {
        this.alias = alias;
        this.certificate = certificate;
    }

    public String getAlias() {
        return alias;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    private String getCN() throws CertificateEncodingException {
        X509Certificate x509cert = (X509Certificate) certificate;
        org.bouncycastle.asn1.x500.X500Name x500name = new JcaX509CertificateHolder(x509cert).getSubject();
        RDN rdn = x500name.getRDNs(BCStyle.CN)[0];

        return IETFUtils.valueToString(rdn.getFirst().getValue());
    }

    @Override
    public String toString() {
        try {
            String cn = getCN();
            return cn + ", " + alias;
        } catch (CertificateEncodingException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }
        return null;
    }

}
