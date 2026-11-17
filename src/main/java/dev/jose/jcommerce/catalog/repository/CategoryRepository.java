package dev.jose.jcommerce.catalog.repository;


import dev.jose.jcommerce.catalog.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlugAndActiveTrue(String slug);
    Optional<Category> findBySlug(String slug);
    Boolean existsByNameIgnoreCase(String name);
    Boolean existsBySlug(String slug);
    Boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    Boolean existsBySlugAndIdNot(String slug, UUID id);
    Page<Category> findByActiveTrue(Pageable pageable);
}
