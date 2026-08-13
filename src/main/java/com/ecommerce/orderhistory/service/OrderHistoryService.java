package com.ecommerce.orderhistory.service;

import com.ecommerce.orderhistory.dto.*;

/**
 * Service interface for Order History operations.
 * Legacy: OrderHistoryDelegate
 */
public interface OrderHistoryService {

    /**
     * Creates a new order history entry.
     * Legacy: OrderHistoryDelegate.addOrderHistoryEntry(OrderHistoryVO)
     */
    OrderHistoryDTO createOrderHistory(CreateOrderHistoryRequest request);

    /**
     * Retrieves all order history entries for a customer.
     * Legacy: OrderHistoryDelegate.getCustomerOrderHistory(long, boolean)
     */
    ViewOrderHistoryDTO getCustomerOrderHistory(Long customerId, boolean includeExternal);

    /**
     * Retrieves a single order history entry by ID.
     * Legacy: OrderHistoryDelegate.getOrderHistoryEntry(long)
     */
    OrderHistoryDTO getOrderHistoryById(Long orderHistoryId);

    /**
     * Updates an existing order history entry.
     * Legacy: OrderHistoryDelegate.updateOrderHistoryEntry(OrderHistoryVO)
     */
    OrderHistoryDTO updateOrderHistory(Long orderHistoryId, UpdateOrderHistoryRequest request);
}