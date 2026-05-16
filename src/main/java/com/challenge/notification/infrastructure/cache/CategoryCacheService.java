package com.challenge.notification.infrastructure.cache;

import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
