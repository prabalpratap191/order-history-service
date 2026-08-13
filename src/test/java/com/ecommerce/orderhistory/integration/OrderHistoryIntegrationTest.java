package com.ecommerce.orderhistory.integration;

import com.ecommerce.orderhistory.dto.CreateOrderHistoryRequest;
import com.ecommerce.orderhistory.dto.UpdateOrderHistoryRequest;
import com.ecommerce.orderhistory.entity.OrderHistory;
import com.ecommerce.orderhistory.enums.OrderEntryType;
import com.ecommerce.orderhistory.enums.OrderType;
import com.ecommerce.orderhistory.repository.OrderHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"order-history.created", "order-history.updated", "order.created", "order.updated"})
class OrderHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orderHistoryRepository.deleteAll();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser
    void createOrderHistory_shouldPersistAndReturn201() throws Exception {
        LocalDateTime orderDate = LocalDateTime.now().minusDays(1);
        CreateOrderHistoryRequest request = new CreateOrderHistoryRequest(
            1L, OrderEntryType.CUSTOMER, orderDate, "John Smith",
            OrderType.NEW_ORDER, "Customer placed a new order for widgets and gadgets",
            "ORD20260723001", false);

        mockMvc.perform(post("/api/v1/order-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderHistoryId").isNotEmpty())
            .andExpect(jsonPath("$.customerId").value(1))
            .andExpect(jsonPath("$.orderType").value("NEW_ORDER"))
            .andExpect(jsonPath("$.representative").value("John Smith"))
            .andExpect(jsonPath("$.orderNumber").value("ORD20260723001"));
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
    void getOrderHistoryById_nonExisting_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/order-history/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getCustomerOrderHistory_noEntries_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/order-history/customer/99999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value(99999))
            .andExpect(jsonPath("$.entries").isArray())
            .andExpect(jsonPath("$.entries.length()").value(0));
    }

    @Test
    @WithMockUser
    void createAndRetrieveOrderHistory_fullFlow() throws Exception {
        LocalDateTime orderDate = LocalDateTime.now().minusDays(1);
        CreateOrderHistoryRequest request = new CreateOrderHistoryRequest(
            42L, OrderEntryType.ORDER, orderDate, "Jane Doe",
            OrderType.INQUIRY, "Customer inquiry about order delivery status update",
            "ORD20260723042", false);

        String createResponse = mockMvc.perform(post("/api/v1/order-history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long orderHistoryId = objectMapper.readTree(createResponse).get("orderHistoryId").asLong();

        mockMvc.perform(get("/api/v1/order-history/" + orderHistoryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderHistoryId").value(orderHistoryId))
            .andExpect(jsonPath("$.customerId").value(42))
            .andExpect(jsonPath("$.orderType").value("INQUIRY"))
            .andExpect(jsonPath("$.representative").value("Jane Doe"));

        mockMvc.perform(get("/api/v1/order-history/customer/42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerId").value(42))
            .andExpect(jsonPath("$.entries.length()").value(1));

        UpdateOrderHistoryRequest updateRequest = new UpdateOrderHistoryRequest(
            orderDate, "Jane Doe", OrderType.ORDER_UPDATE,
            "Customer inquiry resolved and order status updated successfully",
            "ORD20260723042");

        mockMvc.perform(put("/api/v1/order-history/" + orderHistoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderType").value("ORDER_UPDATE"))
            .andExpect(jsonPath("$.note").value("Customer inquiry resolved and order status updated successfully"));
    }
}