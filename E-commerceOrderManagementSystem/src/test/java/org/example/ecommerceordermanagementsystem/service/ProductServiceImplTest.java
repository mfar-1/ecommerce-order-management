package org.example.ecommerceordermanagementsystem.service;

import org.example.ecommerceordermanagementsystem.dto.CreateProductRequest;
import org.example.ecommerceordermanagementsystem.dto.ProductResponse;
import org.example.ecommerceordermanagementsystem.dto.UpdateProductRequest;
import org.example.ecommerceordermanagementsystem.entity.Product;
import org.example.ecommerceordermanagementsystem.exception.ProductNotFoundException;
import org.example.ecommerceordermanagementsystem.repository.ProductRepository;
import org.example.ecommerceordermanagementsystem.service.Impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private CreateProductRequest createProductRequest;
    private UpdateProductRequest updateProductRequest;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Laptop", BigDecimal.valueOf(1200.00), 10, "Electronics", true, LocalDateTime.now());

        createProductRequest = new CreateProductRequest();
        createProductRequest.setName("New Phone");
        createProductRequest.setPrice(BigDecimal.valueOf(700.00));
        createProductRequest.setStock(20);
        createProductRequest.setCategory("Electronics");
        createProductRequest.setIsActive(true);

        updateProductRequest = new UpdateProductRequest();
        updateProductRequest.setName("Updated Laptop");
        updateProductRequest.setPrice(BigDecimal.valueOf(1250.00));
        updateProductRequest.setStock(8);
        updateProductRequest.setCategory("Electronics");
        updateProductRequest.setIsActive(true);
    }

    @Test
    @DisplayName("Should return all products with pagination")
    void getAllProducts_shouldReturnPageOfProductResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(pageable); // findAll() metodi 1 marta chaqirilganini tekshirish
    }

    @Test
    @DisplayName("Should return product by ID when product exists")
    void getProductById_shouldReturnProductResponse_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product does not exist")
    void getProductById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(2L));
        verify(productRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should create a new product successfully")
    void createProduct_shouldReturnNewProductResponse() {
        Product newProduct = new Product(null, createProductRequest.getName(), createProductRequest.getPrice(),
                createProductRequest.getStock(), createProductRequest.getCategory(), createProductRequest.getIsActive(), null);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse result = productService.createProduct(createProductRequest);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update an existing product successfully")
    void updateProduct_shouldReturnUpdatedProductResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse result = productService.updateProduct(1L, updateProductRequest);

        assertNotNull(result);
        assertEquals("Updated Laptop", result.getName());
        assertEquals(BigDecimal.valueOf(1250.00), result.getPrice());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating non-existent product")
    void updateProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(2L, updateProductRequest));
        verify(productRepository, times(1)).findById(2L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_shouldDeleteProduct_whenProductExists() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
    void deleteProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.existsById(2L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(2L));
        verify(productRepository, times(1)).existsById(2L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should search products by name and category")
    void searchProducts_shouldReturnFilteredProducts_byNameAndCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(
                "Laptop", "Electronics", pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.searchProducts("Laptop", "Electronics", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(
                "Laptop", "Electronics", pageable);
    }

    @Test
    @DisplayName("Should search products by name only")
    void searchProducts_shouldReturnFilteredProducts_byNameOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByNameContainingIgnoreCase("Laptop", pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.searchProducts("Laptop", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Laptop", pageable);
    }

    @Test
    @DisplayName("Should search products by category only")
    void searchProducts_shouldReturnFilteredProducts_byCategoryOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByCategoryContainingIgnoreCase("Electronics", pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.searchProducts(null, "Electronics", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findByCategoryContainingIgnoreCase("Electronics", pageable);
    }

    @Test
    @DisplayName("Should return active products when no search criteria provided")
    void searchProducts_shouldReturnActiveProducts_whenNoCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByIsActiveTrue(pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.searchProducts(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findByIsActiveTrue(pageable);
    }
}