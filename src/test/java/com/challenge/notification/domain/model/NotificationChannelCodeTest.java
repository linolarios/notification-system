
package com.challenge.notification.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationChannelCodeTest {

    @Test
    void shouldParseChannelIgnoringCase() {
        // given / when
        NotificationChannelCode channel = NotificationChannelCode.from("email");

        // assert
        assertThat(channel).isEqualTo(NotificationChannelCode.EMAIL);
    }

    @Test
    void shouldThrowForUnsupportedChannel() {
        // given / when / assert
        assertThatThrownBy(() -> NotificationChannelCode.from("WHATSAPP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported notification channel");
    }

    @Test
    void shouldThrowForBlankChannel() {
        // given / when / assert
        assertThatThrownBy(() -> NotificationChannelCode.from(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}