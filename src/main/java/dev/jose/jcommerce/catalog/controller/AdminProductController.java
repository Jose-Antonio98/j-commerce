package dev.jose.jcommerce.catalog.controller;

import dev.jose.jcommerce.catalog.dto.ProductFilterRequest;
import dev.jose.jcommerce.catalog.dto.ProductRequest;
import dev.jose.jcommerce.catalog.dto.ProductRequestUpdate;
import dev.jose.jcommerce.catalog.dto.ProductResponse;
import dev.jose.jcommerce.catalog.service.ProductService;
import dev.jose.jcommerce.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/catalog/products")
@Tag(name = "Administração de produtos", description = "Operações administrativas de produtos; requer role ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um Produto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "409", description = "Nome ou slug já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza parcialmente um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Nome ou slug já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequestUpdate request) {
        return productService.updateProduct(id, request);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ativa um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produtos ativado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Produto já está ativo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void updateActive(@PathVariable UUID id) {
        productService.activeProduct(id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desativa um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto desativado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Produto já está desativado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void updateDeactive(@PathVariable UUID id) {
        productService.deactivateProduct(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Busca um produto pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "produto encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductResponse findById(@Parameter(description = "ID da categoria",
            example = "123e4567-e89b-12d3-a456-426614174000")@PathVariable UUID id) {
        return productService.findProductById(id);
    }

    @GetMapping("/slug")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Busca um produto pelo slug, incluindo inativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "produto encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores"),
            @ApiResponse(responseCode = "404", description = "produto não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductResponse findBySlug(
            @Parameter(description = "Slug do produto", example = "maquina-de-lavar")
            @RequestParam String slug
    ) {
        return  productService.findProductBySlug(slug);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os produtos, incluindo inativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de categorias"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente a administradores")
    })
    public Page<ProductResponse> findAllProducts(ProductFilterRequest filter, Pageable pageable) {
        return productService.findAllProducts(filter, pageable);
    }

}
