package com.challenge.notification.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationPageController {

    @GetMapping("/notifications")
    public String notificationsPage() {
        return "notifications";
    }
}
