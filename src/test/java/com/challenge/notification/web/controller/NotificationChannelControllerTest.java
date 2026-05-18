package com.challenge.notification.web.controller.controller;

import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.NotificationChannelResponse;
import com.challenge.notification.web.controller.NotificationChannelController;
import com.challenge.notification.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationChannelController.class)
@Import(GlobalExceptionHandler.class)
class NotificationChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    @Test
    void shouldReturnActiveNotificationChannels() throws Exception {
        // given
        when(notificationQueryService.getActiveNotificationChannels())
                .thenReturn(List.of(
                        new NotificationChannelResponse("EMAIL", "Email"),
                        new NotificationChannelResponse("SMS", "SMS")
                ));

        // when / assert
        mockMvc.perform(get("/api/notification-channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("EMAIL"))
                .andExpect(jsonPath("$[1].code").value("SMS"));
    }
}

