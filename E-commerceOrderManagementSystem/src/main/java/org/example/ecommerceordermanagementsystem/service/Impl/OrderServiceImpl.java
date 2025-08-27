package org.example.ecommerceordermanagementsystem.service.Impl;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceordermanagementsystem.dto.CreateOrderRequest;
import org.example.ecommerceordermanagementsystem.dto.OrderItemRequest;
import org.example.ecommerceordermanagementsystem.dto.OrderResponse;
import org.example.ecommerceordermanagementsystem.entity.Order;
import org.example.ecommerceordermanagementsystem.entity.OrderItem;
import org.example.ecommerceordermanagementsystem.entity.Product;
import org.example.ecommerceordermanagementsystem.enums.OrderStatus;
import org.example.ecommerceordermanagementsystem.exception.InsufficientStockException;
import org.example.ecommerceordermanagementsystem.exception.InvalidOrderStatusException;
import org.example.ecommerceordermanagementsystem.exception.OrderNotFoundException;
import org.example.ecommerceordermanagementsystem.exception.ProductNotFoundException;
import org.example.ecommerceordermanagementsystem.repository.OrderRepository;
import org.example.ecommerceordermanagementsystem.repository.ProductRepository;
import org.example.ecommerceordermanagementsystem.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders with pagination: {}", pageable);
        return orderRepository.findAll(pageable).map(OrderResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found", id);
                    return new OrderNotFoundException("Order with ID " + id + " not found");
                });
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Initiating new order creation for customer: {}", request.getCustomerEmail());

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setStatus(OrderStatus.PENDING);

        Set<Long> productIdsInOrder = new HashSet<>();

        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            if (!productIdsInOrder.add(itemRequest.getProductId())) {
                log.error("Duplicate product ID {} found in order request for order: {}", itemRequest.getProductId(), request.getCustomerEmail());
                throw new IllegalArgumentException("Product with ID " + itemRequest.getProductId() + " already exists in this order.");
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> {
                        log.warn("Product with ID {} not found during order creation", itemRequest.getProductId());
                        return new ProductNotFoundException("Product with ID " + itemRequest.getProductId() + " not found.");
                    });

            if (!product.getIsActive() || product.getStock() <= 0) {
                log.error("Product {} is inactive or out of stock during order creation", product.getName());
                throw new InsufficientStockException("Product " + product.getName() + " is currently out of stock or inactive.");
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                log.error("Insufficient stock for product {} (requested: {}, available: {})", product.getName(), itemRequest.getQuantity(), product.getStock());
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStock() + ", Requested: " + itemRequest.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Updating status for order ID {} to {}", id, newStatus);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found for status update", id);
                    return new OrderNotFoundException("Order with ID " + id + " not found");
                });

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
            log.error("Cannot change order status from {} to {} for order ID {}. Only PENDING or CONFIRMED orders can be updated to other statuses (except CANCELLED from any status)", order.getStatus(), newStatus, id);
            throw new InvalidOrderStatusException("Order status can only be changed from PENDING or CONFIRMED. Current status: " + order.getStatus());
        }

        if (newStatus == OrderStatus.CONFIRMED && order.getStatus() == OrderStatus.PENDING) {
            log.info("Confirming order ID {}. Reducing product stock.", id);
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product.getStock() < item.getQuantity()) {
                    log.error("Insufficient stock for product {} during order confirmation (requested: {}, available: {})", product.getName(), item.getQuantity(), product.getStock());
                    throw new InsufficientStockException("Insufficient stock for product: " + product.getName() + " to confirm order. Available: " + product.getStock() + ", Requested: " + item.getQuantity());
                }
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
                log.debug("Reduced stock for product {} by {}", product.getName(), item.getQuantity());
            }
        } else if (newStatus == OrderStatus.CANCELLED && (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.SHIPPED)) {
            log.info("Cancelling order ID {}. Returning product stock.", id);
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product); // Return stock
                log.debug("Returned stock for product {} by {}", product.getName(), item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order ID {} status updated to {}", id, newStatus);
        return OrderResponse.fromEntity(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.info("Attempting to cancel order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order with ID {} not found for cancellation", id);
                    return new OrderNotFoundException("Order with ID " + id + " not found");
                });

        if (order.getStatus() == OrderStatus.DELIVERED) {
            log.error("Cannot cancel a DELIVERED order with ID: {}", id);
            throw new InvalidOrderStatusException("Cannot cancel a delivered order.");
        }

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.SHIPPED) {
            log.info("Order ID {} was CONFIRMED/SHIPPED, returning product stock upon cancellation.", id);
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
                log.debug("Returned stock for product {} by {}", product.getName(), item.getQuantity());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order with ID {} cancelled successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerEmail(String customerEmail) {
        log.info("Fetching orders for customer email: {}", customerEmail);
        return orderRepository.findByCustomerEmail(customerEmail)
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }
}