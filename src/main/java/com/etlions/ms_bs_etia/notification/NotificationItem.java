package com.etlions.ms_bs_etia.notification;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class NotificationItem {
    private String id;
    private String companyId;
    private String accion;          // EMITIDA, ANULADA, ELIMINADA
    private String tipoDocumento;   // FACTURA, BOLETA, NOTA_CREDITO, NOTA_DEBITO
    private String documentoId;
    private String serie;
    private String mensaje;
    private LocalDateTime timestamp;
    @Builder.Default
    private boolean leida = false;
}

