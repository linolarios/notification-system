package com.challenge.notification.dto.response;

public record FieldErrorResponse(
        String field,
        String message
) {
}
