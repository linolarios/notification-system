package com.challenge.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(

        @NotNull(message = "category is required")
        String category,

        @NotBlank(message = "message must not be blank")
        @Size(max = 1000, message = "message must not exceed 1000 characters")
        String message
) {
}
