package com.challenge.notification.web.exception;

import com.challenge.notification.domain.exception.CategoryNotFoundException;
import com.challenge.notification.domain.exception.NotificationJobProcessingException;
import com.challenge.notification.domain.exception.NotificationQueueException;
import com.challenge.notification.domain.exception.NotificationSendingException;
import com.challenge.notification.domain.exception.UnsupportedNotificationChannelException;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/notifications");
    }

    @Test
    void shouldHandleCategoryNotFoundAsBadRequest() {
        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleCategoryNotFoundException(
                new CategoryNotFoundException("Unknown category: UNKNOWN"),
                request
        );

        // assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).contains("Unknown category: UNKNOWN");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("category");
    }

    @Test
    void shouldHandleUnsupportedNotificationChannelAsBadRequest() {
        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleUnsupportedNotificationChannelException(
                new UnsupportedNotificationChannelException("WHATSAPP"),
                request
        );

        // assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Unsupported notification channel: WHATSAPP");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
    }

    @Test
    void shouldHandleQueueFailureAsServiceUnavailable() {
        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleNotificationQueueException(
                new NotificationQueueException("Queue is full"),
                request
        );

        // assert
        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(503);
        assertThat(response.getBody().error()).isEqualTo("Service Unavailable");
        assertThat(response.getBody().message()).isEqualTo("Queue is full");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
    }

    @Test
    void shouldHandleNotificationSendingFailureAsInternalServerError() {
        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleNotificationProcessingException(
                new NotificationSendingException(NotificationChannelCode.EMAIL,"Provider failed"),
                request
        );

        // assert
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).contains("Provider failed");
        assertThat(response.getBody().message()).contains("channel EMAIL");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
    }

    @Test
    void shouldHandleNotificationJobProcessingFailureAsInternalServerError() {
        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleNotificationProcessingException(
                new NotificationJobProcessingException("Job processing failed"),
                request
        );

        // assert
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("Job processing failed");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
    }

    @Test
    void shouldHandleIllegalArgumentAsBadRequest() {
        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Invalid argument"),
                request
        );

        // assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid argument");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
    }

    @Test
    void shouldHandleUnexpectedExceptionAsInternalServerErrorWithoutLeakingMessage() {
        // when
        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(
                new RuntimeException("Sensitive failure detail"),
                request
        );

        // assert
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("Unexpected internal server error");
        assertThat(response.getBody().path()).isEqualTo("/api/notifications");
    }
}
