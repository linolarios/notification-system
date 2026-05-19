package com.challenge.notification.web.controller;
import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.NotificationJobResponse;
import com.challenge.notification.web.controller.NotificationJobController;
import com.challenge.notification.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationJobController.class)
@Import(GlobalExceptionHandler.class)
class NotificationJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    @Test
    void shouldReturnJobById() throws Exception {
        // given
        when(notificationQueryService.getNotificationJob(200L))
                .thenReturn(jobResponse());

        // when / assert
        mockMvc.perform(get("/api/notification-jobs/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correlationId").value("job-controller-correlation"))
                .andExpect(jsonPath("$.jobId").value(200))
                .andExpect(jsonPath("$.messageId").value(100))
                .andExpect(jsonPath("$.status").value("PROCESSED"));
    }

    @Test
    void shouldReturnJobByCorrelationId() throws Exception {
        // given
        when(notificationQueryService.getNotificationJobByCorrelationId("job-controller-correlation"))
                .thenReturn(jobResponse());

        // when / assert
        mockMvc.perform(get("/api/notification-jobs")
                        .param("correlationId", "job-controller-correlation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correlationId").value("job-controller-correlation"))
                .andExpect(jsonPath("$.jobId").value(200));
    }

    private static NotificationJobResponse jobResponse() {
        return new NotificationJobResponse(
                "job-controller-correlation",
                200L,
                100L,
                "PROCESSED",
                1,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
