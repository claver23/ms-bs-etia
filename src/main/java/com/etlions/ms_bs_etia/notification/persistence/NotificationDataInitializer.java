package com.etlions.ms_bs_etia.notification.persistence;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds demo notifications on startup so the notification panel
 * is not empty when the user first opens the application.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDataInitializer {

    private final NotificationStore store;

    @PostConstruct
    void seedDemoNotifications() {
        LocalDateTime now = LocalDateTime.now();

        store.add(NotificationItem.builder()
                .companyId("cmp-demo-001")
                .accion("EMITIDA")
                .tipoDocumento("FACTURA")
                .documentoId("fac-demo-001")
                .serie("F001-00000001")
                .mensaje("Factura F001-00000001 emitida · S/ 1,180.00")
                .timestamp(now.minusHours(2))
                .leida(true)
                .build());

        store.add(NotificationItem.builder()
                .companyId("cmp-demo-001")
                .accion("EMITIDA")
                .tipoDocumento("FACTURA")
                .documentoId("fac-demo-002")
                .serie("F001-00000002")
                .mensaje("Factura F001-00000002 emitida · S/ 3,540.00")
                .timestamp(now.minusHours(1).minusMinutes(30))
                .leida(true)
                .build());

        store.add(NotificationItem.builder()
                .companyId("cmp-demo-001")
                .accion("EMITIDA")
                .tipoDocumento("BOLETA")
                .documentoId("bol-demo-001")
                .serie("B001-00000001")
                .mensaje("Boleta B001-00000001 emitida · S/ 413.00")
                .timestamp(now.minusMinutes(45))
                .build());

        store.add(NotificationItem.builder()
                .companyId("cmp-demo-001")
                .accion("EMITIDA")
                .tipoDocumento("FACTURA")
                .documentoId("fac-demo-003")
                .serie("F001-00000003")
                .mensaje("Factura F001-00000003 emitida · S/ 590.00")
                .timestamp(now.minusMinutes(20))
                .build());

        store.add(NotificationItem.builder()
                .companyId("cmp-demo-001")
                .accion("ANULADA")
                .tipoDocumento("FACTURA")
                .documentoId("fac-demo-006")
                .serie("F001-00000004")
                .mensaje("Factura F001-00000004 anulada por el usuario")
                .timestamp(now.minusMinutes(10))
                .build());

        log.info("📬 NotificationDataInitializer: 5 notificaciones demo cargadas");
    }
}

