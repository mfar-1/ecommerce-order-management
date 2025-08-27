package org.example.ecommerceordermanagementsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Product name cannot be empty")
    private String name;

    @NotNull(message = "Product price cannot be null")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Product stock cannot be null")
    @Min(value = 0, message = "Product stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Product category cannot be empty")
    private String category;

    private Boolean isActive;
}