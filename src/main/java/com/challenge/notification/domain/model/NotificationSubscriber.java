package com.challenge.notification.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class NotificationSubscriber {
    private final Long id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final Set<CategoryCode> subscribedCategories;
    private final Set<NotificationChannelCode> enabledChannels;

    public NotificationSubscriber(
            Long id,
            String name,
            String email,
            String phoneNumber,
            Set<CategoryCode> subscribedCategories,
            Set<NotificationChannelCode> enabledChannels
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = requireText(name, "name");
        this.email = requireText(email, "email");
        this.phoneNumber = requireText(phoneNumber, "phoneNumber");
        this.subscribedCategories = copyCategories(subscribedCategories);
        this.enabledChannels = copyChannels(enabledChannels);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }

    private static Set<CategoryCode> copyCategories(Set<CategoryCode> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(EnumSet.copyOf(categories));
    }

    private static Set<NotificationChannelCode> copyChannels(Set<NotificationChannelCode> channels) {
        if (channels == null || channels.isEmpty()) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(EnumSet.copyOf(channels));
    }

    public boolean isSubscribedTo(CategoryCode category) {
        return subscribedCategories.contains(category);
    }

    public boolean hasEnabledChannel(NotificationChannelCode channel) {
        return enabledChannels.contains(channel);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Set<CategoryCode> getSubscribedCategories() {
        return subscribedCategories;
    }

    public Set<NotificationChannelCode> getEnabledChannels() {
        return enabledChannels;
    }
}
