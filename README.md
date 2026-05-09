# Reactive Order Monitor

Real-Time Order Monitor with two reactive microservices, Kafka in the middle, and an Angular dashboard streaming live updates


## Project Structure

```
.
├── pom.xml
└── src
    └── main
        ├── java
        │   └── mlocks
        │       └── orders
        │           ├── config
        │           │   ├── JacksonConfig.java
        │           │   └── KafkaConfig.java
        │           ├── controller
        │           │   └── OrderController.java
        │           ├── dto
        │           │   ├── EnrichedOrder.java
        │           │   ├── OrderRequest.java
        │           │   └── PaymentStatus.java
        │           ├── model
        │           │   ├── Audit.java
        │           │   ├── OrderEvent.java
        │           │   └── Order.java
        │           ├── OrderServiceApplication.java
        │           └── repository
        │               ├── AuditRepository.java
        │               └── OrderRepository.java
        └── resources
            ├── application.yml
            └── schema.sql

11 directories, 15 files
```
