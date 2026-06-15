package com.etlions.ms_bs_etia.doc_factura.xml;

import com.etlions.ms_bs_etia.doc_boleta.persistence.BoletaEntity;
import com.etlions.ms_bs_etia.doc_factura.persistence.FacturaEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Contexto unificado para la generación de XML UBL 2.1.
 * Abstrae los campos comunes entre FacturaEntity y BoletaEntity.
 *
 * @param tipoDocumento "01" = Factura, "03" = Boleta
 */
public record XmlBuildContext(
        String tipoDocumento,
        String serie,
        Integer numero,
        LocalDate fechaEmision,
        LocalTime horaEmision,
        LocalDate fechaVencimiento,
        String tipoOperacion,
        String moneda,
        String formaPago,
        BigDecimal tipoCambio,
        String codigoUbigeo,
        BigDecimal gravadas,
        BigDecimal exoneradas,
        BigDecimal inafectas,
        BigDecimal gratuitas,
        BigDecimal igv,
        BigDecimal total,
        BigDecimal totalCargos,
        BigDecimal totalDescuentos,
        BigDecimal subtotal,
        BigDecimal totalConImpuestos,
        String leyenda,
        String detalleJson
) {

    /** Construye contexto desde FacturaEntity (tipo "01") */
    public static XmlBuildContext fromFactura(FacturaEntity f) {
        return new XmlBuildContext(
                "01",
                f.getSerie(), f.getNumero(),
                f.getFechaEmision(), f.getHoraEmision(), f.getFechaVencimiento(),
                f.getTipoOperacion(), f.getMoneda(), f.getFormaPago(), f.getTipoCambio(),
                f.getCodigoUbigeo(),
                f.getGravadas(), f.getExoneradas(), f.getInafectas(), f.getGratuitas(),
                f.getIgv(), f.getTotal(),
                f.getTotalCargos(), f.getTotalDescuentos(),
                f.getSubtotal(), f.getTotalConImpuestos(),
                f.getLeyenda(), f.getDetalleJson()
        );
    }

    /** Construye contexto desde BoletaEntity (tipo "03") */
    public static XmlBuildContext fromBoleta(BoletaEntity b) {
        return new XmlBuildContext(
                "03",
                b.getSerie(), b.getNumero(),
                b.getFechaEmision(), b.getHoraEmision(), b.getFechaVencimiento(),
                b.getTipoOperacion(), b.getMoneda(), b.getFormaPago(), b.getTipoCambio(),
                b.getCodigoUbigeo(),
                b.getGravadas(), b.getExoneradas(), b.getInafectas(), b.getGratuitas(),
                b.getIgv(), b.getTotal(),
                b.getTotalCargos(), b.getTotalDescuentos(),
                b.getSubtotal(), b.getTotalConImpuestos(),
                b.getLeyenda(), b.getDetalleJson()
        );
    }
}

