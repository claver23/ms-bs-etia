package com.etlions.ms_bs_etia.doc_boleta.service;

import com.etlions.openapicodegen.dto.BoletaResponseDto;
import com.etlions.openapicodegen.dto.AnularBoletaDto;
import com.etlions.openapicodegen.dto.BoletaEmitirDto;
import com.etlions.openapicodegen.dto.BoletaListResponseDto;
import com.etlions.openapicodegen.dto.DocumentValidationResultDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

import java.util.Date;

public interface DocBoletaService {

    BoletaResponseDto emitirBoleta(BoletaEmitirDto request);

    BoletaResponseDto anularBoleta(String boletaId, AnularBoletaDto request);

    SuccessResponse eliminarBoletaBorrador(String boletaId);

    BoletaListResponseDto listarBoletas(String companyId, Date desdeFecha, Date hastaFecha,
                                           String serie, Integer numero,
                                           Integer page, Integer size);

    BoletaResponseDto obtenerBoleta(String boletaId);

    /** Ejecuta validación interna de una boleta y actualiza su estado a VALIDADO o CON_ERROR */
    DocumentValidationResultDto validarBoleta(String boletaId);

    /** Actualiza un borrador de boleta sin emitir */
    BoletaResponseDto actualizarBoletaBorrador(String boletaId, BoletaEmitirDto request);

    /** Descarga el XML UBL 2.1 tipo "03" de la boleta */
    BoletaXmlResult descargarBoletaXml(String boletaId);

    /** Result record para descarga de XML */
    record BoletaXmlResult(byte[] content, String filename, boolean hasContent) {}
}

