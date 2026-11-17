package dev.jose.jcommerce.catalog.service;

import dev.jose.jcommerce.catalog.domain.Category;
import dev.jose.jcommerce.catalog.dto.CategoryRequest;
import dev.jose.jcommerce.catalog.dto.CategoryRequestUpdate;
import dev.jose.jcommerce.catalog.mapper.CategoryMapper;
import dev.jose.jcommerce.catalog.repository.CategoryRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, new CategoryMapper());
    }

    @Test
    void createsCategoryWithNormalizedRequestValues() {
        var request = new CategoryRequest("  Café   da manhã ", "Café da manhã");
        when(categoryRepository.existsByNameIgnoreCase("Café da manhã")).thenReturn(false);
        when(categoryRepository.existsBySlug("cafe-da-manha")).thenReturn(false);
        when(categoryRepository.saveAndFlush(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category category = invocation.getArgument(0);
                    category.setId(UUID.randomUUID());
                    return category;
                });

        var response = categoryService.createCategory(request);

        assertEquals("Café da manhã", response.name());
        assertEquals("cafe-da-manha", response.slug());
        assertTrue(response.active());
    }

    @Test
    void rejectsDuplicateNameBeforePersisting() {
        var request = new CategoryRequest("Livros", "livros");
        when(categoryRepository.existsByNameIgnoreCase("Livros")).thenReturn(true);

        assertThrows(ResourceAlreadyRegistered.class, () -> categoryService.createCategory(request));

        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateSlugBeforePersisting() {
        var request = new CategoryRequest("Livros", "livros");
        when(categoryRepository.existsByNameIgnoreCase("Livros")).thenReturn(false);
        when(categoryRepository.existsBySlug("livros")).thenReturn(true);

        assertThrows(ResourceAlreadyRegistered.class, () -> categoryService.createCategory(request));

        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void translatesSlugConstraintViolationDuringCreate() {
        var request = new CategoryRequest("Livros", "livros");
        when(categoryRepository.existsByNameIgnoreCase("Livros")).thenReturn(false);
        when(categoryRepository.existsBySlug("livros")).thenReturn(false);
        var constraintViolation = new ConstraintViolationException(
                "duplicate slug", new SQLException(), "uk_category_slug");
        when(categoryRepository.saveAndFlush(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate slug", constraintViolation));

        assertThrows(ResourceAlreadyRegistered.class, () -> categoryService.createCategory(request));
    }

    @Test
    void updatesOnlyProvidedFieldsAndChecksDuplicatesExcludingCurrentCategory() {
        UUID id = UUID.randomUUID();
        Category category = category(id, "Casa", "casa", true);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Casa e jardim", id)).thenReturn(false);
        when(categoryRepository.existsBySlugAndIdNot("casa", id)).thenReturn(false);
        when(categoryRepository.saveAndFlush(category)).thenReturn(category);

        var response = categoryService.updateCategory(
                id,
                new CategoryRequestUpdate("Casa e jardim", null)
        );

        assertEquals("Casa e jardim", response.name());
        assertEquals("casa", response.slug());
        verify(categoryRepository).existsByNameIgnoreCaseAndIdNot("Casa e jardim", id);
        verify(categoryRepository).existsBySlugAndIdNot("casa", id);
    }

    @Test
    void rejectsDuplicateSlugDuringUpdate() {
        UUID id = UUID.randomUUID();
        Category category = category(id, "Casa", "existente", true);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlugAndIdNot("novo-slug", id)).thenReturn(true);

        assertThrows(
                ResourceAlreadyRegistered.class,
                () -> categoryService.updateCategory(id, new CategoryRequestUpdate(null, "novo slug"))
        );

        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingCategory() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(id, new CategoryRequestUpdate("Nome novo", null))
        );
    }

    @Test
    void activatesAndDeactivatesCategory() {
        UUID id = UUID.randomUUID();
        Category category = category(id, "Casa", "casa", false);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        categoryService.activeCategory(id);
        assertTrue(category.getActive());

        categoryService.desativeCategory(id);
        assertFalse(category.getActive());
    }

    @Test
    void rejectsActivationOrDeactivationWhenAlreadyInRequestedState() {
        UUID id = UUID.randomUUID();
        Category activeCategory = category(id, "Casa", "casa", true);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(activeCategory));
        assertThrows(ResourceAlreadyActive.class, () -> categoryService.activeCategory(id));

        Category inactiveCategory = category(id, "Casa", "casa", false);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(inactiveCategory));
        assertThrows(ResourceAlreadyDesative.class, () -> categoryService.desativeCategory(id));
    }

    @Test
    void returnsOnlyActiveCategoriesForPublicListingAndPreservesPageMetadata() {
        Pageable pageable = PageRequest.of(1, 2);
        Category category = category(UUID.randomUUID(), "Casa", "casa", true);
        when(categoryRepository.findByActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(category), pageable, 5));

        var page = categoryService.findActiveCategories(pageable);

        assertEquals(5, page.getTotalElements());
        assertEquals(1, page.getNumber());
        assertEquals(List.of("casa"), page.getContent().stream().map(response -> response.slug()).toList());
    }

    @Test
    void findsCategoryBySlugAndIdOrThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        Category category = category(id, "Casa", "casa", true);
        when(categoryRepository.findBySlug("casa")).thenReturn(Optional.of(category));
        when(categoryRepository.findBySlugAndActiveTrue("casa")).thenReturn(Optional.of(category));
        when(categoryRepository.findBySlug("ausente")).thenReturn(Optional.empty());

        assertEquals(id, categoryService.findCategorySlug(" CASA ").id());
        assertEquals(id, categoryService.findCategorySlugActiveTrue("casa").id());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.findCategorySlug("ausente"));
    }

    @Test
    void listsAllCategoriesForAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Category inactive = category(UUID.randomUUID(), "Inativa", "inativa", false);
        when(categoryRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(inactive), pageable, 1));

        var page = categoryService.findAllCategories(pageable);

        assertEquals(1, page.getTotalElements());
        assertFalse(page.getContent().getFirst().active());
    }

    private static Category category(UUID id, String name, String slug, boolean active) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(slug);
        category.setActive(active);
        return category;
    }
}
