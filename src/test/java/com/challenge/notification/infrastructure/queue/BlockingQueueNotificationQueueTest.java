package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.exception.NotificationQueueException;
import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlockingQueueNotificationQueueTest {

    @Test
    void shouldEnqueueAndPollSameJob() {
        // given
        BlockingQueueNotificationQueue queue = new BlockingQueueNotificationQueue(10);
        NotificationJob job = pendingJob(1L, "queue-test-correlation");

        // when
        queue.enqueue(job);
        Optional<NotificationJob> polledJob = queue.poll();

        // assert
        assertThat(polledJob).containsSame(job);
    }

    @Test
    void shouldReturnEmptyWhenQueueIsEmpty() {
        // given
        BlockingQueueNotificationQueue queue = new BlockingQueueNotificationQueue(10);

        // when
        Optional<NotificationJob> polledJob = queue.poll();

        // assert
        assertThat(polledJob).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenQueueIsFull() {
        // given
        BlockingQueueNotificationQueue queue = new BlockingQueueNotificationQueue(1);
        NotificationJob firstJob = pendingJob(1L, "correlation-1");
        NotificationJob secondJob = pendingJob(2L, "correlation-2");

        queue.enqueue(firstJob);

        // when / assert
        assertThatThrownBy(() -> queue.enqueue(secondJob))
                .isInstanceOf(NotificationQueueException.class)
                .hasMessageContaining("Notification queue is full");
    }

    private static NotificationJob pendingJob(Long messageId, String correlationId) {
        return NotificationJob.initializePending(
                messageId,
                correlationId,
                CategoryCode.SPORTS
        );
    }
}
