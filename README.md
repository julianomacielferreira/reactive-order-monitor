# Reactive Order Monitor $${\color{red}[in \space progress]}$$

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
├── Reactive Order Monitor.postman_collection.json
├── README.md
├── realtime-reactive-order-monitor.png
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

35 directories, 51 files

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

Type the following three commands in directory ``order-service`` to install dependencies and start the service:

```bash
$ cd order-service/
$ ../mvnw install
$ ../mvnw spring-boot:run
```

Do the same in the directory ``payment-service`` (install dependencies and start the service):

```bash
$ cd payment-service/
$ ../mvnw install
$ ../mvnw spring-boot:run
```

Check if you have a recent version of [Node.js](https://nodejs.org/) (which comes bundled with [npm](https://www.npmjs.com/), a JavaScript package manager):

```bash
$ node -v
```

```bash
$ npm -v
```

In the ``ui`` directory install all the dependencies and libs:

```bash
$ cd ui/
$ npm install
```

Run the following command:

```bash
$ npm run start
```

And then access [http://localhost:4200/](http://localhost:4200/) on your browser.

## Endpoints

A Postman collection of endpoints is located in the file [Reactive Order Monitor.postman_collection.json](https://github.com/julianomacielferreira/reactive-order-monitor/blob/main/Reactive%20Order%20Monitor.postman_collection.json) and
below are example cURL calls to the endpoints.

- **`POST` /api/orders** (Create new order)

```bash
$ curl --location 'http://localhost:8081/api/orders' \
--header 'Content-Type: application/json' \
--data '{
    "sku": "ABCD",
    "amount": 100
}'
```

<details>
<summary><b>Response</b></summary>

```json
{
    "id": 5,
    "sku": "ABCD",
    "amount": 100,
    "status": "CREATED",
    "createdAt": "2026-05-12T19:54:16.406162538Z"
}
```
</details>

---

- **`GET` /api/orders/stream** (Retrieve all orders)

```bash
$ curl --location 'http://localhost:8081/api/orders/stream'
```

<details>
<summary><b>Response</b></summary>

```json
data:{"id":5,"sku":"ABCD","amount":100,"status":"CREATED","createdAt":"2026-05-12T19:54:16.406163Z"}

data:{"id":4,"sku":"ABCD","amount":100,"status":"CREATED","createdAt":"2026-05-12T19:29:21.344336Z"}

data:{"id":3,"sku":"ABCD","amount":100,"status":"CREATED","createdAt":"2026-05-12T19:28:49.163356Z"}

data:{"id":2,"sku":"ABCD","amount":100,"status":"CREATED","createdAt":"2026-05-11T13:40:42.397927Z"}
```
</details>

---

## References

- [**Spring Boot**](https://spring.io/projects/spring-boot)
- [**MDN Web Docs**](https://developer.mozilla.org/)
- [**IntelliJ IDEA Community Edition**](https://www.jetbrains.com/idea/download/?section=linux)
- [**Docker**](https://www.docker.com/)
- [**Angular**](https://angular.io/)
- [**Apache Kafka**](https://kafka.apache.org/)

## License

Please see the [license agreement](https://github.com/julianomacielferreira/reactive-order-monitor/blob/main/LICENSE).