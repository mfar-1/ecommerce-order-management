package org.example.ecommerceordermanagementsystem.service;

import org.example.ecommerceordermanagementsystem.dto.CreateProductRequest;
import org.example.ecommerceordermanagementsystem.dto.ProductResponse;
import org.example.ecommerceordermanagementsystem.dto.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
    Page<ProductResponse> searchProducts(String name, String category, Pageable pageable);
}