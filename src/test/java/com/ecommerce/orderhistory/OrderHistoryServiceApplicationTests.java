package com.ecommerce.orderhistory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"order-history.created", "order-history.updated", "order.created", "order.updated"})
class OrderHistoryServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}