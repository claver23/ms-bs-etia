package com.etlions.ms_bs_etia.app_util.sunat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * Servicio de firma digital XML (XMLDSig) para comprobantes electronicos SUNAT.
 * Firma el XML UBL 2.1 con un certificado digital .p12/.pfx.
 */
@Slf4j
@Service
public class SunatSigningService {

    /**
     * Firma un XML UBL 2.1 con el certificado digital proporcionado.
     *
     * @param xmlContent      XML sin firmar
     * @param certPath         Ruta al archivo .p12/.pfx
     * @param certPassword     Password del certificado
     * @return XML firmado como string
     */
    public String signXml(String xmlContent, String certPath, String certPassword) {
        try {
            // 1. Cargar certificado
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream fis = new FileInputStream(certPath)) {
                ks.load(fis, certPassword.toCharArray());
            }

            String alias = ks.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, certPassword.toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            // 2. Parsear XML
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));

            // 3. Encontrar el nodo ExtensionContent donde va la firma
            Node signatureNode = findSignatureNode(doc);
            if (signatureNode == null) {
                log.warn("No se encontro nodo ExtensionContent para firma, firmando en root");
                signatureNode = doc.getDocumentElement();
            }

            // 4. Crear firma XMLDSig
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            Reference ref = fac.newReference("",
                    fac.newDigestMethod(DigestMethod.SHA256, null),
                    List.of(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                    null, null);

            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
                    Collections.singletonList(ref));

            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data x509d = kif.newX509Data(List.of(cert));
            KeyInfo ki = kif.newKeyInfo(List.of(x509d));

            // 5. Firmar
            DOMSignContext dsc = new DOMSignContext(privateKey, signatureNode);
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

            // 6. Serializar a string
            return serializeDocument(doc);

        } catch (FileNotFoundException e) {
            log.error("Certificado no encontrado: {}", certPath);
            throw new RuntimeException("Certificado digital no encontrado: " + certPath, e);
        } catch (Exception e) {
            log.error("Error firmando XML: {}", e.getMessage(), e);
            throw new RuntimeException("Error al firmar XML: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si el servicio puede firmar (certificado existe y es valido).
     */
    public boolean canSign(String certPath, String certPassword) {
        if (certPath == null || certPath.isBlank()) return false;
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream fis = new FileInputStream(certPath)) {
                ks.load(fis, certPassword != null ? certPassword.toCharArray() : null);
            }
            return ks.aliases().hasMoreElements();
        } catch (Exception e) {
            log.warn("Certificado no valido o no accesible: {}", e.getMessage());
            return false;
        }
    }

    private Node findSignatureNode(Document doc) {
        // Buscar ext:ExtensionContent dentro de UBLExtensions
        NodeList nodes = doc.getElementsByTagNameNS("*", "ExtensionContent");
        if (nodes.getLength() > 0) {
            return nodes.item(0);
        }
        return null;
    }

    private String serializeDocument(Document doc) throws Exception {
        var tf = TransformerFactory.newInstance().newTransformer();
        var sw = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}

