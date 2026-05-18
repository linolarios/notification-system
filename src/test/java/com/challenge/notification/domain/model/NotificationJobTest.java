package com.challenge.notification.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationJobTest {

    @Test
    void shouldCreatePendingJob() {
        // given / when
        NotificationJob job = NotificationJob.initializePending(
                100L,
                "job-test-correlation",
                CategoryCode.SPORTS
        );

        // assert
        assertThat(job.getStatus()).isEqualTo(NotificationJobStatus.PENDING);
        assertThat(job.getMessageId()).isEqualTo(100L);
        assertThat(job.getCorrelationId()).isEqualTo("job-test-correlation");
        assertThat(job.getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(job.getAttemptCount()).isZero();
        assertThat(job.getLockedAt()).isNull();
        assertThat(job.getProcessedAt()).isNull();
        assertThat(job.getLastError()).isNull();
    }

    @Test
    void shouldTransitionFromPendingToProcessing() {
        // given
        NotificationJob pendingJob = NotificationJob.initializePending(
                100L,
                "job-test-correlation",
                CategoryCode.SPORTS
        );

        // when
        NotificationJob processingJob = pendingJob.markProcessing();

        // assert
        assertThat(processingJob.getStatus()).isEqualTo(NotificationJobStatus.PROCESSING);
        assertThat(processingJob.getAttemptCount()).isEqualTo(1);
        assertThat(processingJob.getLockedAt()).isNotNull();
        assertThat(processingJob.getProcessedAt()).isNull();
        assertThat(processingJob.getLastError()).isNull();
    }

    @Test
    void shouldTransitionFromProcessingToProcessed() {
        // given
        NotificationJob processingJob = NotificationJob.initializePending(
                100L,
                "job-test-correlation",
                CategoryCode.SPORTS
        ).markProcessing();

        // when
        NotificationJob processedJob = processingJob.markProcessed();

        // assert
        assertThat(processedJob.getStatus()).isEqualTo(NotificationJobStatus.PROCESSED);
        assertThat(processedJob.getProcessedAt()).isNotNull();
        assertThat(processedJob.getLastError()).isNull();
    }

    @Test
    void shouldTransitionFromProcessingToFailed() {
        // given
        NotificationJob processingJob = NotificationJob.initializePending(
                100L,
                "job-test-correlation",
                CategoryCode.SPORTS
        ).markProcessing();

        // when
        NotificationJob failedJob = processingJob.markFailed("Provider unavailable");

        // assert
        assertThat(failedJob.getStatus()).isEqualTo(NotificationJobStatus.FAILED);
        assertThat(failedJob.getProcessedAt()).isNotNull();
        assertThat(failedJob.getLastError()).isEqualTo("Provider unavailable");
    }

    @Test
    void shouldNotMarkPendingJobAsProcessedDirectly() {
        // given
        NotificationJob pendingJob = NotificationJob.initializePending(
                100L,
                "job-test-correlation",
                CategoryCode.SPORTS
        );

        // when / assert
        assertThatThrownBy(pendingJob::markProcessed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PROCESSING jobs can be marked as PROCESSED");
    }

    @Test
    void shouldNotMarkFailedJobAsProcessingAgain() {
        // given
        NotificationJob failedJob = NotificationJob.initializePending(
                100L,
                "job-test-correlation",
                CategoryCode.SPORTS
        ).markProcessing().markFailed("Provider unavailable");

        // when / assert
        assertThatThrownBy(failedJob::markProcessing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING jobs can be marked as PROCESSING");
    }

    @Test
    void shouldTruncateLongErrorMessageWhenMarkingFailed() {
        // given
        NotificationJob processingJob = NotificationJob.initializePending(
                100L,
                "job-test-correlation",
                CategoryCode.SPORTS
        ).markProcessing();

        String longError = "x".repeat(600);

        // when
        NotificationJob failedJob = processingJob.markFailed(longError);

        // assert
        assertThat(failedJob.getStatus()).isEqualTo(NotificationJobStatus.FAILED);
        assertThat(failedJob.getLastError()).hasSize(500);
    }
}
