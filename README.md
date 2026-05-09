# Reactive Order Monitor

Real-Time Order Monitor with two reactive microservices, Kafka in the middle, and an Angular dashboard streaming live updates


## Project Structure

```
.
├── docker-compose.yml
├── .gitignore
├── .idea
├── LICENSE
├── .mvn
│   └── wrapper
│       └── maven-wrapper.properties
├── mvnw
├── order-service
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── mlocks
│           │       └── orders
│           │           ├── config
│           │           │   ├── JacksonConfig.java
│           │           │   └── KafkaConfig.java
│           │           ├── controller
│           │           │   └── OrderController.java
│           │           ├── dto
│           │           │   ├── EnrichedOrder.java
│           │           │   ├── OrderRequest.java
│           │           │   └── PaymentStatus.java
│           │           ├── model
│           │           │   ├── Audit.java
│           │           │   ├── OrderEvent.java
│           │           │   └── Order.java
│           │           ├── OrderServiceApplication.java
│           │           └── repository
│           │               ├── AuditRepository.java
│           │               └── OrderRepository.java
│           └── resources
│               ├── application.yml
│               └── schema.sql
├── payment-service
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── mlocks
│           │       └── payment
│           │           ├── config
│           │           │   └── KafkaConfig.java
│           │           ├── controller
│           │           │   └── PaymentController.java
│           │           ├── model
│           │           │   ├── OrderEvent.java
│           │           │   └── Payment.java
│           │           ├── PaymentServiceApplication.java
│           │           ├── repository
│           │           │   └── PaymentRepository.java
│           │           └── service
│           │               └── PaymentProcessor.java
│           └── resources
│               ├── application.yml
│               └── schema.sql
└── README.md

27 directories, 31 files

```
