package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, Short> {

    Optional<CategoryEntity> findByCodeAndActiveTrue(String code);

    List<CategoryEntity> findAllByActiveTrueOrderByNameAsc();
}
