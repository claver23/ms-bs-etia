package com.etlions.ms_bs_etia.doc_factura.service;

import com.etlions.openapicodegen.dto.AnularFactura200Response;
import com.etlions.openapicodegen.dto.AnularFacturaRequest;
import com.etlions.openapicodegen.dto.FacturaEmitirRequest;
import com.etlions.openapicodegen.dto.ListarFacturas200Response;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface docFacturaService {

    AnularFactura200Response emitirFactura(FacturaEmitirRequest request);

    AnularFactura200Response anularFactura(String facturaId, AnularFacturaRequest request);

    SuccessResponse eliminarFactura(String facturaId);

    ListarFacturas200Response listarFacturas(String companyId, Date desdeFecha, Date hastaFecha,
                                             String serie, Integer numero, String estado,
                                             Integer page, Integer size);

    AnularFactura200Response obtenerFactura(String facturaId);
}
