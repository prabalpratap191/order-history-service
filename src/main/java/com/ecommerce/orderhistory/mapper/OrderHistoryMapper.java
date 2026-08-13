package com.ecommerce.orderhistory.mapper;

import com.ecommerce.orderhistory.dto.CreateOrderHistoryRequest;
import com.ecommerce.orderhistory.dto.OrderHistoryDTO;
import com.ecommerce.orderhistory.entity.OrderHistory;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * Maps between OrderHistory entity and DTOs.
 * Legacy: Transformation logic from OrderHistoryDelegate and Commands.
 */
@Component
public class OrderHistoryMapper {

    public OrderHistoryDTO toDTO(OrderHistory entity) {
        Objects.requireNonNull(entity, "OrderHistory entity must not be null");
        return new OrderHistoryDTO(
            entity.getOrderHistoryId(),
            entity.getCustomerId(),
            entity.getOrderEntryType(),
            entity.getOrderDate(),
            entity.getRepresentative(),
            entity.getOrderType(),
            entity.getNote(),
            entity.getOrderNumber(),
            entity.isExternalFlag(),
            entity.getCreatedDate(),
            entity.getUpdatedDate()
        );
    }

    public OrderHistory toEntity(CreateOrderHistoryRequest request) {
        Objects.requireNonNull(request, "CreateOrderHistoryRequest must not be null");
        OrderHistory entity = new OrderHistory();
        entity.setCustomerId(request.customerId());
        entity.setOrderEntryType(request.orderEntryType());
        entity.setOrderDate(request.orderDate());
        entity.setRepresentative(request.representative());
        entity.setOrderType(request.orderType());
        entity.setNote(request.note());
        entity.setOrderNumber(request.orderNumber());
        entity.setExternalFlag(request.externalFlag());
        return entity;
    }
}