package com.ecommerce.orderhistory.dto;

import com.ecommerce.orderhistory.enums.OrderType;
import com.ecommerce.orderhistory.validation.ValidOrderDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for updating order history entries.
 * Legacy: Form data from editOrderHistory.jsp -> OrderHistoryEditSaveCommand
 */
public record UpdateOrderHistoryRequest(
    @NotNull(message = "Order date is required")
    @ValidOrderDate
    LocalDateTime orderDate,

    @Size(min = 1, max = 100, message = "Representative must be between 1 and 100 characters")
    String representative,

    @NotNull(message = "Order type is required")
    OrderType orderType,

    @NotNull(message = "Note is required")
    @Size(min = 10, max = 4000, message = "Note must be between 10 and 4000 characters")
    String note,

    @Size(max = 50, message = "Order number must not exceed 50 characters")
    String orderNumber
) {}