package com.challenge.notification.web.controller;

import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.NotificationLogResponse;
import com.challenge.notification.dto.response.PagedResponse;
import com.challenge.notification.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationLogController.class)
@Import(GlobalExceptionHandler.class)
class NotificationLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    private static PagedResponse<NotificationLogResponse> pagedResponse() {
        return new PagedResponse<>(
                List.of(logResponse()),
                0,
                20,
                1,
                1,
                true,
                true
        );
    }

    private static NotificationLogResponse logResponse() {
        return new NotificationLogResponse(
                1L,
                "log-controller-correlation",
                100L,
                10L,
                "SPORTS",
                "EMAIL",
                "Alice",
                "alice@example.com",
                "+15550000001",
                "SENT",
                null,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void shouldReturnLogs() throws Exception {
        // given
        PageRequest pageRequest = PageRequest.of(
                0,
                20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        when(notificationQueryService.searchNotificationLogs(
                isNull(),
                isNull(),
                eq(pageRequest)
        )).thenReturn(pagedResponse());

        // when / assert
        mockMvc.perform(get("/api/notification-logs")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].correlationId").value("log-controller-correlation"))
                .andExpect(jsonPath("$.content[0].status").value("SENT"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnLogsByCorrelationId() throws Exception {
        // given
        PageRequest pageRequest = PageRequest.of(
                0,
                20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        when(notificationQueryService.searchNotificationLogs(
                eq("log-controller-correlation"),
                isNull(),
                eq(pageRequest)
        )).thenReturn(pagedResponse());

        // when / assert
        mockMvc.perform(get("/api/notification-logs")
                        .param("page", "0")
                        .param("size", "20")
                        .param("correlationId", "log-controller-correlation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].correlationId").value("log-controller-correlation"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnLogsByCorrelationIdAndCategorySortedAscending() throws Exception {
        // given
        PageRequest pageRequest = PageRequest.of(
                0,
                20,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );

        when(notificationQueryService.searchNotificationLogs(
                eq("log-controller-correlation"),
                eq("SPORTS"),
                eq(pageRequest)
        )).thenReturn(pagedResponse());

        // when / assert
        mockMvc.perform(get("/api/notification-logs")
                        .param("page", "0")
                        .param("size", "20")
                        .param("correlationId", "log-controller-correlation")
                        .param("category", "SPORTS")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].correlationId").value("log-controller-correlation"))
                .andExpect(jsonPath("$.content[0].category").value("SPORTS"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
