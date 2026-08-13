package com.ecommerce.orderhistory.repository;

import com.ecommerce.orderhistory.entity.OrderHistory;
import com.ecommerce.orderhistory.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Legacy: OrderHistoryDAO (not implemented in legacy, now fully implemented).
 * Provides data access for ORDER_HISTORY table.
 */
@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    List<OrderHistory> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    List<OrderHistory> findByCustomerIdAndExternalFlagOrderByOrderDateDesc(Long customerId, boolean externalFlag);

    List<OrderHistory> findByCustomerIdAndOrderTypeOrderByOrderDateDesc(Long customerId, OrderType orderType);

    List<OrderHistory> findByOrderNumber(String orderNumber);
}