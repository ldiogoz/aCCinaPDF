/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import listener.SignatureClickListener;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

/**
 *
 * @author Toshiba
 */
public class SignatureValidation {

    private final String filename;
    private final String name;
    private final PdfPKCS7 pdfPkcs7;
    private final boolean changed;
    private final boolean coversEntireDocument;
    private final int revision, numRevisions;
    private final int certificationLevel;
    private final CertificateStatus ocspCertificateStatus;
    private final CertificateStatus crlCertificateStatus;
    private final boolean validTimeStamp;
    private final List<AcroFields.FieldPosition> posList;
    private final JPanel panel;
    private SignatureClickListener listener;

    public SignatureValidation(String filename, String name, PdfPKCS7 pdfPkcs7, boolean changed, boolean coversEntireDocument, int revision, int numRevisions, int certificationLevel, CertificateStatus ocspCertificateStatus, CertificateStatus crlCertificateStatus, boolean validTimeStamp, List<AcroFields.FieldPosition> posList) {
        this.filename = filename;
        this.name = name;
        this.pdfPkcs7 = pdfPkcs7;
        this.changed = changed;
        this.coversEntireDocument = coversEntireDocument;
        this.revision = revision;
        this.numRevisions = numRevisions;
        this.certificationLevel = certificationLevel;
        this.ocspCertificateStatus = ocspCertificateStatus;
        this.crlCertificateStatus = crlCertificateStatus;
        this.validTimeStamp = validTimeStamp;
        this.posList = posList;
        this.panel = new JPanel();

        this.panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.panel.setBackground(new Color(0, 0, 0, 0));
        this.panel.setToolTipText(name);
        this.panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (SwingUtilities.isLeftMouseButton(evt)) {
                    if (listener != null) {
                        listener.onSignatureClick(SignatureValidation.this);
                    }
                }
            }
        });
    }

    public int getNumRevisions() {
        return numRevisions;
    }

    public void setListener(SignatureClickListener listener) {
        this.listener = listener;
    }

    public JPanel getPanel() {
        return panel;
    }

    public List<AcroFields.FieldPosition> getPosList() {
        return posList;
    }

    public String getFilename() {
        return filename;
    }

    public CertificateStatus getOcspCertificateStatus() {
        return ocspCertificateStatus;
    }

    public CertificateStatus getCrlCertificateStatus() {
        return crlCertificateStatus;
    }

    public boolean isValidTimeStamp() {
        return validTimeStamp;
    }

    public String getName() {
        return name;
    }

    public PdfPKCS7 getSignature() {
        return pdfPkcs7;
    }

    public boolean isChanged() {
        return changed;
    }

    public boolean isCoversEntireDocument() {
        return coversEntireDocument;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isValid() {
        //TODO
        return !changed;
    }

    public boolean isCertified() {
        return (coversEntireDocument
                && certificationLevel == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED
                || certificationLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING
                || certificationLevel == PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS);
    }

    public boolean isWarning() {
        return (getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED));
    }

    public String getSignerName() {
        X509Certificate x509cert = (X509Certificate) pdfPkcs7.getSigningCertificate();
        org.bouncycastle.asn1.x500.X500Name x500name = null;
        try {
            x500name = new JcaX509CertificateHolder(x509cert).getSubject();
        } catch (CertificateEncodingException ex) {
            return "DESCONHECIDO";
        }
        RDN rdn = x500name.getRDNs(BCStyle.CN)[0];
        return IETFUtils.valueToString(rdn.getFirst().getValue());
    }

    private boolean isVisible() {
        return ((this.posList.get(0).position.getWidth() != 0) && (this.posList.get(0).position.getHeight() != 0));
    }

    @Override
    public String toString() {
        return name + (this.isVisible() ? "" : " (Invisível)");
    }

}