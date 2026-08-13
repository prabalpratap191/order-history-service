package com.ecommerce.orderhistory.dto;

import java.util.List;

/**
 * Maps to legacy ViewOrderHistoryVO.
 * Legacy: com.ecommerce.view.customer.ViewOrderHistoryVO
 * Contains the customer ID and a list of order history entries.
 */
public record ViewOrderHistoryDTO(
    Long customerId,
    boolean includeExternal,
    List<OrderHistoryDTO> entries
) {}