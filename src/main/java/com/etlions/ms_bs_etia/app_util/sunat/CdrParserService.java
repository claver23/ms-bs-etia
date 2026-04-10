package com.etlions.ms_bs_etia.app_util.sunat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;

/**
 * Parsea el CDR (Constancia de Recepcion) XML devuelto por SUNAT.
 * Extrae codigo de respuesta, descripcion y hash de firma.
 */
@Slf4j
@Service
public class CdrParserService {

    /**
     * Parsea el XML del CDR y extrae la informacion relevante.
     */
    public CdrResult parse(String cdrXml) {
        if (cdrXml == null || cdrXml.isBlank()) {
            return CdrResult.rejected("9999", "CDR vacio o nulo");
        }
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            var builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(cdrXml.getBytes("UTF-8")));

            String codigo = extractText(doc, "ResponseCode");
            String descripcion = extractText(doc, "Description");
            String hash = extractText(doc, "DigestValue");

            boolean accepted = "0".equals(codigo) || (codigo != null && codigo.startsWith("0"));

            if (accepted) {
                return CdrResult.accepted(codigo, descripcion, hash);
            } else {
                return CdrResult.rejected(codigo, descripcion);
            }
        } catch (Exception e) {
            log.error("Error parseando CDR XML: {}", e.getMessage());
            return CdrResult.rejected("9999", "Error parseando CDR: " + e.getMessage());
        }
    }

    private String extractText(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() > 0 && nodes.item(0).getTextContent() != null) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
}

