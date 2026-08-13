package com.ecommerce.orderhistory.service.impl;

import com.ecommerce.orderhistory.dto.*;
import com.ecommerce.orderhistory.entity.OrderHistory;
import com.ecommerce.orderhistory.event.OrderHistoryCreatedEvent;
import com.ecommerce.orderhistory.event.OrderHistoryEventPublisher;
import com.ecommerce.orderhistory.event.OrderHistoryUpdatedEvent;
import com.ecommerce.orderhistory.exception.ResourceNotFoundException;
import com.ecommerce.orderhistory.mapper.OrderHistoryMapper;
import com.ecommerce.orderhistory.repository.OrderHistoryRepository;
import com.ecommerce.orderhistory.service.OrderHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of OrderHistoryService.
 * Legacy: OrderHistoryDelegate business logic.
 * Business Rules:
 * - Order date cannot be in the future (validated via @ValidOrderDate)
 * - Note must be between 10 and 4000 characters
 * - Representative is required
 * - External flag filters entries from external systems
 * - Entries are returned in reverse chronological order
 */
@Service
@Transactional(readOnly = true)
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryServiceImpl.class);

    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderHistoryMapper orderHistoryMapper;
    private final OrderHistoryEventPublisher orderHistoryEventPublisher;

    public OrderHistoryServiceImpl(OrderHistoryRepository orderHistoryRepository,
                                    OrderHistoryMapper orderHistoryMapper,
                                    OrderHistoryEventPublisher orderHistoryEventPublisher) {
        this.orderHistoryRepository = Objects.requireNonNull(orderHistoryRepository);
        this.orderHistoryMapper = Objects.requireNonNull(orderHistoryMapper);
        this.orderHistoryEventPublisher = Objects.requireNonNull(orderHistoryEventPublisher);
    }

    @Override
    @Transactional
    public OrderHistoryDTO createOrderHistory(CreateOrderHistoryRequest request) {
        log.info("Creating order history entry for customer ID: {}, order type: {}",
            request.customerId(), request.orderType());

        OrderHistory entity = orderHistoryMapper.toEntity(request);
        OrderHistory savedEntity = orderHistoryRepository.save(entity);

        log.info("Order history entry created with ID: {} for customer ID: {}",
            savedEntity.getOrderHistoryId(), savedEntity.getCustomerId());

        OrderHistoryCreatedEvent event = OrderHistoryCreatedEvent.of(
            savedEntity.getOrderHistoryId(),
            savedEntity.getCustomerId(),
            savedEntity.getOrderEntryType().name(),
            savedEntity.getOrderType().name(),
            savedEntity.getOrderNumber(),
            savedEntity.getOrderDate()
        );
        orderHistoryEventPublisher.publishOrderHistoryCreated(event);

        return orderHistoryMapper.toDTO(savedEntity);
    }

    @Override
    public ViewOrderHistoryDTO getCustomerOrderHistory(Long customerId, boolean includeExternal) {
        log.debug("Retrieving order history for customer ID: {}, includeExternal: {}",
            customerId, includeExternal);

        List<OrderHistory> entries;
        if (includeExternal) {
            entries = orderHistoryRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
        } else {
            entries = orderHistoryRepository.findByCustomerIdAndExternalFlagOrderByOrderDateDesc(
                customerId, false);
        }

        List<OrderHistoryDTO> entryDTOs = entries.stream()
            .map(orderHistoryMapper::toDTO)
            .toList();

        log.debug("Found {} order history entries for customer ID: {}", entryDTOs.size(), customerId);

        return new ViewOrderHistoryDTO(customerId, includeExternal, entryDTOs);
    }

    @Override
    public OrderHistoryDTO getOrderHistoryById(Long orderHistoryId) {
        log.debug("Retrieving order history entry with ID: {}", orderHistoryId);

        OrderHistory entity = orderHistoryRepository.findById(orderHistoryId)
            .orElseThrow(() -> new ResourceNotFoundException("OrderHistory", "orderHistoryId", orderHistoryId));

        return orderHistoryMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public OrderHistoryDTO updateOrderHistory(Long orderHistoryId, UpdateOrderHistoryRequest request) {
        log.info("Updating order history entry with ID: {}", orderHistoryId);

        OrderHistory entity = orderHistoryRepository.findById(orderHistoryId)
            .orElseThrow(() -> new ResourceNotFoundException("OrderHistory", "orderHistoryId", orderHistoryId));

        entity.setOrderDate(request.orderDate());
        if (request.representative() != null) {
            entity.setRepresentative(request.representative());
        }
        entity.setOrderType(request.orderType());
        entity.setNote(request.note());
        if (request.orderNumber() != null) {
            entity.setOrderNumber(request.orderNumber());
        }

        OrderHistory updatedEntity = orderHistoryRepository.save(entity);

        log.info("Order history entry updated with ID: {}", updatedEntity.getOrderHistoryId());

        OrderHistoryUpdatedEvent event = OrderHistoryUpdatedEvent.of(
            updatedEntity.getOrderHistoryId(),
            updatedEntity.getCustomerId(),
            updatedEntity.getOrderType().name(),
            updatedEntity.getOrderNumber()
        );
        orderHistoryEventPublisher.publishOrderHistoryUpdated(event);

        return orderHistoryMapper.toDTO(updatedEntity);
    }
}