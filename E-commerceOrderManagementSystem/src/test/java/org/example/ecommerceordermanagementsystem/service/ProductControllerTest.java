package org.example.ecommerceordermanagementsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ecommerceordermanagementsystem.controller.ProductController;
import org.example.ecommerceordermanagementsystem.dto.CreateProductRequest;
import org.example.ecommerceordermanagementsystem.dto.ProductResponse;
import org.example.ecommerceordermanagementsystem.dto.UpdateProductRequest;
import org.example.ecommerceordermanagementsystem.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse productResponse;
    private CreateProductRequest createProductRequest;
    private UpdateProductRequest updateProductRequest;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(1200.00))
                .stock(10)
                .category("Electronics")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

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
    @DisplayName("GET /api/products - Should return all products with pagination")
    void getAllProducts_shouldReturnPageOfProductResponses() throws Exception {
        Page<ProductResponse> productPage = new PageImpl<>(Collections.singletonList(productResponse));
        when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(productResponse.getId()))
                .andExpect(jsonPath("$.content[0].name").value(productResponse.getName()));

        verify(productService, times(1)).getAllProducts(any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return product by ID")
    void getProductById_shouldReturnProductResponse() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productResponse.getId()))
                .andExpect(jsonPath("$.name").value(productResponse.getName()));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 404 if product not found")
    void getProductById_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        when(productService.getProductById(anyLong())).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService, times(1)).getProductById(99L);
    }

    @Test
    @DisplayName("POST /api/products - Should create a new product")
    void createProduct_shouldReturnCreatedProductResponse() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productResponse.getId()))
                .andExpect(jsonPath("$.name").value(productResponse.getName()));

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 if validation fails")
    void createProduct_shouldReturnBadRequest_whenValidationFails() throws Exception {
        createProductRequest.setName(null); // Invalid request
        createProductRequest.setPrice(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists());

        verify(productService, never()).createProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should update an existing product")
    void updateProduct_shouldReturnUpdatedProductResponse() throws Exception {
        when(productService.updateProduct(anyLong(), any(UpdateProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productResponse.getId()))
                .andExpect(jsonPath("$.name").value(productResponse.getName()));

        verify(productService, times(1)).updateProduct(anyLong(), any(UpdateProductRequest.class));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should return 404 if product to update not found")
    void updateProduct_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        when(productService.updateProduct(anyLong(), any(UpdateProductRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(put("/api/products/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProductRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService, times(1)).updateProduct(anyLong(), any(UpdateProductRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should delete product successfully")
    void deleteProduct_shouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L); // Service void qaytaradi

        mockMvc.perform(delete("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should return 404 if product to delete not found")
    void deleteProduct_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        doThrow(new ProductNotFoundException("Product not found")).when(productService).deleteProduct(anyLong());

        mockMvc.perform(delete("/api/products/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService, times(1)).deleteProduct(99L);
    }

    @Test
    @DisplayName("GET /api/products/search - Should search products by name and category")
    void searchProducts_shouldReturnFilteredProducts() throws Exception {
        Page<ProductResponse> productPage = new PageImpl<>(Collections.singletonList(productResponse));
        when(productService.searchProducts(anyString(), anyString(), any(PageRequest.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products/search")
                        .param("name", "Laptop")
                        .param("category", "Electronics")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value(productResponse.getName()));

        verify(productService, times(1)).searchProducts(anyString(), anyString(), any(PageRequest.class));
    }
}