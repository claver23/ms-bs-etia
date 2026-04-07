package com.etlions.ms_bs_etia.doc_factura.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cuota de crédito UBL 2.1 — mapea a &lt;cac:PaymentTerms&gt; con Cuota001..N
 * Relación polimórfica: puede pertenecer a una factura o una boleta.
 */
@Entity
@Table(name = "cuotas_credito")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CuotaCreditoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** FK a facturas.id (nullable si pertenece a boleta) */
    @Column(name = "factura_id")
    private String facturaId;

    /** FK a boletas.id (nullable si pertenece a factura) */
    @Column(name = "boleta_id")
    private String boletaId;

    /** Número de cuota: "Cuota001", "Cuota002", etc. */
    @Column(name = "numero_cuota", nullable = false, length = 20)
    private String numeroCuota;

    /** Monto de la cuota */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Fecha de vencimiento de la cuota */
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    /** Moneda de la cuota (PEN, USD) */
    @Column(length = 3)
    private String moneda;
}

