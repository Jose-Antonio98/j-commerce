package dev.jose.jcommerce.catalog.controller;

import dev.jose.jcommerce.catalog.dto.CategoryResponse;
import dev.jose.jcommerce.exception.ErrorResponse;
import dev.jose.jcommerce.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog/categories")
@Tag(name = "Categorias", description = "Consultas públicas de categorias ativas.")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping
    @Operation(summary = "Lista categorias ativas")
    @ApiResponse(responseCode = "200", description = "Página de categorias ativas")
    public Page<CategoryResponse> findActiveCategories(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        return categoryService.findActiveCategories(pageable);
    }

    @GetMapping("/slug")
    @Operation(summary = "Busca uma categoria ativa pelo slug")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "400", description = "Parâmetro slug ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoria ativa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse findActiveSlug(
            @Parameter(description = "Slug da categoria", example = "cafe-da-manha")
            @RequestParam("slug") String slug
    ) {
        return categoryService.findCategorySlugActiveTrue(slug);
    }
}
