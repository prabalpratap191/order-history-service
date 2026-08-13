package com.ecommerce.orderhistory.dto;

import com.ecommerce.orderhistory.enums.OrderEntryType;
import com.ecommerce.orderhistory.enums.OrderType;
import com.ecommerce.orderhistory.validation.ValidOrderDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for creating order history entries.
 * Legacy: Form data from addOrderHistory.jsp -> OrderHistorySaveAddCommand
 * Business Rules:
 * - Order date cannot be in the future
 * - Note is required, min 10 characters, max 4000 characters
 * - Representative is required
 * - Order type is required
 */
public record CreateOrderHistoryRequest(
    @NotNull(message = "Customer ID is required")
    Long customerId,

    @NotNull(message = "Order entry type is required")
    OrderEntryType orderEntryType,

    @NotNull(message = "Order date is required")
    @ValidOrderDate
    LocalDateTime orderDate,

    @NotNull(message = "Representative is required")
    @Size(min = 1, max = 100, message = "Representative must be between 1 and 100 characters")
    String representative,

    @NotNull(message = "Order type is required")
    OrderType orderType,

    @NotNull(message = "Note is required")
    @Size(min = 10, max = 4000, message = "Note must be between 10 and 4000 characters")
    String note,

    @Size(max = 50, message = "Order number must not exceed 50 characters")
    String orderNumber,

    boolean externalFlag
) {}