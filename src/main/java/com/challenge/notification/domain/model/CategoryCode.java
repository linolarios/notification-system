package com.challenge.notification.domain.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CategoryCode {

    SPORTS,
    FINANCE,
    MOVIES;

    private static final Map<String, CategoryCode> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(
                            category -> category.name().toLowerCase(Locale.ROOT),
                            Function.identity()
                    ));

    public static CategoryCode from(String value) {
        CategoryCode category = LOOKUP.get(value.toLowerCase(Locale.ROOT));

        if (category == null) {
            throw new IllegalArgumentException("Unsupported category: " + value);
        }

        return category;
    }
}
