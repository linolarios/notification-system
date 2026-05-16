INSERT INTO categories (code, name, active)
VALUES
    ('SPORTS', 'Sports', TRUE),
    ('FINANCE', 'Finance', TRUE),
    ('MOVIES', 'Movies', TRUE)
    ON CONFLICT (code) DO NOTHING;

INSERT INTO notification_channels (code, name, active)
VALUES
    ('SMS', 'SMS', TRUE),
    ('EMAIL', 'Email', TRUE),
    ('PUSH', 'Push Notification', TRUE)
    ON CONFLICT (code) DO NOTHING;