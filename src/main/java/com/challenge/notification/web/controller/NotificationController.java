package com.challenge.notification.web.controller;

import com.challenge.notification.application.NotificationCommandService;
import com.challenge.notification.dto.request.CreateNotificationRequest;
import com.challenge.notification.dto.response.NotificationAcceptedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private final NotificationCommandService notificationCommandService;

    public NotificationController(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    @PostMapping("/api/notifications")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NotificationAcceptedResponse createNotification(
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        return notificationCommandService.createNotification(request);
    }
}
