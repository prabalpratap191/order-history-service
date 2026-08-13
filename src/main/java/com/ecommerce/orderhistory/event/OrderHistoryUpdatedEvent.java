package com.ecommerce.orderhistory.event;

import java.time.LocalDateTime;

public record OrderHistoryUpdatedEvent(
    Long orderHistoryId,
    Long customerId,
    String orderType,
    String orderNumber,
    LocalDateTime eventTimestamp
) {
    public static OrderHistoryUpdatedEvent of(Long orderHistoryId, Long customerId,
                                               String orderType, String orderNumber) {
        return new OrderHistoryUpdatedEvent(orderHistoryId, customerId, orderType,
            orderNumber, LocalDateTime.now());
    }
}