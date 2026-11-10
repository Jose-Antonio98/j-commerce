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
@RequestMapping("/catalog/category/")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping
    public Page<CategoryResponse> findActiveCategories( @ParameterObject
                                           @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return categoryService.findActiveCategories(pageable);
    }

    @GetMapping("/slug")
    public CategoryResponse findActiveSlug(@RequestParam("slug") String slug) {
        return categoryService.findCategorySlugActiveTrue(slug);
    }
}
