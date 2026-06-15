package com.etlions.ms_bs_etia.resumen_diario.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Resumen Diario de Boletas (RC-{YYYYMMDD}-{NNN})
 * Agrupa boletas VALIDADAS para envío masivo a SUNAT.
 */
@Entity
@Table(name = "resumenes_diarios")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ResumenDiarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    /** Identificador SUNAT: RC-{YYYYMMDD}-{NNN} */
    @Column(nullable = false, unique = true)
    private String identificador;

    /** Fecha en que se generó el resumen */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    /** Fecha de referencia (fecha de emisión de las boletas incluidas) */
    @Column(name = "fecha_referencia", nullable = false)
    private LocalDate fechaReferencia;

    /** GENERADO, ENVIADO, ACEPTADO, RECHAZADO, CON_ERROR */
    @Column(nullable = false)
    private String estado;

    /** Ticket devuelto por SUNAT al enviar el resumen */
    @Column(name = "ticket_sunat")
    private String ticketSunat;

    @Column(name = "codigo_respuesta")
    private String codigoRespuesta;

    @Column(name = "descripcion_respuesta")
    private String descripcionRespuesta;

    @Column(name = "cantidad_boletas")
    private Integer cantidadBoletas;

    /** XML del resumen diario generado */
    @Lob
    @Column(name = "xml_content", columnDefinition = "TEXT")
    private String xmlContent;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

