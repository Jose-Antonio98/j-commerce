package dev.jose.jcommerce.catalog.controller;

import dev.jose.jcommerce.catalog.dto.CategoryRequest;
import dev.jose.jcommerce.catalog.dto.CategoryRequestUpdate;
import dev.jose.jcommerce.catalog.dto.CategoryResponse;
import dev.jose.jcommerce.catalog.service.CategoryService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/catalog/category/")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequestUpdate request) {
        return categoryService.updateCategory(id, request);
    }

    @PatchMapping("/active/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateActive(@PathVariable UUID id) {
        categoryService.activeCategory(id);
    }

    @PatchMapping("/desative/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDesative(@PathVariable UUID id) {
        categoryService.desativeCategory(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse findById(@PathVariable UUID id) {
        return categoryService.findCategoryId(id);
    }

    @GetMapping("/slug")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse findSlug(@RequestParam String slug) {
        return categoryService.findCategorySlug(slug);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CategoryResponse> findAll( @ParameterObject
                                               @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return categoryService.findAllCategories(pageable);
    }
}
