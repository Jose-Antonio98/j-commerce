package dev.jose.jcommerce.catalog.repository;



import dev.jose.jcommerce.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    Boolean existsBySlugAndIdNot(String slug, UUID id);
    Boolean existsBySlug(String slug);
    Optional<Product> findBySlugAndActiveTrueAndCategory_ActiveTrue(String slug);
    Optional<Product> findBySlug(String slug);
}
