package dev.jose.jcommerce.catalog.service;

import dev.jose.jcommerce.catalog.domain.Category;
import dev.jose.jcommerce.catalog.dto.CategoryRequest;
import dev.jose.jcommerce.catalog.dto.CategoryRequestUpdate;
import dev.jose.jcommerce.catalog.dto.CategoryResponse;
import dev.jose.jcommerce.catalog.mapper.CategoryMapper;
import dev.jose.jcommerce.catalog.repository.CategoryRepository;
import dev.jose.jcommerce.exception.ResourceAlreadyActive;
import dev.jose.jcommerce.exception.ResourceAlreadyDesative;
import dev.jose.jcommerce.exception.ResourceAlreadyRegistered;
import dev.jose.jcommerce.exception.ResourceNotFoundException;
import dev.jose.jcommerce.util.SlugNormalize;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        duplicateNameCheck(categoryRequest.name());
        duplicateSlugCheck(categoryRequest.slug());

        try{
            var category = categoryRepository.saveAndFlush(categoryMapper.toEntity(categoryRequest));
            return categoryMapper.toResponse(category);
        } catch (DataIntegrityViolationException ex){
            if(isCategoryNameOrSlugUniqueViolation(ex)){
                throw new ResourceAlreadyRegistered("Categoria");
            }
            throw ex;
        }
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequestUpdate categoryRequest) {
        var category = findById(id);
        categoryMapper.applyUpdate(categoryRequest, category);

        checkUpdateCategoryDuplicate(id, category);

        try {
            var updatedCategory = categoryRepository.saveAndFlush(category);
            return categoryMapper.toResponse(updatedCategory);
        } catch (DataIntegrityViolationException ex){
            if(isCategoryNameOrSlugUniqueViolation(ex)){
                throw new ResourceAlreadyRegistered("Categoria");
            }
            throw ex;
        }
    }

    @Transactional
    public void activeCategory(UUID id)  {
        var category = findById(id);

        if(Boolean.TRUE.equals(category.getActive())) {
            throw new ResourceAlreadyActive("categoria");
        }

        category.setActive(true);
    }

    @Transactional
    public void desativeCategory(UUID id)  {
        var category = findById(id);

        if(Boolean.FALSE.equals(category.getActive())) {
            throw new ResourceAlreadyDesative("categoria");
        }

        category.setActive(false);
    }


    public CategoryResponse findCategoryId(UUID id) {
        return categoryMapper.toResponse(findById(id));
    }

    public CategoryResponse findCategorySlug(String slug) {
        return categoryRepository.findBySlug(SlugNormalize.normalizeSlug(slug))
                .map(categoryMapper::toResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Page<CategoryResponse> findAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    public CategoryResponse findCategorySlugActiveTrue(String slug) {
        return categoryRepository.findBySlugAndActiveTrue(SlugNormalize.normalizeSlug(slug))
                .map(categoryMapper::toResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }



    public Page<CategoryResponse> findActiveCategories(Pageable pageable) {
        return categoryRepository.findByActiveTrue(pageable)
                .map(categoryMapper::toResponse);
    }




    private boolean isCategoryNameOrSlugUniqueViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return "uk_category_name".equals(violation.getConstraintName())
                        || "uk_category_slug".equals(violation.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void duplicateNameCheck(String categoryName) {
        if (categoryName != null && categoryRepository.existsByNameIgnoreCase(categoryName)) {
            throw new ResourceAlreadyRegistered(
                    "categoria com o nome " + categoryName + " já cadastrada.");
        }
    }

    private void duplicateSlugCheck(String slug) {
        if (slug != null && categoryRepository.existsBySlug(slug)) {
            throw new ResourceAlreadyRegistered("categoria com o slug " + slug + " já cadastrada.");
        }
    }

    private void checkUpdateCategoryDuplicate(UUID id, Category category) {
        if (category.getName() != null && categoryRepository.existsByNameIgnoreCaseAndIdNot(category.getName(), id)) {
            throw new ResourceAlreadyRegistered(
                    "categoria com o nome " + category.getName() + " já cadastrada.");
        }

        if (category.getSlug() != null && categoryRepository.existsBySlugAndIdNot(category.getSlug(), id)) {
            throw new ResourceAlreadyRegistered(
                    "categoria com o slug " + category.getSlug() + " já cadastrada.");
        }

    }

    private Category findById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
