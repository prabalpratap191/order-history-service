package com.ecommerce.orderhistory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Order History Service API")
                .version("1.0.0")
                .description("RESTful APIs for Order History Management - Migrated from Legacy Jakarta EE Order History Module. "
                    + "Legacy: OrderHistoryAddCommand, OrderHistorySaveAddCommand, OrderHistoryMaintainCommand, "
                    + "OrderHistoryEditCommand, OrderHistoryEditSaveCommand, OrderHistoryDelegate, "
                    + "OrderHistoryVO, ViewOrderHistoryVO")
                .contact(new Contact().name("E-commerce Team").email("ecommerce@company.com"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}