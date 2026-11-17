package dev.jose.jcommerce.catalog.service;

import dev.jose.jcommerce.catalog.domain.Category;
import dev.jose.jcommerce.catalog.domain.Product;
import dev.jose.jcommerce.catalog.dto.ProductFilterRequest;
import dev.jose.jcommerce.catalog.dto.ProductRequest;
import dev.jose.jcommerce.catalog.dto.ProductRequestUpdate;
import dev.jose.jcommerce.catalog.dto.ProductResponse;
import dev.jose.jcommerce.catalog.exception.InactiveCategoryException;
import dev.jose.jcommerce.catalog.mapper.ProductMapper;
import dev.jose.jcommerce.catalog.repository.CategoryRepository;
import dev.jose.jcommerce.catalog.repository.ProductRepository;
import dev.jose.jcommerce.catalog.specification.ProductSpecification;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;


    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    //Metodos administrativos
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        duplicateSlugCheck(productRequest.slug());

        var category = findActiveCategoryById(productRequest.categoryId());

        try {
            var product = productRepository.saveAndFlush(productMapper.toEntity(productRequest, category));
            return productMapper.toResponse(product);
        }catch (DataIntegrityViolationException ex) {
            if (isProductSlugUniqueViolation(ex)) {
                throw new ResourceAlreadyRegistered(
                        "Produto com o slug " + productRequest.slug() + " já cadastrado.");
            }
            throw ex;
        }
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequestUpdate productRequest) {
        var product = productRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        productMapper.applyUpdate(productRequest, product);

        if (productRequest.categoryId() != null) {
            var category = findActiveCategoryById(productRequest.categoryId());
            product.setCategory(category);
        }

        checkUpdateProductDuplicate(id, product);

        try {
            productRepository.saveAndFlush(product);
            return productMapper.toResponse(product);
        }catch (DataIntegrityViolationException ex) {
            if (isProductSlugUniqueViolation(ex)) {
                throw new ResourceAlreadyRegistered(
                        "Produto com o slug " + product.getSlug() + " já cadastrado.");
            }
            throw ex;
        }
    }

    @Transactional
    public void activeProduct(UUID id)  {
        var product = productRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        if(Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceAlreadyActive("produto");
        }

        if (!Boolean.TRUE.equals(product.getCategory().getActive())) {
            throw new InactiveCategoryException();
        }

        product.setActive(true);
    }

    @Transactional
    public void deactivateProduct(UUID id) {
        var product = productRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ResourceAlreadyDesative("produto");
        }

        product.setActive(false);
    }

    public ProductResponse findProductById(UUID id) {
        var product = productRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return productMapper.toResponse(product);
    }

    public ProductResponse findProductBySlug(String slug) {
        return productRepository.findBySlug(SlugNormalize.normalizeSlug(slug))
                .map(productMapper::toResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public Page<ProductResponse> findAllProducts(ProductFilterRequest filter, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.adminFilters(filter), pageable)
                .map(productMapper::toResponse);
    }



    //Metodos publicos
    public Page<ProductResponse> findAllProductsActives(ProductFilterRequest filter, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.publicFilters(filter), pageable)
                .map(productMapper::toResponse);
    }

    public ProductResponse findById(UUID id) {
        return productMapper.toResponse(findActiveProductById(id));
    }

    public ProductResponse findProductActiveBySlug(String slug) {
        return productRepository.findBySlugAndActiveTrueAndCategory_ActiveTrue(
                SlugNormalize.normalizeSlug(slug))
                .map(productMapper::toResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }


    //Metodos privados
    private Product findActiveProductById(UUID id) {
        var product = productRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        if (!Boolean.TRUE.equals(product.getActive()) ||
                !Boolean.TRUE.equals(product.getCategory().getActive())) {
            throw new ResourceNotFoundException();

        }

        return product;
    }

    private Category findActiveCategoryById(UUID id) {
        var category = categoryRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        if  (!Boolean.TRUE.equals(category.getActive())) {
            throw new InactiveCategoryException();
        }

        return category;
    }

    private void duplicateSlugCheck(String slug) {
        if (slug != null && productRepository.existsBySlug(slug)) {
            throw new ResourceAlreadyRegistered("Produto com o slug " + slug + " já cadastrado.");
        }
    }

    private boolean isProductSlugUniqueViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return "uk_product_slug".equals(violation.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void checkUpdateProductDuplicate(UUID id, Product product) {
        if (product.getSlug() != null && productRepository.existsBySlugAndIdNot(product.getSlug(), id)) {
            throw new ResourceAlreadyRegistered(
                    "Produto com o slug " + product.getSlug() + " já cadastrado.");
        }
    }
}
