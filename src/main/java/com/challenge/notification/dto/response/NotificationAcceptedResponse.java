package com.challenge.notification.dto.response;

public record NotificationAcceptedResponse(
        String correlationId,
        Long messageId,
        Long jobId,
        String status,
        String detail
) {
    public static NotificationAcceptedResponse accepted(String correlationId, Long messageId, Long jobId) {
        return new NotificationAcceptedResponse(
                correlationId,
                messageId,
                jobId,
                "ACCEPTED",
                "Notification job accepted for background processing."
        );
    }
}
