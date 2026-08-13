package com.ecommerce.orderhistory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${order-history.kafka.topic.order-history-created}")
    private String orderHistoryCreatedTopic;

    @Value("${order-history.kafka.topic.order-history-updated}")
    private String orderHistoryUpdatedTopic;

    @Bean
    public NewTopic orderHistoryCreatedTopic() {
        return TopicBuilder.name(orderHistoryCreatedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderHistoryUpdatedTopic() {
        return TopicBuilder.name(orderHistoryUpdatedTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
}