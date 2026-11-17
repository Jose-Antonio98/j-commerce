package dev.jose.jcommerce.catalog.repository;

import dev.jose.jcommerce.catalog.domain.Category;
import dev.jose.jcommerce.catalog.domain.Product;
import dev.jose.jcommerce.catalog.dto.ProductFilterRequest;
import dev.jose.jcommerce.catalog.specification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:catalog;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class ProductSpecificationRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category activeCategory;
    private Category inactiveCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        activeCategory = categoryRepository.save(category("Eletrodomésticos", "eletrodomesticos", true));
        inactiveCategory = categoryRepository.save(category("Inativos", "inativos", false));

        productRepository.saveAll(List.of(
                product("Cafeteira", "cafeteira", activeCategory, true, "129.90"),
                product("Moedor de café", "moedor-cafe", activeCategory, true, "79.90"),
                product("Cafeteira antiga", "cafeteira-antiga", activeCategory, false, "99.90"),
                product("Moedor antigo", "moedor-antigo", activeCategory, false, "49.90"),
                product("Cafeteira oculta", "cafeteira-oculta", inactiveCategory, true, "110.00")
        ));
        productRepository.flush();
    }

    @Test
    void publicSpecificationCombinesOptionalFiltersAndOnlyReturnsSellableProducts() {
        var filter = new ProductFilterRequest(
                "CAF",
                activeCategory.getId(),
                true,
                new BigDecimal("100.00"),
                new BigDecimal("150.00")
        );

        Page<Product> result = productRepository.findAll(
                ProductSpecification.publicFilters(filter),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Cafeteira", result.getContent().getFirst().getName());
        assertTrue(result.getContent().getFirst().getActive());
        assertTrue(result.getContent().getFirst().getCategory().getActive());
    }

    @Test
    void publicSpecificationWithNoOptionalFiltersStillExcludesInactiveProductsAndCategories() {
        Page<Product> result = productRepository.findAll(
                ProductSpecification.publicFilters(new ProductFilterRequest(null, null, null, null, null)),
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(Product::getActive));
        assertTrue(result.getContent().stream()
                .allMatch(product -> Boolean.TRUE.equals(product.getCategory().getActive())));
    }

    @Test
    void adminSpecificationCanFilterInactiveProductsAndApplyPagination() {
        var filter = new ProductFilterRequest(null, null, false, null, null);

        Page<Product> firstPage = productRepository.findAll(
                ProductSpecification.adminFilters(filter),
                PageRequest.of(0, 1)
        );

        assertEquals(2, firstPage.getTotalElements());
        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalPages());

        var allInactive = productRepository.findAll(
                ProductSpecification.adminFilters(filter),
                PageRequest.of(0, 10)
        );
        assertEquals(2, allInactive.getContent().size());
        assertTrue(allInactive.getContent().stream().noneMatch(Product::getActive));
    }

    @Test
    void categoryRepositoryFindsPublicCategoriesAndSlug() {
        assertEquals(1, categoryRepository.findByActiveTrue(PageRequest.of(0, 10)).getTotalElements());
        assertTrue(categoryRepository.findBySlugAndActiveTrue("eletrodomesticos").isPresent());
        assertTrue(categoryRepository.findBySlugAndActiveTrue("inativos").isEmpty());
    }

    private static Category category(String name, String slug, boolean active) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setActive(active);
        return category;
    }

    private static Product product(
            String name,
            String slug,
            Category category,
            boolean active,
            String price
    ) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(slug);
        product.setCategory(category);
        product.setActive(active);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(5);
        return product;
    }
}
