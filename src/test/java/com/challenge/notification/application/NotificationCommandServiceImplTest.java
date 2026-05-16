package com.challenge.notification.application;

import com.challenge.notification.config.CorrelationConstants;
import com.challenge.notification.domain.exception.CategoryNotFoundException;
import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.port.CategoryRepositoryPort;
import com.challenge.notification.domain.port.MessageRepositoryPort;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.dto.request.CreateNotificationRequest;
import com.challenge.notification.dto.response.NotificationAcceptedResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @Mock
    private MessageRepositoryPort messageRepositoryPort;

    @Mock
    private NotificationJobRepositoryPort notificationJobRepositoryPort;

    private NotificationCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationCommandServiceImpl(
                categoryRepositoryPort,
                messageRepositoryPort,
                notificationJobRepositoryPort
        );

        MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, "command-test-correlation");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldCreateMessageAndPendingJob() {
        CreateNotificationRequest request =
                new CreateNotificationRequest(
                "SPORTS",
                "Game starts tonight"
        );

        when(categoryRepositoryPort.existsActiveByCode(CategoryCode.SPORTS))
                .thenReturn(true);

        when(messageRepositoryPort.save(any(NotificationMessage.class)))
                .thenAnswer(invocation -> {
                    NotificationMessage message = invocation.getArgument(0);

                    return new NotificationMessage(
                            100L,
                            message.getCorrelationId(),
                            message.getCategory(),
                            message.getBody(),
                            message.getCreatedAt()
                    );
                });

        when(notificationJobRepositoryPort.save(any(NotificationJob.class)))
                .thenAnswer(invocation -> {
                    NotificationJob job = invocation.getArgument(0);

                    return NotificationJob.builder()
                            .id(200L)
                            .correlationId(job.getCorrelationId())
                            .messageId(job.getMessageId())
                            .category(job.getCategory())
                            .status(job.getStatus())
                            .attemptCount(job.getAttemptCount())
                            .createdAt(job.getCreatedAt())
                            .updatedAt(LocalDateTime.now())
                            .build();
                });

        NotificationAcceptedResponse response = service.createNotification(request);

        assertThat(response.correlationId()).isEqualTo("command-test-correlation");
        assertThat(response.messageId()).isEqualTo(100L);
        assertThat(response.jobId()).isEqualTo(200L);
        assertThat(response.status()).isEqualTo("ACCEPTED");

        ArgumentCaptor<NotificationMessage> messageCaptor =
                ArgumentCaptor.forClass(NotificationMessage.class);

        verify(messageRepositoryPort).save(messageCaptor.capture());

        assertThat(messageCaptor.getValue().getCorrelationId()).isEqualTo("command-test-correlation");
        assertThat(messageCaptor.getValue().getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(messageCaptor.getValue().getBody()).isEqualTo("Game starts tonight");
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "SPORTS",
                "Message"
        );

        when(categoryRepositoryPort.existsActiveByCode(CategoryCode.SPORTS))
                .thenReturn(false);

        assertThatThrownBy(() -> service.createNotification(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("SPORTS");

        verifyNoInteractions(messageRepositoryPort, notificationJobRepositoryPort);
    }

    @Test
    void shouldThrowWhenCategoryCodeIsInvalid() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                "UNKNOWN",
                "Message"
        );

        assertThatThrownBy(() -> service.createNotification(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("UNKNOWN");

        verifyNoInteractions(categoryRepositoryPort, messageRepositoryPort, notificationJobRepositoryPort);
    }
}
