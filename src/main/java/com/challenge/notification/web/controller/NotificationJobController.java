package com.challenge.notification.web.controller;

import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.NotificationJobResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationJobController {

    private final NotificationQueryService notificationQueryService;

    public NotificationJobController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/api/notification-jobs/{jobId}")
    public NotificationJobResponse getNotificationJob(@PathVariable Long jobId) {
        return notificationQueryService.getNotificationJob(jobId);
    }

    @GetMapping("/api/notification-jobs")
    public NotificationJobResponse getNotificationJobByCorrelationId(@RequestParam String correlationId) {
        return notificationQueryService.getNotificationJobByCorrelationId(correlationId);
    }
}
