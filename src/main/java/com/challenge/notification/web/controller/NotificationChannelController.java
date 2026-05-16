package com.challenge.notification.web.controller;

import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.NotificationChannelResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificationChannelController {

    private final NotificationQueryService notificationQueryService;

    public NotificationChannelController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/api/notification-channels")
    public List<NotificationChannelResponse> getNotificationChannels() {
        return notificationQueryService.getActiveNotificationChannels();
    }
}
