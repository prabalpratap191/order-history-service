package com.ecommerce.orderhistory.service;

import com.ecommerce.orderhistory.dto.*;
import com.ecommerce.orderhistory.entity.OrderHistory;
import com.ecommerce.orderhistory.enums.OrderEntryType;
import com.ecommerce.orderhistory.enums.OrderType;
import com.ecommerce.orderhistory.event.OrderHistoryEventPublisher;
import com.ecommerce.orderhistory.exception.ResourceNotFoundException;
import com.ecommerce.orderhistory.mapper.OrderHistoryMapper;
import com.ecommerce.orderhistory.repository.OrderHistoryRepository;
import com.ecommerce.orderhistory.service.impl.OrderHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceImplTest {

    @Mock private OrderHistoryRepository orderHistoryRepository;
    @Mock private OrderHistoryMapper orderHistoryMapper;
    @Mock private OrderHistoryEventPublisher orderHistoryEventPublisher;
    private OrderHistoryServiceImpl orderHistoryService;

    @BeforeEach
    void setUp() {
        orderHistoryService = new OrderHistoryServiceImpl(
            orderHistoryRepository, orderHistoryMapper, orderHistoryEventPublisher);
    }

    @Test
    void createOrderHistory_shouldSaveAndReturnDTO() {
        LocalDateTime orderDate = LocalDateTime.now().minusDays(1);
        CreateOrderHistoryRequest request = new CreateOrderHistoryRequest(
            1L, OrderEntryType.CUSTOMER, orderDate, "John Smith",
            OrderType.NEW_ORDER, "Customer placed a new order for widgets",
            "ORD20260723001", false);

        OrderHistory entity = new OrderHistory();
        entity.setCustomerId(1L);
        entity.setOrderEntryType(OrderEntryType.CUSTOMER);
        entity.setOrderDate(orderDate);
        entity.setRepresentative("John Smith");
        entity.setOrderType(OrderType.NEW_ORDER);
        entity.setNote("Customer placed a new order for widgets");
        entity.setOrderNumber("ORD20260723001");

        OrderHistory savedEntity = new OrderHistory();
        savedEntity.setOrderHistoryId(1L);
        savedEntity.setCustomerId(1L);
        savedEntity.setOrderEntryType(OrderEntryType.CUSTOMER);
        savedEntity.setOrderDate(orderDate);
        savedEntity.setRepresentative("John Smith");
        savedEntity.setOrderType(OrderType.NEW_ORDER);
        savedEntity.setNote("Customer placed a new order for widgets");
        savedEntity.setOrderNumber("ORD20260723001");

        OrderHistoryDTO expectedDTO = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, orderDate, "John Smith",
            OrderType.NEW_ORDER, "Customer placed a new order for widgets",
            "ORD20260723001", false, LocalDateTime.now(), null);

        when(orderHistoryMapper.toEntity(request)).thenReturn(entity);
        when(orderHistoryRepository.save(entity)).thenReturn(savedEntity);
        when(orderHistoryMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        OrderHistoryDTO result = orderHistoryService.createOrderHistory(request);

        assertThat(result.orderHistoryId()).isEqualTo(1L);
        assertThat(result.customerId()).isEqualTo(1L);
        assertThat(result.orderType()).isEqualTo(OrderType.NEW_ORDER);
        assertThat(result.representative()).isEqualTo("John Smith");
        verify(orderHistoryRepository).save(entity);
        verify(orderHistoryEventPublisher).publishOrderHistoryCreated(any());
    }

    @Test
    void getCustomerOrderHistory_includeExternal_shouldReturnAll() {
        OrderHistory entry = new OrderHistory();
        entry.setOrderHistoryId(1L);
        entry.setCustomerId(1L);
        entry.setOrderEntryType(OrderEntryType.CUSTOMER);
        entry.setOrderType(OrderType.NEW_ORDER);

        OrderHistoryDTO dto = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, LocalDateTime.now(),
            "John Smith", OrderType.NEW_ORDER, "Test note for order",
            "ORD001", false, LocalDateTime.now(), null);

        when(orderHistoryRepository.findByCustomerIdOrderByOrderDateDesc(1L)).thenReturn(List.of(entry));
        when(orderHistoryMapper.toDTO(entry)).thenReturn(dto);

        ViewOrderHistoryDTO result = orderHistoryService.getCustomerOrderHistory(1L, true);

        assertThat(result.customerId()).isEqualTo(1L);
        assertThat(result.includeExternal()).isTrue();
        assertThat(result.entries()).hasSize(1);
        verify(orderHistoryRepository).findByCustomerIdOrderByOrderDateDesc(1L);
    }

    @Test
    void getCustomerOrderHistory_excludeExternal_shouldFilterEntries() {
        OrderHistory entry = new OrderHistory();
        entry.setOrderHistoryId(1L);
        entry.setCustomerId(1L);
        entry.setExternalFlag(false);

        OrderHistoryDTO dto = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, LocalDateTime.now(),
            "John Smith", OrderType.NEW_ORDER, "Test note for order",
            "ORD001", false, LocalDateTime.now(), null);

        when(orderHistoryRepository.findByCustomerIdAndExternalFlagOrderByOrderDateDesc(1L, false))
            .thenReturn(List.of(entry));
        when(orderHistoryMapper.toDTO(entry)).thenReturn(dto);

        ViewOrderHistoryDTO result = orderHistoryService.getCustomerOrderHistory(1L, false);

        assertThat(result.includeExternal()).isFalse();
        assertThat(result.entries()).hasSize(1);
        verify(orderHistoryRepository).findByCustomerIdAndExternalFlagOrderByOrderDateDesc(1L, false);
    }

    @Test
    void getOrderHistoryById_existingEntry_shouldReturnDTO() {
        OrderHistory entity = new OrderHistory();
        entity.setOrderHistoryId(1L);
        entity.setCustomerId(1L);
        entity.setOrderType(OrderType.NEW_ORDER);

        OrderHistoryDTO expectedDTO = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, LocalDateTime.now(),
            "John Smith", OrderType.NEW_ORDER, "Test note for order",
            "ORD001", false, LocalDateTime.now(), null);

        when(orderHistoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(orderHistoryMapper.toDTO(entity)).thenReturn(expectedDTO);

        OrderHistoryDTO result = orderHistoryService.getOrderHistoryById(1L);

        assertThat(result.orderHistoryId()).isEqualTo(1L);
        verify(orderHistoryRepository).findById(1L);
    }

    @Test
    void getOrderHistoryById_nonExistingEntry_shouldThrowException() {
        when(orderHistoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderHistoryService.getOrderHistoryById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("OrderHistory");
    }

    @Test
    void updateOrderHistory_existingEntry_shouldUpdateAndReturn() {
        LocalDateTime orderDate = LocalDateTime.now().minusDays(1);
        UpdateOrderHistoryRequest request = new UpdateOrderHistoryRequest(
            orderDate, "Jane Doe", OrderType.ORDER_UPDATE,
            "Order status has been updated to processing state", "ORD20260723001");

        OrderHistory existingEntity = new OrderHistory();
        existingEntity.setOrderHistoryId(1L);
        existingEntity.setCustomerId(1L);
        existingEntity.setOrderEntryType(OrderEntryType.CUSTOMER);
        existingEntity.setOrderDate(LocalDateTime.now().minusDays(2));
        existingEntity.setRepresentative("John Smith");
        existingEntity.setOrderType(OrderType.NEW_ORDER);
        existingEntity.setNote("Original note for the order");
        existingEntity.setOrderNumber("ORD20260723001");

        OrderHistory updatedEntity = new OrderHistory();
        updatedEntity.setOrderHistoryId(1L);
        updatedEntity.setCustomerId(1L);
        updatedEntity.setOrderEntryType(OrderEntryType.CUSTOMER);
        updatedEntity.setOrderDate(orderDate);
        updatedEntity.setRepresentative("Jane Doe");
        updatedEntity.setOrderType(OrderType.ORDER_UPDATE);
        updatedEntity.setNote("Order status has been updated to processing state");
        updatedEntity.setOrderNumber("ORD20260723001");

        OrderHistoryDTO expectedDTO = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, orderDate, "Jane Doe",
            OrderType.ORDER_UPDATE, "Order status has been updated to processing state",
            "ORD20260723001", false, LocalDateTime.now(), LocalDateTime.now());

        when(orderHistoryRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(updatedEntity);
        when(orderHistoryMapper.toDTO(updatedEntity)).thenReturn(expectedDTO);

        OrderHistoryDTO result = orderHistoryService.updateOrderHistory(1L, request);

        assertThat(result.representative()).isEqualTo("Jane Doe");
        assertThat(result.orderType()).isEqualTo(OrderType.ORDER_UPDATE);
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(orderHistoryEventPublisher).publishOrderHistoryUpdated(any());
    }

    @Test
    void updateOrderHistory_nonExistingEntry_shouldThrowException() {
        UpdateOrderHistoryRequest request = new UpdateOrderHistoryRequest(
            LocalDateTime.now(), "Jane Doe", OrderType.ORDER_UPDATE,
            "Updated note for the order history entry", "ORD001");

        when(orderHistoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderHistoryService.updateOrderHistory(999L, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("OrderHistory");
    }
}