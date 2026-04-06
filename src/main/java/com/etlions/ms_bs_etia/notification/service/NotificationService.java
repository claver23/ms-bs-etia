package com.etlions.ms_bs_etia.notification.service;

import com.etlions.openapicodegen.dto.NotificationListResponseDto;
import com.etlions.openapicodegen.dto.NotificationPublishDto;
import com.etlions.openapicodegen.dto.SuccessResponse;

/**
 * Service used by document modules and the REST controller
 * to manage transmission notifications.
 */
public interface NotificationService {

    /** Publicar notificación (uso interno desde servicios de documentos) */
    void publicar(String companyId, String accion, String tipoDocumento,
                  String documentoId, String serie, String mensaje);

    /** Publicar notificación (uso REST) */
    SuccessResponse publicarNotificacion(NotificationPublishDto request);

    /** Listar notificaciones de una empresa */
    NotificationListResponseDto listarNotificaciones(String companyId);

    /** Marcar todas las notificaciones de una empresa como leídas */
    SuccessResponse marcarTodasLeidas(String companyId);

    /** Marcar una notificación individual como leída */
    SuccessResponse marcarNotificacionLeida(String notificationId);

    /** Limpiar (eliminar) todas las notificaciones de una empresa */
    SuccessResponse limpiarNotificaciones(String companyId);
}
