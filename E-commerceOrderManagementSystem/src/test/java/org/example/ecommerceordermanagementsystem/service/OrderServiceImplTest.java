package org.example.ecommerceordermanagementsystem.service;

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
import org.example.ecommerceordermanagementsystem.service.Impl.OrderServiceImpl;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product1, product2;
    private Order order;
    private OrderItem orderItem1, orderItem2;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Laptop", BigDecimal.valueOf(1200.00), 10, "Electronics", true, LocalDateTime.now());
        product2 = new Product(2L, "Mouse", BigDecimal.valueOf(25.00), 5, "Electronics", true, LocalDateTime.now());

        order = new Order(1L, "John Doe", "john@example.com", LocalDateTime.now(), OrderStatus.PENDING, BigDecimal.valueOf(1225.00), new java.util.ArrayList<>());

        orderItem1 = new OrderItem(1L, order, product1, 1, BigDecimal.valueOf(1200.00), BigDecimal.valueOf(1200.00));
        orderItem2 = new OrderItem(2L, order, product2, 1, BigDecimal.valueOf(25.00), BigDecimal.valueOf(25.00));
        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);

        OrderItemRequest itemRequest1 = new OrderItemRequest(1L, 1);
        OrderItemRequest itemRequest2 = new OrderItemRequest(2L, 1);
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerName("Jane Doe");
        createOrderRequest.setCustomerEmail("jane@example.com");
        createOrderRequest.setOrderItems(Arrays.asList(itemRequest1, itemRequest2));
    }

    @Test
    @DisplayName("Should return all orders with pagination")
    void getAllOrders_shouldReturnPageOfOrderResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Collections.singletonList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getCustomerName());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return order by ID when order exists")
    void getOrderById_shouldReturnOrderResponse_whenOrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getCustomerName());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void getOrderById_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(2L));
        verify(orderRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should create a new order successfully")
    void createOrder_shouldReturnNewOrderResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse result = orderService.createOrder(createOrderRequest);

        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(2, result.getOrderItems().size());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(2L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when creating order with non-existent product")
    void createOrder_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(createOrderRequest));
        verify(productRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when creating order with insufficient stock")
    void createOrder_shouldThrowInsufficientStockException_whenInsufficientStock() {
        product1.setStock(0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(createOrderRequest));
        verify(productRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when creating order with inactive product")
    void createOrder_shouldThrowInsufficientStockException_whenProductInactive() {
        product1.setIsActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(createOrderRequest));
        verify(productRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating order with duplicate product items")
    void createOrder_shouldThrowIllegalArgumentException_whenDuplicateProductItems() {
        OrderItemRequest itemRequest1 = new OrderItemRequest(1L, 1);
        OrderItemRequest itemRequestDuplicate = new OrderItemRequest(1L, 2);
        createOrderRequest.setOrderItems(Arrays.asList(itemRequest1, itemRequestDuplicate));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(createOrderRequest));
        verify(productRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should confirm order and reduce stock when status changes from PENDING to CONFIRMED")
    void updateOrderStatus_shouldConfirmOrderAndReduceStock() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.save(any(Product.class))).thenReturn(product1, product2);

        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(2)).save(any(Product.class));
        assertEquals(9, product1.getStock());
        assertEquals(4, product2.getStock());
    }

    @Test
    @DisplayName("Should cancel order and return stock when status changes from CONFIRMED to CANCELLED")
    void updateOrderStatus_shouldCancelOrderAndReturnStock_fromConfirmed() {
        order.setStatus(OrderStatus.CONFIRMED);
        product1.setStock(9);
        product2.setStock(4);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.save(any(Product.class))).thenReturn(product1, product2);

        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(2)).save(any(Product.class));
        assertEquals(10, product1.getStock());
        assertEquals(5, product2.getStock());
    }

    @Test
    @DisplayName("Should cancel order and return stock when status changes from SHIPPED to CANCELLED")
    void updateOrderStatus_shouldCancelOrderAndReturnStock_fromShipped() {
        order.setStatus(OrderStatus.SHIPPED);
        product1.setStock(9);
        product2.setStock(4);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.save(any(Product.class))).thenReturn(product1, product2);

        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(2)).save(any(Product.class));
        assertEquals(10, product1.getStock());
        assertEquals(5, product2.getStock());
    }

    @Test
    @DisplayName("Should throw InvalidOrderStatusException when trying to change status from DELIVERED")
    void updateOrderStatus_shouldThrowInvalidOrderStatusException_whenDelivered() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.SHIPPED));
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when confirming order with insufficient stock")
    void updateOrderStatus_shouldThrowInsufficientStockException_onConfirmWithInsufficientStock() {
        order.setStatus(OrderStatus.PENDING);
        product1.setStock(0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InsufficientStockException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
    }


    @Test
    @DisplayName("Should cancel order successfully and return stock if CONFIRMED/SHIPPED")
    void cancelOrder_shouldCancelOrderAndReturnStock() {
        order.setStatus(OrderStatus.CONFIRMED);
        product1.setStock(9);
        product2.setStock(4);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.save(any(Product.class))).thenReturn(product1, product2);

        assertDoesNotThrow(() -> orderService.cancelOrder(1L));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(10, product1.getStock());
        assertEquals(5, product2.getStock());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should cancel order successfully without stock return if PENDING")
    void cancelOrder_shouldCancelOrderWithoutStockReturn_ifPending() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        assertDoesNotThrow(() -> orderService.cancelOrder(1L));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
    }


    @Test
    @DisplayName("Should throw InvalidOrderStatusException when canceling DELIVERED order")
    void cancelOrder_shouldThrowInvalidOrderStatusException_whenDelivered() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> orderService.cancelOrder(1L));
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when canceling non-existent order")
    void cancelOrder_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(2L));
        verify(orderRepository, times(1)).findById(2L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should return all orders by customer email")
    void getOrdersByCustomerEmail_shouldReturnListOfOrderResponses() {
        List<Order> orders = Collections.singletonList(order);
        when(orderRepository.findByCustomerEmail("john@example.com")).thenReturn(orders);

        List<OrderResponse> result = orderService.getOrdersByCustomerEmail("john@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john@example.com", result.get(0).getCustomerEmail());
        verify(orderRepository, times(1)).findByCustomerEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return empty list when no orders found for customer email")
    void getOrdersByCustomerEmail_shouldReturnEmptyList_whenNoOrdersFound() {
        when(orderRepository.findByCustomerEmail("noexist@example.com")).thenReturn(Collections.emptyList());

        List<OrderResponse> result = orderService.getOrdersByCustomerEmail("noexist@example.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByCustomerEmail("noexist@example.com");
    }
}