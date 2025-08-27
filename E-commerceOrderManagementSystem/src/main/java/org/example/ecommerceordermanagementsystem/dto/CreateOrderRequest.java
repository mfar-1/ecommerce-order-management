package org.example.ecommerceordermanagementsystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "Customer name cannot be empty")
    private String customerName;

    @NotBlank(message = "Customer email cannot be empty")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemRequest> orderItems;
}