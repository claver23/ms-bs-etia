package com.etlions.ms_bs_etia.doc_notacred.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notas_credito")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotaCreditoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "factura_referencia_id")
    private String facturaReferenciaId;

    private String motivo;

    @Column(name = "monto_descuento")
    private BigDecimal montoDescuento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    private String descripcion;

    private String estado;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String detalleJson;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

