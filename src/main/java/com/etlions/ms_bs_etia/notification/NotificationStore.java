package com.etlions.ms_bs_etia.notification;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * In-memory notification store. For demo purposes — data is lost on restart.
 */
@Component
public class NotificationStore {

    private static final int MAX_SIZE = 100;
    private final Deque<NotificationItem> items = new ConcurrentLinkedDeque<>();

    public void add(NotificationItem item) {
        if (item.getId() == null) item.setId(UUID.randomUUID().toString());
        if (item.getTimestamp() == null) item.setTimestamp(LocalDateTime.now());
        items.addFirst(item);
        // Trim oldest if over limit
        while (items.size() > MAX_SIZE) {
            items.removeLast();
        }
    }

    public List<NotificationItem> getByCompany(String companyId) {
        return items.stream()
                .filter(n -> companyId.equals(n.getCompanyId()))
                .collect(Collectors.toList());
    }

    public long countUnreadByCompany(String companyId) {
        return items.stream()
                .filter(n -> companyId.equals(n.getCompanyId()) && !n.isLeida())
                .count();
    }

    public void markAsRead(String notificationId) {
        items.stream()
                .filter(n -> notificationId.equals(n.getId()))
                .findFirst()
                .ifPresent(n -> n.setLeida(true));
    }

    public void markAllReadByCompany(String companyId) {
        items.stream()
                .filter(n -> companyId.equals(n.getCompanyId()) && !n.isLeida())
                .forEach(n -> n.setLeida(true));
    }

    public void clearByCompany(String companyId) {
        items.removeIf(n -> companyId.equals(n.getCompanyId()));
    }
}

