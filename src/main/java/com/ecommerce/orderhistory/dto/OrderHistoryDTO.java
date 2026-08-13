package com.ecommerce.orderhistory.dto;

import com.ecommerce.orderhistory.enums.OrderEntryType;
import com.ecommerce.orderhistory.enums.OrderType;
import java.time.LocalDateTime;

/**
 * Maps to legacy OrderHistoryVO.
 * Legacy: com.ecommerce.view.customer.OrderHistoryVO
 */
public record OrderHistoryDTO(
    Long orderHistoryId,
    Long customerId,
    OrderEntryType orderEntryType,
    LocalDateTime orderDate,
    String representative,
    OrderType orderType,
    String note,
    String orderNumber,
    boolean externalFlag,
    LocalDateTime createdDate,
    LocalDateTime updatedDate
) {}