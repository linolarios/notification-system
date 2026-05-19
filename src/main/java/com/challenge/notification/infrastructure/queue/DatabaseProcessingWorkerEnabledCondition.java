package com.challenge.notification.infrastructure.queue;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Enables database-backed notification workers only when database processing mode is active
 * and workers are explicitly enabled.
 *
 * <p>This composite condition keeps scheduled workers disabled in tests or environments
 * where background processing should not start automatically.</p>
 */
class DatabaseProcessingWorkerEnabledCondition extends AllNestedConditions {

    DatabaseProcessingWorkerEnabledCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(
            prefix = "notification.queue",
            name = "processing-mode",
            havingValue = "database",
            matchIfMissing = true
    )
    static class DatabaseProcessingModeEnabled {
    }

    @ConditionalOnProperty(
            prefix = "notification.worker",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    static class WorkerEnabled {
    }
}
