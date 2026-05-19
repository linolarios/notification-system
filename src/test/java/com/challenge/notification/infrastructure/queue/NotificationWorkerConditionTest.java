package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationQueue;
import com.challenge.notification.domain.service.NotificationJobProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationWorkerConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldCreateDatabaseWorkerByDefault() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(NotificationDatabaseJobWorker.class);
                    assertThat(context).doesNotHaveBean(NotificationQueueWorker.class);
                });
    }

    @Test
    void shouldCreateDatabaseWorkerWhenDatabaseModeAndWorkerEnabled() {
        contextRunner
                .withPropertyValues(
                        "notification.queue.processing-mode=database",
                        "notification.worker.enabled=true",
                        "notification.worker.batch-size=7"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(NotificationDatabaseJobWorker.class);
                    assertThat(context).doesNotHaveBean(NotificationQueueWorker.class);

                    NotificationJobProcessor processor = context.getBean(NotificationJobProcessor.class);
                    NotificationDatabaseJobWorker worker = context.getBean(NotificationDatabaseJobWorker.class);

                    when(processor.processPendingJobs(7))
                            .thenReturn(2);

                    worker.processPendingJobs();

                    verify(processor).processPendingJobs(7);
                });
    }

    @Test
    void shouldNotCreateDatabaseWorkerWhenWorkerDisabled() {
        contextRunner
                .withPropertyValues(
                        "notification.queue.processing-mode=database",
                        "notification.worker.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NotificationDatabaseJobWorker.class);
                    assertThat(context).doesNotHaveBean(NotificationQueueWorker.class);
                });
    }

    @Test
    void shouldCreateMemoryWorkerWhenMemoryModeAndWorkerEnabled() {
        contextRunner
                .withPropertyValues(
                        "notification.queue.processing-mode=memory",
                        "notification.worker.enabled=true",
                        "notification.worker.batch-size=3"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NotificationDatabaseJobWorker.class);
                    assertThat(context).hasSingleBean(NotificationQueueWorker.class);

                    NotificationQueue queue = context.getBean(NotificationQueue.class);
                    NotificationJobProcessor processor = context.getBean(NotificationJobProcessor.class);
                    NotificationQueueWorker worker = context.getBean(NotificationQueueWorker.class);

                    NotificationJob firstJob = job(1L, "memory-worker-correlation-1");
                    NotificationJob secondJob = job(2L, "memory-worker-correlation-2");
                    NotificationJob thirdJob = job(3L, "memory-worker-correlation-3");

                    when(queue.poll())
                            .thenReturn(Optional.of(firstJob))
                            .thenReturn(Optional.of(secondJob))
                            .thenReturn(Optional.of(thirdJob));

                    worker.pollQueue();

                    verify(queue, Mockito.times(3)).poll();
                    verify(processor).process(firstJob);
                    verify(processor).process(secondJob);
                    verify(processor).process(thirdJob);
                });
    }

    @Test
    void shouldNotCreateMemoryWorkerWhenWorkerDisabled() {
        contextRunner
                .withPropertyValues(
                        "notification.queue.processing-mode=memory",
                        "notification.worker.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NotificationDatabaseJobWorker.class);
                    assertThat(context).doesNotHaveBean(NotificationQueueWorker.class);
                });
    }

    @Test
    void shouldNotCreateDatabaseWorkerWhenMemoryModeIsActive() {
        contextRunner
                .withPropertyValues(
                        "notification.queue.processing-mode=memory",
                        "notification.worker.enabled=true"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NotificationDatabaseJobWorker.class);
                    assertThat(context).hasSingleBean(NotificationQueueWorker.class);
                });
    }

    @Test
    void shouldNotCreateMemoryWorkerWhenDatabaseModeIsActive() {
        contextRunner
                .withPropertyValues(
                        "notification.queue.processing-mode=database",
                        "notification.worker.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(NotificationDatabaseJobWorker.class);
                    assertThat(context).doesNotHaveBean(NotificationQueueWorker.class);
                });
    }

    private static NotificationJob job(Long id, String correlationId) {
        return NotificationJob.initializePending(
                        100L,
                        correlationId,
                        CategoryCode.SPORTS
                )
                .toBuilder()
                .id(id)
                .build();
    }

    @Configuration
    @Import({
            NotificationDatabaseJobWorker.class,
            NotificationQueueWorker.class
    })
    static class TestConfiguration {

        @Bean
        NotificationJobProcessor notificationJobProcessor() {
            return mock(NotificationJobProcessor.class);
        }

        @Bean
        NotificationQueue notificationQueue() {
            return mock(NotificationQueue.class);
        }
    }
}
