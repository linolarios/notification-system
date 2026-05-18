package com.challenge.notification.application;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.dto.response.CategoryResponse;
import com.challenge.notification.dto.response.NotificationChannelResponse;
import com.challenge.notification.dto.response.NotificationJobResponse;
import com.challenge.notification.dto.response.NotificationLogResponse;
import com.challenge.notification.dto.response.PagedResponse;
import com.challenge.notification.dto.response.mapper.NotificationLogResponseMapper;
import com.challenge.notification.infrastructure.cache.CategoryCacheService;
import com.challenge.notification.infrastructure.cache.NotificationChannelCacheService;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationJobRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationLogRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceImplTest {

    @Mock
    private SpringDataNotificationLogRepository notificationLogRepository;

    @Mock
    private SpringDataNotificationJobRepository notificationJobRepository;

    @Mock
    private CategoryCacheService categoryCacheService;

    @Mock
    private NotificationChannelCacheService notificationChannelCacheService;

    @Mock
    private NotificationLogResponseMapper logResponseMapper;

    private NotificationQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        queryService = new NotificationQueryServiceImpl(
                notificationLogRepository,
                notificationJobRepository,
                categoryCacheService,
                notificationChannelCacheService,
                logResponseMapper
        );
    }

    @Test
    void shouldReturnPagedNotificationLogs() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 20);
        NotificationLogEntity logEntity = getLogEntity();
        NotificationLogResponse logResponse = logResponse();

        when(notificationLogRepository.findAllByOrderByCreatedAtDesc(pageRequest))
                .thenReturn(new PageImpl<>(List.of(logEntity), pageRequest, 1));

        when(logResponseMapper.toNotificationLogResponse(logEntity))
                .thenReturn(logResponse);

        // when
        PagedResponse<NotificationLogResponse> response =
                queryService.getNotificationLogs(pageRequest);

        // assert
        assertThat(response.content()).containsExactly(logResponse);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnPagedNotificationLogsFilteredByCorrelationId() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 20);
        NotificationLogEntity logEntity = getLogEntity();
        NotificationLogResponse logResponse = logResponse();

        when(notificationLogRepository.findByCorrelationIdOrderByCreatedAtDesc(
                "query-test-correlation",
                pageRequest
        )).thenReturn(new PageImpl<>(List.of(logEntity), pageRequest, 1));

        when(logResponseMapper.toNotificationLogResponse(logEntity))
                .thenReturn(logResponse);

        // when
        PagedResponse<NotificationLogResponse> response =
                queryService.getNotificationLogsByCorrelationId(
                        "query-test-correlation",
                        pageRequest
                );

        // assert
        assertThat(response.content()).containsExactly(logResponse);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    private static @NonNull NotificationLogEntity getLogEntity() {
        CategoryEntity categoryEntity = new CategoryEntity((short) 1, CategoryCode.SPORTS.name(), "Sports", true);
        NotificationChannelEntity notificationChannelEntity = new NotificationChannelEntity((short) 1, "EMAIL", "Email", true);
        return new NotificationLogEntity(
                1L,
                "query-test-correlation",
                100L,
                10L,
                categoryEntity,
                notificationChannelEntity,
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
    void shouldReturnJobById() {
        // given
        NotificationJobEntity entity = notificationJobEntity();

        when(notificationJobRepository.findById(10L))
                .thenReturn(Optional.of(entity));

        // when
        NotificationJobResponse response = queryService.getNotificationJob(10L);

        // assert
        assertThat(response.correlationId()).isEqualTo("query-test-correlation");
        assertThat(response.jobId()).isEqualTo(10L);
        assertThat(response.messageId()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo("PROCESSED");
    }

    @Test
    void shouldThrowWhenJobByIdDoesNotExist() {
        // given
        when(notificationJobRepository.findById(10L))
                .thenReturn(Optional.empty());

        // when / assert
        assertThatThrownBy(() -> queryService.getNotificationJob(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification job not found");
    }

    @Test
    void shouldReturnJobByCorrelationId() {
        // given
        NotificationJobEntity entity = notificationJobEntity();

        when(notificationJobRepository.findByCorrelationId("query-test-correlation"))
                .thenReturn(Optional.of(entity));

        // when
        NotificationJobResponse response =
                queryService.getNotificationJobByCorrelationId("query-test-correlation");

        // assert
        assertThat(response.correlationId()).isEqualTo("query-test-correlation");
        assertThat(response.jobId()).isEqualTo(10L);
    }

    @Test
    void shouldReturnActiveCategoriesFromCache() {
        // given
        CategoryEntity sports = new CategoryEntity((short) 1, "SPORTS", "Sports", true);
        CategoryEntity movies = new CategoryEntity((short) 2, "MOVIES", "Movies", true);

        when(categoryCacheService.getActiveCategories())
                .thenReturn(List.of(movies, sports));

        // when
        List<CategoryResponse> response = queryService.getActiveCategories();

        // assert
        assertThat(response)
                .extracting(CategoryResponse::code)
                .containsExactly("MOVIES", "SPORTS");
    }

    @Test
    void shouldReturnActiveNotificationChannelsFromCache() {
        // given
        NotificationChannelEntity email =
                new NotificationChannelEntity((short) 1, "EMAIL", "Email", true);

        NotificationChannelEntity sms =
                new NotificationChannelEntity((short) 2, "SMS", "SMS", true);

        when(notificationChannelCacheService.getActiveNotificationChannels())
                .thenReturn(List.of(email, sms));

        // when
        List<NotificationChannelResponse> response =
                queryService.getActiveNotificationChannels();

        // assert
        assertThat(response)
                .extracting(NotificationChannelResponse::code)
                .containsExactly("EMAIL", "SMS");
    }

    private static NotificationLogResponse logResponse() {
        return new NotificationLogResponse(
                1L,
                "query-test-correlation",
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

    private static NotificationJobEntity notificationJobEntity() {
        return new NotificationJobEntity(
                10L,
                "query-test-correlation",
                100L,
                null,
                "PROCESSED",
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
