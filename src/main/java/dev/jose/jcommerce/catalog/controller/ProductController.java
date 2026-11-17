package dev.jose.jcommerce.catalog.controller;

import dev.jose.jcommerce.catalog.dto.ProductFilterRequest;
import dev.jose.jcommerce.catalog.dto.ProductResponse;
import dev.jose.jcommerce.catalog.service.ProductService;
import dev.jose.jcommerce.exception.ErrorResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/catalog/products")
@Tag(name = "Produtos", description = "Consultas públicas de produtos ativos.")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lista produtos ativos")
    @ApiResponse(responseCode = "200", description = "Página de produtos ativos")
    public Page<ProductResponse> findAllActiveProducts(
            @ParameterObject ProductFilterRequest filter,
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return productService.findAllProductsActives(filter, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto ativo pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ID ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto ativa não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProductResponse findActiveProductById(@Parameter(description = "ID da categoria",
            example = "123e4567-e89b-12d3-a456-426614174000")@PathVariable UUID id){
        return productService.findById(id);
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
    public ProductResponse findActiveSlug(
            @Parameter(description = "Slug do produto", example = "lava-loucas")
            @RequestParam("slug") String slug){
        return productService.findProductActiveBySlug(slug);
    }
}
