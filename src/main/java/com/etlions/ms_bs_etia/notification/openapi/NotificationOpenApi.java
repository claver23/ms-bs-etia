package com.etlions.ms_bs_etia.notification.openapi;

import com.etlions.ms_bs_etia.notification.service.NotificationService;
import com.etlions.openapicodegen.api.v1.NotificationApi;
import com.etlions.openapicodegen.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationOpenApi implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<NotificationListResponseDto> listarNotificaciones(String companyId) {
        return ResponseEntity.ok(notificationService.listarNotificaciones(companyId));
    }

    @Override
    public ResponseEntity<SuccessResponse> publicarNotificacion(NotificationPublishDto notificationPublishDto) {
        return ResponseEntity.ok(notificationService.publicarNotificacion(notificationPublishDto));
    }

    @Override
    public ResponseEntity<SuccessResponse> marcarTodasLeidas(String companyId) {
        return ResponseEntity.ok(notificationService.marcarTodasLeidas(companyId));
    }

    @Override
    public ResponseEntity<SuccessResponse> marcarNotificacionLeida(String notificationId) {
        return ResponseEntity.ok(notificationService.marcarNotificacionLeida(notificationId));
    }

    @Override
    public ResponseEntity<SuccessResponse> limpiarNotificaciones(String companyId) {
        return ResponseEntity.ok(notificationService.limpiarNotificaciones(companyId));
    }
}
