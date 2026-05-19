package com.challenge.notification.infrastructure.cache;

import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing category-related caching operations.
 *
 * <p>This service provides methods to retrieve active categories from the database
 * and cache them for efficient retrieval. It also checks for the existence of active
 * categories by their code.</p>
 */
@Service
public class CategoryCacheService {

    private final SpringDataCategoryRepository categoryRepository;

    public CategoryCacheService(SpringDataCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(cacheNames = NotificationCacheNames.ACTIVE_CATEGORIES)
    @Transactional(readOnly = true)
    public List<CategoryEntity> getActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Cacheable(
            cacheNames = NotificationCacheNames.ACTIVE_CATEGORIES,
            key = "'exists:' + #code"
    )
    @Transactional(readOnly = true)
    public boolean existsActiveByCode(String code) {
        return categoryRepository.findByCodeAndActiveTrue(code)
                .isPresent();
    }
}
