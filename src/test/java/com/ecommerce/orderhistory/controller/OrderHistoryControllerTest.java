package com.ecommerce.orderhistory.controller;

import com.ecommerce.orderhistory.dto.*;
import com.ecommerce.orderhistory.enums.OrderEntryType;
import com.ecommerce.orderhistory.enums.OrderType;
import com.ecommerce.orderhistory.service.OrderHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderHistoryController.class)
class OrderHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderHistoryService orderHistoryService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser
    void createOrderHistory_shouldReturnCreated() throws Exception {
        LocalDateTime orderDate = LocalDateTime.now().minusDays(1);
        CreateOrderHistoryRequest request = new CreateOrderHistoryRequest(
            1L, OrderEntryType.CUSTOMER, orderDate, "John Smith",
            OrderType.NEW_ORDER, "Customer placed a new order for widgets and gadgets",
            "ORD20260723001", false);

        OrderHistoryDTO responseDTO = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, orderDate, "John Smith",
            OrderType.NEW_ORDER, "Customer placed a new order for widgets and gadgets",
            "ORD20260723001", false, LocalDateTime.now(), null);

        when(orderHistoryService.createOrderHistory(any(CreateOrderHistoryRequest.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/order-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderHistoryId").value(1))
            .andExpect(jsonPath("$.customerId").value(1))
            .andExpect(jsonPath("$.orderType").value("NEW_ORDER"))
            .andExpect(jsonPath("$.representative").value("John Smith"));
    }

    @Test
    @WithMockUser
    void createOrderHistory_invalidRequest_shouldReturn400() throws Exception {
        CreateOrderHistoryRequest request = new CreateOrderHistoryRequest(
            null, null, null, null, null, "short", null, false);

        mockMvc.perform(post("/api/v1/order-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getCustomerOrderHistory_shouldReturnEntries() throws Exception {
        OrderHistoryDTO entryDTO = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, LocalDateTime.now().minusDays(1),
            "John Smith", OrderType.NEW_ORDER, "Customer placed a new order",
            "ORD20260723001", false, LocalDateTime.now(), null);

        ViewOrderHistoryDTO viewDTO = new ViewOrderHistoryDTO(1L, false, List.of(entryDTO));

        when(orderHistoryService.getCustomerOrderHistory(1L, false)).thenReturn(viewDTO);

        mockMvc.perform(get("/api/v1/order-history/customer/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value(1))
            .andExpect(jsonPath("$.includeExternal").value(false))
            .andExpect(jsonPath("$.entries").isArray())
            .andExpect(jsonPath("$.entries[0].orderHistoryId").value(1));
    }

    @Test
    @WithMockUser
    void getCustomerOrderHistory_withExternalFlag_shouldReturnEntries() throws Exception {
        ViewOrderHistoryDTO viewDTO = new ViewOrderHistoryDTO(1L, true, List.of());

        when(orderHistoryService.getCustomerOrderHistory(1L, true)).thenReturn(viewDTO);

        mockMvc.perform(get("/api/v1/order-history/customer/1").param("includeExternal", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.includeExternal").value(true));
    }

    @Test
    @WithMockUser
    void getOrderHistoryById_shouldReturnEntry() throws Exception {
        OrderHistoryDTO dto = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, LocalDateTime.now().minusDays(1),
            "John Smith", OrderType.NEW_ORDER, "Customer placed a new order",
            "ORD20260723001", false, LocalDateTime.now(), null);

        when(orderHistoryService.getOrderHistoryById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/order-history/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderHistoryId").value(1))
            .andExpect(jsonPath("$.orderNumber").value("ORD20260723001"));
    }

    @Test
    @WithMockUser
    void updateOrderHistory_shouldReturnUpdatedEntry() throws Exception {
        LocalDateTime orderDate = LocalDateTime.now().minusDays(1);
        UpdateOrderHistoryRequest request = new UpdateOrderHistoryRequest(
            orderDate, "Jane Doe", OrderType.ORDER_UPDATE,
            "Order status has been updated to processing state", "ORD20260723001");

        OrderHistoryDTO responseDTO = new OrderHistoryDTO(
            1L, 1L, OrderEntryType.CUSTOMER, orderDate, "Jane Doe",
            OrderType.ORDER_UPDATE, "Order status has been updated to processing state",
            "ORD20260723001", false, LocalDateTime.now(), LocalDateTime.now());

        when(orderHistoryService.updateOrderHistory(eq(1L), any(UpdateOrderHistoryRequest.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/order-history/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderHistoryId").value(1))
            .andExpect(jsonPath("$.representative").value("Jane Doe"))
            .andExpect(jsonPath("$.orderType").value("ORDER_UPDATE"));
    }
}