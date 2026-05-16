package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.port.CategoryRepositoryPort;
import com.challenge.notification.infrastructure.cache.CategoryCacheService;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCategoryRepositoryAdapter implements CategoryRepositoryPort {
    private final CategoryCacheService categoryCacheService;

    public JpaCategoryRepositoryAdapter(CategoryCacheService categoryCacheService) {
        this.categoryCacheService = categoryCacheService;
    }

    @Override
    public boolean existsActiveByCode(CategoryCode categoryCode) {
        return categoryCacheService.existsActiveByCode(categoryCode.name());
    }
}
