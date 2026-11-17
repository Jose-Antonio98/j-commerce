package dev.jose.jcommerce.catalog.specification;

import dev.jose.jcommerce.catalog.domain.Product;
import dev.jose.jcommerce.catalog.dto.ProductFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> adminFilters(ProductFilterRequest filter) {
        return Specification.allOf(
                nameContains(filter.name()),
                categoryIs(filter.categoryId()),
                activeIs(filter.active()),
                priceAtLeast(filter.minPrice()),
                priceAtMost(filter.maxPrice())
        );
    }

    public static Specification<Product> publicFilters(ProductFilterRequest filter) {
        return Specification.allOf(
                nameContains(filter.name()),
                categoryIs(filter.categoryId()),
                activeIs(true),
                categoryIsActive(true),
                priceAtLeast(filter.minPrice()),
                priceAtMost(filter.maxPrice())
        );
    }

    private static Specification<Product> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return null;
            }

            String pattern = "%" + name.strip().toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
        };
    }

    private static Specification<Product> categoryIs(UUID categoryId) {
        return ((root, query, criteriaBuilder) ->
                categoryId == null ? null : criteriaBuilder.equal(root.get("category").get("id"), categoryId));
    }

    private static Specification<Product> activeIs(Boolean active) {
        return ((root, query, criteriaBuilder) ->
                active == null ? null : criteriaBuilder.equal(root.get("active"), active));
    }

    private static Specification<Product> priceAtLeast(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<Product> priceAtMost(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null
                ? null
                : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private static Specification<Product> categoryIsActive(boolean active) {
        return (root, query, cb) ->
                cb.equal(root.get("category").get("active"), active);
    }

}
