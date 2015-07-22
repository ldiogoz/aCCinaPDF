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
package model;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.apache.commons.lang3.text.WordUtils;
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

    public String getName() {
        try {
            String cn = getCN();
            return WordUtils.capitalize(cn.toLowerCase());
        } catch (CertificateEncodingException ex) {
            controller.Logger.getLogger().addEntry(ex);
        }
        return null;
    }

    @Override
    public String toString() {
        return getName() + ", " + alias;
    }

}
