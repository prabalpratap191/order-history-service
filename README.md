# Order History Service

Spring Boot 3.x microservice for Order History Management, migrated from legacy Jakarta EE Order History Management Module.

## Legacy Mapping
- Legacy Module: Order History Management (within Customer Management)
- Legacy Classes: OrderHistoryAddCommand, OrderHistorySaveAddCommand, OrderHistoryMaintainCommand, OrderHistoryEditCommand, OrderHistoryEditSaveCommand, OrderHistoryDelegate
- Legacy Tables: ORDER_HISTORY
- Legacy VOs: OrderHistoryVO, ViewOrderHistoryVO

## Tech Stack
- Java 17, Spring Boot 3.2.5, PostgreSQL, Apache Kafka, Docker

## Build & Run
mvn clean package
java -jar target/order-history-service-1.0.0.jar

## Docker
docker build -t order-history-service .
docker run -p 8084:8084 order-history-service

## API Docs
http://localhost:8084/swagger-ui.html

## Health Check
http://localhost:8084/actuator/health

## Endpoints
- POST /api/v1/order-history - Create a new order history entry
- GET /api/v1/order-history/customer/{customerId} - Get order history for a customer
- GET /api/v1/order-history/{orderHistoryId} - Get a specific order history entry
- PUT /api/v1/order-history/{orderHistoryId} - Update an order history entry

## Kafka Topics
- order-history.created - Published when a new order history entry is created
- order-history.updated - Published when an order history entry is updated
- order.created - Consumed from Order Service to auto-create order history entries
- order.updated - Consumed from Order Service to auto-create order history entries