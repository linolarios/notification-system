package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.infrastructure.cache.CategoryCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaCategoryRepositoryAdapterTest {

    @Mock
    private CategoryCacheService categoryCacheService;

    private JpaCategoryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaCategoryRepositoryAdapter(categoryCacheService);
    }

    @Test
    void shouldReturnTrueWhenActiveCategoryExists() {
        // given
        when(categoryCacheService.existsActiveByCode("SPORTS"))
                .thenReturn(true);

        // when
        boolean exists = adapter.existsActiveByCode(CategoryCode.SPORTS);

        // assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenActiveCategoryDoesNotExist() {
        // given
        when(categoryCacheService.existsActiveByCode("SPORTS"))
                .thenReturn(false);

        // when
        boolean exists = adapter.existsActiveByCode(CategoryCode.SPORTS);

        // assert
        assertThat(exists).isFalse();
    }
}
