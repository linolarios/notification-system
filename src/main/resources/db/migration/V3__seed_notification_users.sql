INSERT INTO users (name, email, phone_number, created_at, updated_at)
VALUES
    ('Alice Johnson', 'alice@example.com', '+15550000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Bob Smith', 'bob@example.com', '+15550000002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Carla Gomez', 'carla@example.com', '+15550000003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Daniel Lee', 'daniel@example.com', '+15550000004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING;

INSERT INTO user_category_subscriptions (user_id, category_id, created_at)
SELECT u.id, c.id, CURRENT_TIMESTAMP
FROM users u
         JOIN categories c ON c.code IN ('SPORTS', 'MOVIES')
WHERE u.email = 'alice@example.com'
    ON CONFLICT (user_id, category_id) DO NOTHING;

INSERT INTO user_category_subscriptions (user_id, category_id, created_at)
SELECT u.id, c.id, CURRENT_TIMESTAMP
FROM users u
         JOIN categories c ON c.code IN ('FINANCE')
WHERE u.email = 'bob@example.com'
    ON CONFLICT (user_id, category_id) DO NOTHING;

INSERT INTO user_category_subscriptions (user_id, category_id, created_at)
SELECT u.id, c.id, CURRENT_TIMESTAMP
FROM users u
         JOIN categories c ON c.code IN ('SPORTS', 'FINANCE')
WHERE u.email = 'carla@example.com'
    ON CONFLICT (user_id, category_id) DO NOTHING;

INSERT INTO user_category_subscriptions (user_id, category_id, created_at)
SELECT u.id, c.id, CURRENT_TIMESTAMP
FROM users u
         JOIN categories c ON c.code IN ('MOVIES')
WHERE u.email = 'daniel@example.com'
    ON CONFLICT (user_id, category_id) DO NOTHING;

INSERT INTO user_channel_preferences (user_id, channel_id, created_at)
SELECT u.id, ch.id, CURRENT_TIMESTAMP
FROM users u
         JOIN notification_channels ch ON ch.code IN ('EMAIL', 'SMS')
WHERE u.email = 'alice@example.com'
    ON CONFLICT (user_id, channel_id) DO NOTHING;

INSERT INTO user_channel_preferences (user_id, channel_id, created_at)
SELECT u.id, ch.id, CURRENT_TIMESTAMP
FROM users u
         JOIN notification_channels ch ON ch.code IN ('EMAIL')
WHERE u.email = 'bob@example.com'
    ON CONFLICT (user_id, channel_id) DO NOTHING;

INSERT INTO user_channel_preferences (user_id, channel_id, created_at)
SELECT u.id, ch.id, CURRENT_TIMESTAMP
FROM users u
         JOIN notification_channels ch ON ch.code IN ('SMS', 'PUSH')
WHERE u.email = 'carla@example.com'
    ON CONFLICT (user_id, channel_id) DO NOTHING;

INSERT INTO user_channel_preferences (user_id, channel_id, created_at)
SELECT u.id, ch.id, CURRENT_TIMESTAMP
FROM users u
         JOIN notification_channels ch ON ch.code IN ('PUSH')
WHERE u.email = 'daniel@example.com'
    ON CONFLICT (user_id, channel_id) DO NOTHING;