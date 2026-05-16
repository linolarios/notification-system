package com.challenge.notification.web.controller;

import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.CategoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final NotificationQueryService notificationQueryService;

    public CategoryController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/api/categories")
    public List<CategoryResponse> getCategories() {
        return notificationQueryService.getActiveCategories();
    }
}
