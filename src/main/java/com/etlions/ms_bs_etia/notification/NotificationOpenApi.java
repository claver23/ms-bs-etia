package com.etlions.ms_bs_etia.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for the in-memory notification system.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationOpenApi {

    private final NotificationStore store;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(@RequestParam String companyId) {
        List<NotificationItem> items = store.getByCompany(companyId);
        long unread = store.countUnreadByCompany(companyId);
        List<Map<String, Object>> data = items.stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "isSuccess", true,
                "unreadCount", unread,
                "notifications", data
        ));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> marcarTodasLeidas(@RequestParam String companyId) {
        store.markAllReadByCompany(companyId);
        return ResponseEntity.ok(Map.of("isSuccess", true, "message", "Todas marcadas como leídas"));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> marcarLeida(@PathVariable String id) {
        store.markAsRead(id);
        return ResponseEntity.ok(Map.of("isSuccess", true));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> limpiar(@RequestParam String companyId) {
        store.clearByCompany(companyId);
        return ResponseEntity.ok(Map.of("isSuccess", true, "message", "Notificaciones limpiadas"));
    }

    private Map<String, Object> toMap(NotificationItem n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("companyId", n.getCompanyId());
        m.put("accion", n.getAccion());
        m.put("tipoDocumento", n.getTipoDocumento());
        m.put("documentoId", n.getDocumentoId());
        m.put("serie", n.getSerie());
        m.put("mensaje", n.getMensaje());
        m.put("timestamp", n.getTimestamp() != null ? Timestamp.valueOf(n.getTimestamp()).getTime() : null);
        m.put("leida", n.isLeida());
        return m;
    }
}

