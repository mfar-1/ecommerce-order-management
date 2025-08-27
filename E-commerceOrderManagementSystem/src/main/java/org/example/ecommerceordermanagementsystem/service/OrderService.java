package org.example.ecommerceordermanagementsystem.service;

import org.example.ecommerceordermanagementsystem.dto.CreateOrderRequest;
import org.example.ecommerceordermanagementsystem.dto.OrderResponse;
import org.example.ecommerceordermanagementsystem.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface OrderService {
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse getOrderById(Long id);
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);
    void cancelOrder(Long id);
    List<OrderResponse> getOrdersByCustomerEmail(String customerEmail);
}