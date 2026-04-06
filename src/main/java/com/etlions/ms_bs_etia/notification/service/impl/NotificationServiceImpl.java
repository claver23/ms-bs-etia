package com.etlions.ms_bs_etia.notification.service.impl;

import com.etlions.ms_bs_etia.notification.persistence.NotificationItem;
import com.etlions.ms_bs_etia.notification.persistence.NotificationStore;
import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Default implementation – manages the in-memory NotificationStore.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationStore store;

    @Override
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

    @Override
    public SuccessResponse publicarNotificacion(NotificationPublishDto request) {
        publicar(
                request.getCompanyId(),
                request.getAccion() != null ? request.getAccion().getValue() : null,
                request.getTipoDocumento() != null ? request.getTipoDocumento().getValue() : null,
                request.getDocumentoId(),
                request.getSerie(),
                request.getMensaje()
        );
        return new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Notificación publicada exitosamente")
                .timestamp(new Date());
    }

    @Override
    public NotificationListResponseDto listarNotificaciones(String companyId) {
        List<NotificationItem> items = store.getByCompany(companyId);
        long unread = store.countUnreadByCompany(companyId);

        List<NotificationData> notifications = items.stream().map(this::toDto).toList();

        NotificationListData listData = new NotificationListData()
                .unreadCount(unread)
                .notifications(notifications);

        return new NotificationListResponseDto()
                .isSuccess(true).status("OK")
                .message("Operación exitosa")
                .timestamp(new Date())
                .data(listData);
    }

    @Override
    public SuccessResponse marcarTodasLeidas(String companyId) {
        store.markAllReadByCompany(companyId);
        return new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Todas marcadas como leídas")
                .timestamp(new Date());
    }

    @Override
    public SuccessResponse marcarNotificacionLeida(String notificationId) {
        store.markAsRead(notificationId);
        return new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Notificación marcada como leída")
                .timestamp(new Date());
    }

    @Override
    public SuccessResponse limpiarNotificaciones(String companyId) {
        store.clearByCompany(companyId);
        return new SuccessResponse()
                .isSuccess(true).status("OK")
                .message("Notificaciones limpiadas")
                .timestamp(new Date());
    }

    private NotificationData toDto(NotificationItem n) {
        return new NotificationData()
                .id(n.getId())
                .companyId(n.getCompanyId())
                .accion(n.getAccion() != null
                        ? NotificationData.AccionEnum.fromValue(n.getAccion()) : null)
                .tipoDocumento(n.getTipoDocumento() != null
                        ? NotificationData.TipoDocumentoEnum.fromValue(n.getTipoDocumento()) : null)
                .documentoId(n.getDocumentoId())
                .serie(n.getSerie())
                .mensaje(n.getMensaje())
                .timestamp(n.getTimestamp() != null
                        ? Timestamp.valueOf(n.getTimestamp()).getTime() : null)
                .leida(n.isLeida());
    }
}
