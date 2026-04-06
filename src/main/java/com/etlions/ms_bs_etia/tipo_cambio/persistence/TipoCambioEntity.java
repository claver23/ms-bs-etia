package com.etlions.ms_bs_etia.tipo_cambio.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_cambio")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class TipoCambioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDate fecha;

    @Column(name = "moneda_origen", length = 3)
    private String monedaOrigen; // e.g. USD

    @Column(name = "moneda_destino", length = 3)
    private String monedaDestino; // e.g. PEN

    @Column(name = "tasa_compra", precision = 10, scale = 4)
    private BigDecimal tasaCompra;

    @Column(name = "tasa_venta", precision = 10, scale = 4)
    private BigDecimal tasaVenta;

    @Column(length = 10)
    private String fuente; // SBS or SUNAT

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

