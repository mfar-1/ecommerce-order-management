package org.example.ecommerceordermanagementsystem.dto;

import lombok.Builder;
import lombok.Data;
import org.example.ecommerceordermanagementsystem.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}