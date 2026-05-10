# Reactive Order Monitor

Real-Time Order Monitor with two reactive microservices, Kafka in the middle, and an Angular dashboard streaming live updates

![Reactive Order Monitor](realtime-reactive-order-monitor.png)

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
│           │           │   ├── KafkaProducer.java
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
├── README.md
└── ui
    ├── angular.json
    ├── package.json
    ├── proxy.conf.json
    ├── src
    │   ├── app
    │   │   ├── app.component.ts
    │   │   ├── components
    │   │   │   ├── order-create
    │   │   │   │   └── order-create.component.ts
    │   │   │   └── order-list
    │   │   │       ├── order-list.component.html
    │   │   │       └── order-list.component.ts
    │   │   ├── models
    │   │   │   └── order.model.ts
    │   │   └── services
    │   │       └── order.service.ts
    │   ├── favicon.ico
    │   ├── index.html
    │   ├── main.ts
    │   ├── polyfills.ngtypecheck.ts
    │   ├── polyfills.ts
    │   └── styles.css
    ├── tsconfig.app.json
    └── tsconfig.json

35 directories, 49 files

```

## Running the application

Prerequisites
- Java Development Kit (JDK) 17 or above installed on your machine

Make ``maven`` script executable in the project directory root:

```bash
$ chmod +x mvnw
```

Start the containers using Docker Compose with the following command:

```bash
$ docker-compose up
```

To stop the containers, run:

```bash
$ docker-compose down
```

Type the following three commands in directory ``order-service`` to install dependencies:

```bash
$ cd order-service/
$ ../mvnw install
$ ../mvnw spring-boot:run
```

Do the same in the directory ``payment-service``:

```bash
$ cd payment-service/
$ ../mvnw install
$ ../mvnw spring-boot:run
```

## References

- [**Spring Boot**](https://spring.io/projects/spring-boot)
- [**MDN Web Docs**](https://developer.mozilla.org/)
- [**IntelliJ IDEA Community Edition**](https://www.jetbrains.com/idea/download/?section=linux)
- [**Docker**](https://www.docker.com/)
- [**Angular**](https://angular.io/)
- [**Apache Kafka**](https://kafka.apache.org/)

## License

Please see the [license agreement](https://github.com/julianomacielferreira/reactive-order-monitor/blob/main/LICENSE).