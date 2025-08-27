package org.example.ecommerceordermanagementsystem.service.Impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceordermanagementsystem.dto.CreateProductRequest;
import org.example.ecommerceordermanagementsystem.dto.ProductResponse;
import org.example.ecommerceordermanagementsystem.dto.UpdateProductRequest;
import org.example.ecommerceordermanagementsystem.entity.Product;
import org.example.ecommerceordermanagementsystem.exception.ProductNotFoundException;
import org.example.ecommerceordermanagementsystem.repository.ProductRepository;
import org.example.ecommerceordermanagementsystem.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: {}", pageable);
        return productRepository.findAll(pageable).map(ProductResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found", id);
                    return new ProductNotFoundException("Product with ID " + id + " not found");
                });
        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found for update", id);
                    return new ProductNotFoundException("Product with ID " + id + " not found");
                });

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setIsActive(request.getIsActive());

        Product updatedProduct = productRepository.save(product);
        log.info("Product with ID {} updated successfully", updatedProduct.getId());
        return ProductResponse.fromEntity(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Attempting to delete product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Product with ID {} not found for deletion", id);
            throw new ProductNotFoundException("Product with ID " + id + " not found");
        }
        productRepository.deleteById(id);
        log.info("Product with ID {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, String category, Pageable pageable) {
        log.info("Searching products by name '{}' and category '{}' with pagination: {}", name, category, pageable);
        if (name != null && category != null) {
            return productRepository.findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(name, category, pageable)
                    .map(ProductResponse::fromEntity);
        } else if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable)
                    .map(ProductResponse::fromEntity);
        } else if (category != null) {
            return productRepository.findByCategoryContainingIgnoreCase(category, pageable)
                    .map(ProductResponse::fromEntity);
        } else {
            return productRepository.findByIsActiveTrue(pageable)
                    .map(ProductResponse::fromEntity);
        }
    }
}