package com.etlions.ms_bs_etia.reniec.service.impl;

import com.etlions.ms_bs_etia.reniec.service.ReniecService;
import com.etlions.openapicodegen.dto.ReniecData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Mock RENIEC / SUNAT document consultation service.
 * In production, this would integrate with apis.net.pe or apiperu.dev.
 */
@Service
@RequiredArgsConstructor
public class ReniecServiceImpl implements ReniecService {

    @Override
    public ReniecData consultarDocumento(String tipo, String numero) {
        ReniecData data = new ReniecData();

        if ("DNI".equalsIgnoreCase(tipo)) {
            if (numero == null || numero.length() != 8) {
                throw new IllegalArgumentException("DNI debe tener 8 dígitos");
            }
            // Mock response — in production call RENIEC API
            data.tipoDocumento("DNI")
                .numeroDocumento(numero)
                .nombre("JUAN CARLOS")
                .apellidoPaterno("GARCIA")
                .apellidoMaterno("LOPEZ")
                .denominacion("JUAN CARLOS GARCIA LOPEZ")
                .direccion("AV. AREQUIPA 1234, LIMA")
                .departamento("LIMA")
                .provincia("LIMA")
                .distrito("MIRAFLORES");
        } else if ("RUC".equalsIgnoreCase(tipo)) {
            if (numero == null || numero.length() != 11) {
                throw new IllegalArgumentException("RUC debe tener 11 dígitos");
            }
            // Mock response — in production call SUNAT API
            data.tipoDocumento("RUC")
                .numeroDocumento(numero)
                .denominacion("EMPRESA DEMO S.A.C.")
                .razonSocial("EMPRESA DEMO S.A.C.")
                .direccion("CAL. LOS NEGOCIOS 456, SAN ISIDRO, LIMA")
                .estado("ACTIVO")
                .condicion("HABIDO")
                .departamento("LIMA")
                .provincia("LIMA")
                .distrito("SAN ISIDRO");
        } else {
            // Other doc types — no external lookup available
            data.tipoDocumento(tipo)
                .numeroDocumento(numero)
                .mensaje("Consulta no disponible para tipo de documento: " + tipo);
        }

        return data;
    }
}

