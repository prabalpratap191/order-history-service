package com.ecommerce.orderhistory.event;

import java.time.LocalDateTime;

public record OrderHistoryCreatedEvent(
    Long orderHistoryId,
    Long customerId,
    String orderEntryType,
    String orderType,
    String orderNumber,
    LocalDateTime orderDate,
    LocalDateTime eventTimestamp
) {
    public static OrderHistoryCreatedEvent of(Long orderHistoryId, Long customerId,
                                               String orderEntryType, String orderType,
                                               String orderNumber, LocalDateTime orderDate) {
        return new OrderHistoryCreatedEvent(orderHistoryId, customerId, orderEntryType,
            orderType, orderNumber, orderDate, LocalDateTime.now());
    }
}