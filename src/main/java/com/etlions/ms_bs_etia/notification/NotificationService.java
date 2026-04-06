package com.etlions.ms_bs_etia.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service used by document modules to publish transmission notifications.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationStore store;

    public void publicar(String companyId, String accion, String tipoDocumento,
                         String documentoId, String serie, String mensaje) {
        store.add(NotificationItem.builder()
                .companyId(companyId)
                .accion(accion)
                .tipoDocumento(tipoDocumento)
                .documentoId(documentoId)
                .serie(serie)
                .mensaje(mensaje)
                .build());
    }
}

