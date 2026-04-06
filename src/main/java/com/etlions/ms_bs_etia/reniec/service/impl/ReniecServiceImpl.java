package com.etlions.ms_bs_etia.reniec.service.impl;

import com.etlions.ms_bs_etia.reniec.service.ReniecService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock RENIEC / SUNAT document consultation service.
 * In production, this would integrate with apis.net.pe or apiperu.dev.
 */
@Service
@RequiredArgsConstructor
public class ReniecServiceImpl implements ReniecService {

    @Override
    public Map<String, Object> consultarDocumento(String tipo, String numero) {
        Map<String, Object> result = new HashMap<>();

        if ("DNI".equalsIgnoreCase(tipo)) {
            if (numero == null || numero.length() != 8) {
                result.put("error", "DNI debe tener 8 dígitos");
                return result;
            }
            // Mock response — in production call RENIEC API
            result.put("tipoDocumento", "DNI");
            result.put("numeroDocumento", numero);
            result.put("nombre", "JUAN CARLOS");
            result.put("apellidoPaterno", "GARCIA");
            result.put("apellidoMaterno", "LOPEZ");
            result.put("denominacion", "JUAN CARLOS GARCIA LOPEZ");
            result.put("direccion", "AV. AREQUIPA 1234, LIMA");
            result.put("departamento", "LIMA");
            result.put("provincia", "LIMA");
            result.put("distrito", "MIRAFLORES");
        } else if ("RUC".equalsIgnoreCase(tipo)) {
            if (numero == null || numero.length() != 11) {
                result.put("error", "RUC debe tener 11 dígitos");
                return result;
            }
            // Mock response — in production call SUNAT API
            result.put("tipoDocumento", "RUC");
            result.put("numeroDocumento", numero);
            result.put("denominacion", "EMPRESA DEMO S.A.C.");
            result.put("razonSocial", "EMPRESA DEMO S.A.C.");
            result.put("direccion", "CAL. LOS NEGOCIOS 456, SAN ISIDRO, LIMA");
            result.put("estado", "ACTIVO");
            result.put("condicion", "HABIDO");
            result.put("departamento", "LIMA");
            result.put("provincia", "LIMA");
            result.put("distrito", "SAN ISIDRO");
        } else {
            // Other doc types — no external lookup available
            result.put("tipoDocumento", tipo);
            result.put("numeroDocumento", numero);
            result.put("denominacion", null);
            result.put("direccion", null);
            result.put("mensaje", "Consulta no disponible para tipo de documento: " + tipo);
        }

        return result;
    }
}

