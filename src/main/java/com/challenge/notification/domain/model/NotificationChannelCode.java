package com.challenge.notification.domain.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum NotificationChannelCode {

    SMS,
    EMAIL,
    PUSH;

    private static final Map<String, NotificationChannelCode> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(
                            channel -> channel.name().toLowerCase(Locale.ROOT),
                            Function.identity()
                    ));

    public static NotificationChannelCode from(String value) {
        NotificationChannelCode channel = LOOKUP.get(value.toLowerCase(Locale.ROOT));
        if (channel == null) {
            throw new IllegalArgumentException("Unsupported notification channel: " + value);
        }

        return channel;
    }
}
