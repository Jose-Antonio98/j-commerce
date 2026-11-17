package dev.jose.jcommerce.catalog.mapper;

import dev.jose.jcommerce.catalog.domain.Category;
import dev.jose.jcommerce.catalog.domain.Product;
import dev.jose.jcommerce.catalog.dto.ProductRequest;
import dev.jose.jcommerce.catalog.dto.ProductRequestUpdate;
import dev.jose.jcommerce.catalog.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest productRequest, Category category) {
        var product = new Product();
        product.setName(productRequest.name());
        product.setSlug(productRequest.slug());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        product.setQuantity(productRequest.quantity());
        product.setImage(productRequest.image());
        product.setCategory(category);
        return product;
    }

    public void applyUpdate(ProductRequestUpdate productRequest, Product product) {
        if (productRequest.name() != null) product.setName(productRequest.name());
        if (productRequest.slug() != null) product.setSlug(productRequest.slug());
        if (productRequest.description() != null) product.setDescription(productRequest.description());
        if (productRequest.price() != null) product.setPrice(productRequest.price());
        if (productRequest.quantity() != null) product.setQuantity(productRequest.quantity());
        if (productRequest.image() != null) product.setImage(productRequest.image());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
               product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getActive(),
                product.getImage(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}

