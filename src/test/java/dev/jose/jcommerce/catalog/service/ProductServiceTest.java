package dev.jose.jcommerce.catalog.service;

import dev.jose.jcommerce.catalog.domain.Category;
import dev.jose.jcommerce.catalog.domain.Product;
import dev.jose.jcommerce.catalog.dto.ProductFilterRequest;
import dev.jose.jcommerce.catalog.dto.ProductRequest;
import dev.jose.jcommerce.catalog.dto.ProductRequestUpdate;
import dev.jose.jcommerce.catalog.exception.InactiveCategoryException;
import dev.jose.jcommerce.catalog.mapper.ProductMapper;
import dev.jose.jcommerce.catalog.repository.CategoryRepository;
import dev.jose.jcommerce.catalog.repository.ProductRepository;
import dev.jose.jcommerce.exception.ResourceAlreadyActive;
import dev.jose.jcommerce.exception.ResourceAlreadyDesative;
import dev.jose.jcommerce.exception.ResourceAlreadyRegistered;
import dev.jose.jcommerce.exception.ResourceNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(
                productRepository,
                categoryRepository,
                new ProductMapper()
        );
    }

    @Test
    void createsProductInActiveCategory() {
        Category category = category(UUID.randomUUID(), true);
        ProductRequest request = request(" Cafeteira ", "Café teira", category.getId());
        when(productRepository.existsBySlug("cafe-teira")).thenReturn(false);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    product.setId(UUID.randomUUID());
                    return product;
                });

        var response = productService.createProduct(request);

        assertEquals("Cafeteira", response.name());
        assertEquals("cafe-teira", response.slug());
        assertEquals(category.getId(), response.category());
        verify(productRepository).saveAndFlush(any(Product.class));
    }

    @Test
    void rejectsDuplicateSlugBeforeCreating() {
        ProductRequest request = request("Cafeteira", "cafeteira", UUID.randomUUID());
        when(productRepository.existsBySlug("cafeteira")).thenReturn(true);

        assertThrows(ResourceAlreadyRegistered.class, () -> productService.createProduct(request));

        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingOrInactiveCategoryWhenCreating() {
        UUID categoryId = UUID.randomUUID();
        ProductRequest request = request("Cafeteira", "cafeteira", categoryId);
        when(productRepository.existsBySlug("cafeteira")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));

        Category inactiveCategory = category(categoryId, false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(inactiveCategory));
        assertThrows(InactiveCategoryException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    void translatesProductSlugConstraintViolationDuringCreate() {
        Category category = category(UUID.randomUUID(), true);
        ProductRequest request = request("Cafeteira", "cafeteira", category.getId());
        when(productRepository.existsBySlug("cafeteira")).thenReturn(false);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        var constraintViolation = new ConstraintViolationException(
                "duplicate slug", new SQLException(), "uk_product_slug");
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate slug", constraintViolation));

        assertThrows(ResourceAlreadyRegistered.class, () -> productService.createProduct(request));
    }

    @Test
    void updatesProductFieldsAndCategoryWhenCategoryIdIsProvided() {
        UUID id = UUID.randomUUID();
        Category originalCategory = category(UUID.randomUUID(), true);
        Category newCategory = category(UUID.randomUUID(), true);
        Product product = product(id, "Cafeteira", "cafeteira", originalCategory, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategory.getId())).thenReturn(Optional.of(newCategory));
        when(productRepository.existsBySlugAndIdNot("cafeteira-premium", id)).thenReturn(false);
        when(productRepository.saveAndFlush(product)).thenReturn(product);

        var response = productService.updateProduct(
                id,
                new ProductRequestUpdate(
                        "Cafeteira Premium",
                        "cafeteira premium",
                        null,
                        new BigDecimal("199.90"),
                        4,
                        null,
                        newCategory.getId()
                )
        );

        assertEquals("Cafeteira Premium", response.name());
        assertEquals("cafeteira-premium", response.slug());
        assertEquals(new BigDecimal("199.90"), response.price());
        assertEquals(4, response.quantity());
        assertEquals(newCategory.getId(), response.category());
    }

    @Test
    void updateKeepsCurrentCategoryWhenCategoryIdIsAbsent() {
        UUID id = UUID.randomUUID();
        Category category = category(UUID.randomUUID(), true);
        Product product = product(id, "Cafeteira", "cafeteira", category, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.saveAndFlush(product)).thenReturn(product);

        var response = productService.updateProduct(
                id,
                new ProductRequestUpdate(null, null, "Nova descrição", null, null, null, null)
        );

        assertEquals(category.getId(), response.category());
        assertEquals("Nova descrição", response.description());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void rejectsDuplicateSlugAndMissingProductDuringUpdate() {
        UUID id = UUID.randomUUID();
        Category category = category(UUID.randomUUID(), true);
        Product product = product(id, "Cafeteira", "cafeteira", category, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.existsBySlugAndIdNot("outro-slug", id)).thenReturn(true);

        assertThrows(
                ResourceAlreadyRegistered.class,
                () -> productService.updateProduct(
                        id,
                        new ProductRequestUpdate(null, "outro slug", null, null, null, null, null)
                )
        );
        verify(productRepository, never()).saveAndFlush(any());

        UUID missingId = UUID.randomUUID();
        when(productRepository.findById(missingId)).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(
                        missingId,
                        new ProductRequestUpdate("Nome novo", null, null, null, null, null, null)
                )
        );
    }

    @Test
    void activatesOnlyInactiveProductWithActiveCategory() {
        UUID id = UUID.randomUUID();
        Category activeCategory = category(UUID.randomUUID(), true);
        Product inactiveProduct = product(id, "Cafeteira", "cafeteira", activeCategory, false);
        when(productRepository.findById(id)).thenReturn(Optional.of(inactiveProduct));

        productService.activeProduct(id);

        assertTrue(inactiveProduct.getActive());
    }

    @Test
    void refusesToActivateProductWhenAlreadyActiveOrCategoryInactive() {
        UUID id = UUID.randomUUID();
        Category activeCategory = category(UUID.randomUUID(), true);
        when(productRepository.findById(id))
                .thenReturn(Optional.of(product(id, "Cafeteira", "cafeteira", activeCategory, true)));
        assertThrows(ResourceAlreadyActive.class, () -> productService.activeProduct(id));

        Category inactiveCategory = category(UUID.randomUUID(), false);
        Product inactiveProduct = product(id, "Cafeteira", "cafeteira", inactiveCategory, false);
        when(productRepository.findById(id)).thenReturn(Optional.of(inactiveProduct));
        assertThrows(InactiveCategoryException.class, () -> productService.activeProduct(id));
        assertFalse(inactiveProduct.getActive());
    }

    @Test
    void deactivatesProductEvenWhenItsCategoryIsInactive() {
        UUID id = UUID.randomUUID();
        Category inactiveCategory = category(UUID.randomUUID(), false);
        Product product = product(id, "Cafeteira", "cafeteira", inactiveCategory, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        productService.deactivateProduct(id);

        assertFalse(product.getActive());
    }

    @Test
    void rejectsDeactivationWhenProductIsAlreadyInactive() {
        UUID id = UUID.randomUUID();
        Category category = category(UUID.randomUUID(), false);
        Product product = product(id, "Cafeteira", "cafeteira", category, false);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        assertThrows(ResourceAlreadyDesative.class, () -> productService.deactivateProduct(id));
    }

    @Test
    void publicProductLookupsHideInactiveProductsAndCategories() {
        UUID id = UUID.randomUUID();
        Category inactiveCategory = category(UUID.randomUUID(), false);
        Product product = product(id, "Cafeteira", "cafeteira", inactiveCategory, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(id));
    }

    @Test
    void adminLookupBySlugNormalizesSlugAndReturnsInactiveProducts() {
        Category category = category(UUID.randomUUID(), false);
        Product product = product(UUID.randomUUID(), "Cafeteira", "cafeteira", category, false);
        when(productRepository.findBySlug("cafeteira")).thenReturn(Optional.of(product));

        var response = productService.findProductBySlug(" Cafeteira ");

        assertEquals(product.getId(), response.id());
        assertFalse(response.active());
    }

    @Test
    void publicAndAdminListingsPreservePagination() {
        Pageable pageable = PageRequest.of(0, 5);
        Category category = category(UUID.randomUUID(), true);
        Product product = product(UUID.randomUUID(), "Cafeteira", "cafeteira", category, true);
        when(productRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 7));

        var filter = new ProductFilterRequest(null, null, null, null, null);
        var publicPage = productService.findAllProductsActives(filter, pageable);
        var adminPage = productService.findAllProducts(filter, pageable);

        assertEquals(7, publicPage.getTotalElements());
        assertEquals(7, adminPage.getTotalElements());
        verify(productRepository, org.mockito.Mockito.times(2))
                .findAll(any(Specification.class), eq(pageable));
    }

    private static ProductRequest request(String name, String slug, UUID categoryId) {
        return new ProductRequest(
                name,
                slug,
                null,
                BigDecimal.TEN,
                1,
                null,
                categoryId
        );
    }

    private static Category category(UUID id, boolean active) {
        Category category = new Category();
        category.setId(id);
        category.setName("Eletrodomésticos");
        category.setSlug("eletrodomesticos");
        category.setActive(active);
        return category;
    }

    private static Product product(UUID id, String name, String slug, Category category, boolean active) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSlug(slug);
        product.setPrice(BigDecimal.TEN);
        product.setQuantity(5);
        product.setActive(active);
        product.setCategory(category);
        return product;
    }
}
