package com.challenge.notification.web.controller;

import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.NotificationLogResponse;
import com.challenge.notification.dto.response.PagedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationLogController {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationQueryService notificationQueryService;

    public NotificationLogController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/api/notification-logs")
    public PagedResponse<NotificationLogResponse> getNotificationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String correlationId
    ) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        PageRequest pageRequest = PageRequest.of(sanitizedPage, sanitizedSize);

        if (correlationId != null && !correlationId.isBlank()) {
            return notificationQueryService.getNotificationLogsByCorrelationId(
                    correlationId,
                    pageRequest
            );
        }

        return notificationQueryService.getNotificationLogs(pageRequest);
    }
}
