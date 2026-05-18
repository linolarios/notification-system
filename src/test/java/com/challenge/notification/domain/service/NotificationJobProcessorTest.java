package com.challenge.notification.domain.service.service2;

import com.challenge.notification.domain.model.*;
import com.challenge.notification.domain.port.MessageRepositoryPort;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.domain.port.NotificationSubscriberRepositoryPort;
import com.challenge.notification.domain.service.NotificationDispatcher;
import com.challenge.notification.domain.service.NotificationJobProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationJobProcessorTest {

    @Mock
    private NotificationJobRepositoryPort notificationJobRepositoryPort;

    @Mock
    private MessageRepositoryPort messageRepositoryPort;

    @Mock
    private NotificationSubscriberRepositoryPort subscriberRepositoryPort;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    private NotificationJobProcessor notificationJobProcessor;

    @BeforeEach
    void setUp() {
        notificationJobProcessor = new NotificationJobProcessor(
                notificationJobRepositoryPort,
                messageRepositoryPort,
                subscriberRepositoryPort,
                notificationDispatcher
        );
    }

    @Test
    void shouldMarkJobProcessingThenProcessedWhenProcessingSucceeds() {
        // given
        NotificationJob pendingJob = pendingJob();
        NotificationMessage message = message();
        List<NotificationSubscriber> subscribers = List.of(subscriber());

        when(notificationJobRepositoryPort.save(any(NotificationJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(messageRepositoryPort.findById(pendingJob.getMessageId()))
                .thenReturn(Optional.of(message));

        when(subscriberRepositoryPort.findActiveSubscribersByCategory(CategoryCode.SPORTS))
                .thenReturn(subscribers);

        // when
        notificationJobProcessor.process(pendingJob);

        // assert
        ArgumentCaptor<NotificationJob> jobCaptor = ArgumentCaptor.forClass(NotificationJob.class);

        verify(notificationJobRepositoryPort, org.mockito.Mockito.times(2))
                .save(jobCaptor.capture());

        List<NotificationJob> savedJobs = jobCaptor.getAllValues();

        assertThat(savedJobs.get(0).getStatus()).isEqualTo(NotificationJobStatus.PROCESSING);
        assertThat(savedJobs.get(0).getAttemptCount()).isEqualTo(1);
        assertThat(savedJobs.get(0).getLockedAt()).isNotNull();

        assertThat(savedJobs.get(1).getStatus()).isEqualTo(NotificationJobStatus.PROCESSED);
        assertThat(savedJobs.get(1).getProcessedAt()).isNotNull();
        assertThat(savedJobs.get(1).getLastError()).isNull();

        verify(notificationDispatcher).dispatch(message, subscribers);
    }

    @Test
    void shouldMarkJobProcessedWhenThereAreNoSubscribers() {
        // given
        NotificationJob pendingJob = pendingJob();
        NotificationMessage message = message();

        when(notificationJobRepositoryPort.save(any(NotificationJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(messageRepositoryPort.findById(pendingJob.getMessageId()))
                .thenReturn(Optional.of(message));

        when(subscriberRepositoryPort.findActiveSubscribersByCategory(CategoryCode.SPORTS))
                .thenReturn(List.of());

        // when
        notificationJobProcessor.process(pendingJob);

        // assert
        ArgumentCaptor<NotificationJob> jobCaptor = ArgumentCaptor.forClass(NotificationJob.class);

        verify(notificationJobRepositoryPort, org.mockito.Mockito.times(2))
                .save(jobCaptor.capture());

        List<NotificationJob> savedJobs = jobCaptor.getAllValues();

        assertThat(savedJobs.get(0).getStatus()).isEqualTo(NotificationJobStatus.PROCESSING);
        assertThat(savedJobs.get(1).getStatus()).isEqualTo(NotificationJobStatus.PROCESSED);

        verify(notificationDispatcher).dispatch(message, List.of());
    }

    @Test
    void shouldMarkJobFailedWhenMessageDoesNotExist() {
        // given
        NotificationJob pendingJob = pendingJob();

        when(notificationJobRepositoryPort.save(any(NotificationJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(messageRepositoryPort.findById(pendingJob.getMessageId()))
                .thenReturn(Optional.empty());

        // when
        notificationJobProcessor.process(pendingJob);

        // assert
        ArgumentCaptor<NotificationJob> jobCaptor = ArgumentCaptor.forClass(NotificationJob.class);

        verify(notificationJobRepositoryPort, org.mockito.Mockito.times(2))
                .save(jobCaptor.capture());

        List<NotificationJob> savedJobs = jobCaptor.getAllValues();

        assertThat(savedJobs.get(0).getStatus()).isEqualTo(NotificationJobStatus.PROCESSING);

        assertThat(savedJobs.get(1).getStatus()).isEqualTo(NotificationJobStatus.FAILED);
        assertThat(savedJobs.get(1).getLastError()).contains("Message not found");
        assertThat(savedJobs.get(1).getProcessedAt()).isNotNull();

        verify(notificationDispatcher, never()).dispatch(any(), any());
    }

    @Test
    void shouldMarkJobFailedWhenDispatcherThrowsException() {
        // given
        NotificationJob pendingJob = pendingJob();
        NotificationMessage message = message();
        List<NotificationSubscriber> subscribers = List.of(subscriber());

        when(notificationJobRepositoryPort.save(any(NotificationJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(messageRepositoryPort.findById(pendingJob.getMessageId()))
                .thenReturn(Optional.of(message));

        when(subscriberRepositoryPort.findActiveSubscribersByCategory(CategoryCode.SPORTS))
                .thenReturn(subscribers);

        doThrow(new RuntimeException("Dispatcher failed"))
                .when(notificationDispatcher)
                .dispatch(message, subscribers);

        // when
        notificationJobProcessor.process(pendingJob);

        // assert
        ArgumentCaptor<NotificationJob> jobCaptor = ArgumentCaptor.forClass(NotificationJob.class);

        verify(notificationJobRepositoryPort, org.mockito.Mockito.times(2))
                .save(jobCaptor.capture());

        List<NotificationJob> savedJobs = jobCaptor.getAllValues();

        assertThat(savedJobs.get(0).getStatus()).isEqualTo(NotificationJobStatus.PROCESSING);

        assertThat(savedJobs.get(1).getStatus()).isEqualTo(NotificationJobStatus.FAILED);
        assertThat(savedJobs.get(1).getLastError()).contains("Dispatcher failed");
        assertThat(savedJobs.get(1).getProcessedAt()).isNotNull();
    }

    @Test
    void shouldProcessPendingJobsFromRepository() {
        // given
        NotificationJob firstJob = pendingJob(1L, 100L, "correlation-1");
        NotificationJob secondJob = pendingJob(2L, 200L, "correlation-2");

        NotificationMessage firstMessage = message(100L, "correlation-1");
        NotificationMessage secondMessage = message(200L, "correlation-2");

        when(notificationJobRepositoryPort.findPendingJobsForProcessing(20))
                .thenReturn(List.of(firstJob, secondJob));

        when(notificationJobRepositoryPort.save(any(NotificationJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(messageRepositoryPort.findById(100L))
                .thenReturn(Optional.of(firstMessage));

        when(messageRepositoryPort.findById(200L))
                .thenReturn(Optional.of(secondMessage));

        when(subscriberRepositoryPort.findActiveSubscribersByCategory(CategoryCode.SPORTS))
                .thenReturn(List.of(subscriber()));

        // when
        int processedCount = notificationJobProcessor.processPendingJobs(20);

        // assert
        assertThat(processedCount).isEqualTo(2);

        verify(notificationJobRepositoryPort).findPendingJobsForProcessing(20);
        verify(notificationDispatcher, org.mockito.Mockito.times(2))
                .dispatch(any(NotificationMessage.class), any());
    }

    private static NotificationJob pendingJob() {
        return pendingJob(1L, 100L, "processor-test-correlation");
    }

    private static NotificationJob pendingJob(
            Long jobId,
            Long messageId,
            String correlationId
    ) {
        return NotificationJob.builder()
                .id(jobId)
                .correlationId(correlationId)
                .messageId(messageId)
                .category(CategoryCode.SPORTS)
                .status(NotificationJobStatus.PENDING)
                .attemptCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static NotificationMessage message() {
        return message(100L, "processor-test-correlation");
    }

    private static NotificationMessage message(
            Long messageId,
            String correlationId
    ) {
        return new NotificationMessage(
                messageId,
                correlationId,
                CategoryCode.SPORTS,
                "Test message",
                LocalDateTime.now()
        );
    }

    private static NotificationSubscriber subscriber() {
        return new NotificationSubscriber(
                10L,
                "Alice",
                "alice@example.com",
                "+15550000001",
                Set.of(CategoryCode.SPORTS),
                EnumSet.of(NotificationChannelCode.EMAIL)
        );
    }
}
