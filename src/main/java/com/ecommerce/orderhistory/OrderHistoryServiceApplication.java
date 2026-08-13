package com.ecommerce.orderhistory;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderHistoryServiceApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // Verify timezone is set correctly
        System.out.println("Application Timezone: " + TimeZone.getDefault().getID());
        SpringApplication.run(OrderHistoryServiceApplication.class, args);
    }
}