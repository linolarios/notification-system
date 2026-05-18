package com.challenge.notification.web.controller.controller;

import com.challenge.notification.application.NotificationQueryService;
import com.challenge.notification.dto.response.CategoryResponse;
import com.challenge.notification.web.controller.CategoryController;
import com.challenge.notification.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    @Test
    void shouldReturnActiveCategories() throws Exception {
        // given
        when(notificationQueryService.getActiveCategories())
                .thenReturn(List.of(
                        new CategoryResponse("SPORTS", "Sports"),
                        new CategoryResponse("FINANCE", "Finance"),
                        new CategoryResponse("MOVIES", "Movies")
                ));

        // when / assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SPORTS"))
                .andExpect(jsonPath("$[0].name").value("Sports"))
                .andExpect(jsonPath("$[1].code").value("FINANCE"))
                .andExpect(jsonPath("$[1].name").value("Finance"))
                .andExpect(jsonPath("$[2].code").value("MOVIES"))
                .andExpect(jsonPath("$[2].name").value("Movies"));
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoActiveCategories() throws Exception {
        // given
        when(notificationQueryService.getActiveCategories())
                .thenReturn(List.of());

        // when / assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
