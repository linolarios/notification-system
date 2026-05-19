CREATE INDEX idx_notification_jobs_status_created_at
    ON notification_jobs (status, created_at);

CREATE INDEX idx_notification_jobs_status_locked_at
    ON notification_jobs (status, locked_at);

CREATE INDEX idx_notification_jobs_status_attempt_count_updated_at
    ON notification_jobs (status, attempt_count, updated_at);