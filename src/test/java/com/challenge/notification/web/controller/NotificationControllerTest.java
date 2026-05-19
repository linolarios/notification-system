package com.challenge.notification.web.controller;

import com.challenge.notification.application.NotificationCommandService;
import com.challenge.notification.dto.response.NotificationAcceptedResponse;
import com.challenge.notification.web.controller.NotificationController;
import com.challenge.notification.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationCommandService notificationCommandService;

    @Test
    void shouldReturnAcceptedWhenRequestIsValid() throws Exception {
        // given
        when(notificationCommandService.createNotification(any()))
                .thenReturn(NotificationAcceptedResponse.accepted(
                        "controller-test-correlation",
                        100L,
                        200L
                ));

        String requestBody = """
                {
                  "category": "SPORTS",
                  "message": "Game starts tonight"
                }
                """;

        // when / assert
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.correlationId").value("controller-test-correlation"))
                .andExpect(jsonPath("$.messageId").value(100))
                .andExpect(jsonPath("$.jobId").value(200))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void shouldReturnBadRequestWhenMessageIsBlank() throws Exception {
        // given
        String requestBody = """
                {
                  "category": "SPORTS",
                  "message": ""
                }
                """;

        // when / assert
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("message"));
    }
}
