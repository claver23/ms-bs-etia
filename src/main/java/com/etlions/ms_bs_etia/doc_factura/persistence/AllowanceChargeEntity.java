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

/**
 * Cargo/Descuento global UBL 2.1 — mapea a &lt;cac:AllowanceCharge&gt;
 * Catálogo 53 SUNAT: 00=Descuentos globales, 02=Descuentos ítem,
 *                    45=FISE, 46=Recargo al consumo, 47=Cargos/consumo, 50=Percepción, etc.
 */
@Entity
@Table(name = "allowance_charges")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllowanceChargeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** FK a facturas.id (nullable si pertenece a boleta) */
    @Column(name = "factura_id")
    private String facturaId;

    /** FK a boletas.id (nullable si pertenece a factura) */
    @Column(name = "boleta_id")
    private String boletaId;

    /** true = Cargo (surcharge), false = Descuento (allowance) */
    @Column(name = "charge_indicator", nullable = false)
    private Boolean chargeIndicator;

    /** Catálogo 53 SUNAT: "00", "02", "45", "46", "47", "50", etc. */
    @Column(name = "reason_code", length = 4)
    private String reasonCode;

    /** Descripción del cargo/descuento */
    @Column(name = "reason_description", length = 250)
    private String reasonDescription;

    /** Factor multiplicador (ej. 0.10 para 10%) */
    @Column(name = "multiplier_factor", precision = 8, scale = 4)
    private BigDecimal multiplierFactor;

    /** Monto del cargo/descuento */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    /** Monto base sobre el que se calcula */
    @Column(name = "base_amount", precision = 12, scale = 2)
    private BigDecimal baseAmount;

    /** Moneda (PEN, USD) */
    @Column(length = 3)
    private String moneda;
}

