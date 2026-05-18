package com.challenge.notification.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryCodeTest {

    @Test
    void shouldParseCategoryIgnoringCase() {
        // given / when
        CategoryCode category = CategoryCode.from("sports");

        // assert
        assertThat(category).isEqualTo(CategoryCode.SPORTS);
    }

    @Test
    void shouldThrowForUnsupportedCategory() {
        // given / when / assert
        assertThatThrownBy(() -> CategoryCode.from("TECH"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported category");
    }

    @Test
    void shouldThrowForBlankCategory() {
        // given / when / assert
        assertThatThrownBy(() -> CategoryCode.from(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
