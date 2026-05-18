package com.challenge.notification.infrastructure.cache;

import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryCacheServiceTest {

    @Mock
    private SpringDataCategoryRepository categoryRepository;

    private CategoryCacheService categoryCacheService;

    @BeforeEach
    void setUp() {
        categoryCacheService = new CategoryCacheService(categoryRepository);
    }

    @Test
    void shouldReturnActiveCategories() {
        // given
        CategoryEntity sports = new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );

        when(categoryRepository.findAllByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(sports));

        // when
        List<CategoryEntity> categories = categoryCacheService.getActiveCategories();

        // assert
        assertThat(categories).containsExactly(sports);
    }

    @Test
    void shouldReturnTrueWhenActiveCategoryExists() {
        // given
        CategoryEntity sports = new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );

        when(categoryRepository.findByCodeAndActiveTrue("SPORTS"))
                .thenReturn(Optional.of(sports));

        // when
        boolean exists = categoryCacheService.existsActiveByCode("SPORTS");

        // assert
        assertThat(exists).isTrue();
    }
}
