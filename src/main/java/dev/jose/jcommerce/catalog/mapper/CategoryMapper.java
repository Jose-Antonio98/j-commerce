package dev.jose.jcommerce.catalog.mapper;

import dev.jose.jcommerce.catalog.domain.Category;
import dev.jose.jcommerce.catalog.dto.CategoryRequest;
import dev.jose.jcommerce.catalog.dto.CategoryRequestUpdate;
import dev.jose.jcommerce.catalog.dto.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest categoryRequest) {
        var category = new Category();
        category.setName(categoryRequest.name());
        category.setSlug(categoryRequest.slug());
        return category;
    }

    public Category applyUpdate(CategoryRequestUpdate categoryRequest, Category category) {
        if (categoryRequest.name() != null) category.setName(categoryRequest.name());
        if (categoryRequest.slug() != null) category.setSlug(categoryRequest.slug());
        return category;
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getActive()
        );
    }


}
