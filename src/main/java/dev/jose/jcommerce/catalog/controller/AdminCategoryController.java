package dev.jose.jcommerce.catalog.controller;

import dev.jose.jcommerce.catalog.dto.CategoryRequest;
import dev.jose.jcommerce.catalog.dto.CategoryRequestUpdate;
import dev.jose.jcommerce.catalog.dto.CategoryResponse;
import dev.jose.jcommerce.exception.ErrorResponse;
import dev.jose.jcommerce.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/admin/catalog/categories")
@Tag(name = "Administração de categorias", description = "Operações administrativas de categorias; requer role ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "409", description = "Nome ou slug já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza parcialmente uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Nome ou slug já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequestUpdate request) {
        return categoryService.updateCategory(id, request);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ativa uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria ativada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Categoria já está ativa",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void updateActive(@PathVariable UUID id) {
        categoryService.activeCategory(id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desativa uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria desativada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Categoria já está desativada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void updateDesative(@PathVariable UUID id) {
        categoryService.desativeCategory(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Busca uma categoria pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse findById(@Parameter(description = "ID da categoria",
            example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        return categoryService.findCategoryId(id);
    }

    @GetMapping("/slug")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Busca uma categoria pelo slug, incluindo inativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse findSlug(@Parameter(description = "Slug da categoria",
            example = "cafe-da-manha") @RequestParam String slug) {
        return categoryService.findCategorySlug(slug);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todas as categorias, incluindo inativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de categorias"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores")
    })
    public Page<CategoryResponse> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable
    ) {
        return categoryService.findAllCategories(pageable);
    }
}
