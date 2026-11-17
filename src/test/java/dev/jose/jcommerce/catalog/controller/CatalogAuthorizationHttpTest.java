package dev.jose.jcommerce.catalog.controller;

import dev.jose.jcommerce.auth.service.CustomUserDetailsService;
import dev.jose.jcommerce.catalog.dto.CategoryResponse;
import dev.jose.jcommerce.catalog.dto.ProductResponse;
import dev.jose.jcommerce.catalog.service.CategoryService;
import dev.jose.jcommerce.catalog.service.ProductService;
import dev.jose.jcommerce.config.JwtConfiguration;
import dev.jose.jcommerce.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                ProductController.class,
                AdminProductController.class,
                CategoryController.class,
                AdminCategoryController.class
        },
        properties = {
                "app.security.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
                "app.security.jwt.access-token-ttl=15m"
        }
)
@Import({SecurityConfig.class, JwtConfiguration.class})
@EnableWebSecurity
class CatalogAuthorizationHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void publicProductListIsAccessibleWithoutToken() throws Exception {
        when(productService.findAllProductsActives(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/catalog/products"))
                .andExpect(status().isOk());

        verify(productService).findAllProductsActives(any(), any());
    }

    @Test
    void publicProductDetailsByIdAndSlugAreAccessibleWithoutToken() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id)).thenReturn(productResponse(id));
        when(productService.findProductActiveBySlug("cafeteira")).thenReturn(productResponse(id));

        mockMvc.perform(get("/catalog/products/{id}", id))
                .andExpect(status().isOk());
        mockMvc.perform(get("/catalog/products/slug").param("slug", "cafeteira"))
                .andExpect(status().isOk());

        verify(productService).findById(id);
        verify(productService).findProductActiveBySlug("cafeteira");
    }

    @Test
    void publicCategoryRoutesAreAccessibleWithoutToken() throws Exception {
        when(categoryService.findActiveCategories(any())).thenReturn(Page.empty());
        UUID id = UUID.randomUUID();
        when(categoryService.findCategorySlugActiveTrue("casa"))
                .thenReturn(new CategoryResponse(id, "Casa", "casa", true));

        mockMvc.perform(get("/catalog/categories"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/catalog/categories/slug").param("slug", "casa"))
                .andExpect(status().isOk());
    }

    @Test
    void adminProductRoutesRejectMissingTokenAndCustomerRole() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/admin/catalog/products/{id}", id))
                .andExpect(status().isUnauthorized());

        String customerToken = createToken("CUSTOMER");
        mockMvc.perform(get("/admin/catalog/products/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessProductSlugAndCategoryOperations() throws Exception {
        UUID id = UUID.randomUUID();
        String adminToken = createToken("ADMIN");
        when(productService.findProductBySlug("cafeteira")).thenReturn(productResponse(id));
        when(categoryService.findCategoryId(id))
                .thenReturn(new CategoryResponse(id, "Casa", "casa", false));

        mockMvc.perform(get("/admin/catalog/products/slug").param("slug", "cafeteira")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/catalog/categories/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());

        verify(productService).findProductBySlug("cafeteira");
        verify(categoryService).findCategoryId(id);
    }

    @Test
    void adminCategoryRouteRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/admin/catalog/categories"))
                .andExpect(status().isUnauthorized());
    }

    private String createToken(String role) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("j-commerce")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject("catalog-test@example.com")
                .claim("roles", List.of(role))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static ProductResponse productResponse(UUID id) {
        return new ProductResponse(
                id,
                "Cafeteira",
                "cafeteira",
                "Cafeteira elétrica",
                new BigDecimal("129.90"),
                5,
                true,
                null,
                UUID.randomUUID(),
                "Eletrodomésticos"
        );
    }
}
