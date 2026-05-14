# Reactive Order Monitor

Real-Time Order Monitor with two reactive microservices, Kafka in the middle, and an Angular dashboard streaming live updates

![Reactive Order Monitor](realtime-reactive-order-monitor.png)

## Project Structure

```
.
├── bdd-tests
│   ├── pom.xml
│   └── src
│       └── test
│           ├── java
│           │   └── mlocks
│           │       └── bdd
│           │           ├── RunCucumberTest.java
│           │           └── steps
│           │               └── OrderSteps.java
│           └── resources
│               ├── features
│               │   └── order.feature
│               ├── junit-platform.properties
│               └── openapi
│                   └── order-api.yaml
├── docker-compose.yml
├── ER_db_model.png
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
├── ui
│   ├── angular.json
│   ├── package.json
│   ├── proxy.conf.json
│   ├── src
│   │   ├── app
│   │   │   ├── app.component.ts
│   │   │   ├── components
│   │   │   │   ├── order-create
│   │   │   │   │   └── order-create.component.ts
│   │   │   │   └── order-list
│   │   │   │       ├── order-list.component.html
│   │   │   │       └── order-list.component.ts
│   │   │   ├── models
│   │   │   │   └── order.model.ts
│   │   │   └── services
│   │   │       └── order.service.ts
│   │   ├── favicon.ico
│   │   ├── index.html
│   │   ├── main.ts
│   │   ├── polyfills.ngtypecheck.ts
│   │   ├── polyfills.ts
│   │   └── styles.css
│   ├── tsconfig.app.json
│   └── tsconfig.json
└── ui-screenshot.png

45 directories, 59 files
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

The service will run on ``http://localhost:8081/api/``

Do the same in the directory ``payment-service`` (install dependencies and start the service):

```bash
$ cd payment-service/
$ ../mvnw install
$ ../mvnw spring-boot:run
```

The service will run on ``http://localhost:8082/api/``


## Database Model

The database is very simple, only containing three tables.

![Database](ER_db_model.png)

## Frontend

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

![Reactive Order Monitor](ui-screenshot.png)

## Cucumber Tests

Type the following three commands in directory ``bdd-tests`` to install dependencies and run the tests:


```bash
$ cd bdd-tests/
$ ../mvnw install
$ ../mvnw test
```

The output must be like this:

```bash
$ ../mvnw test
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------------< mlocks:bdd-tests >--------------------------
[INFO] Building bdd-tests 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ bdd-tests ---
[INFO] skip non existing resourceDirectory /home/juliano/Public/reactive-order-monitor/bdd-tests/src/main/resources
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ bdd-tests ---
[INFO] No sources to compile
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ bdd-tests ---
[INFO] Copying 3 resources from src/test/resources to target/test-classes
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ bdd-tests ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 2 source files with javac [debug target 17] to target/test-classes
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ bdd-tests ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running mlocks.bdd.RunCucumberTest

Scenario: Valid order is persisted and event published  # features/order.feature:3
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
  Given the system is clean                             # mlocks.bdd.steps.OrderSteps.clean()
  When I POST /api/orders with sku "ABC" and amount 100 # mlocks.bdd.steps.OrderSteps.post(java.lang.String,int)
  Then response status is 201                           # mlocks.bdd.steps.OrderSteps.status(int)
  And response matches OpenAPI spec                     # mlocks.bdd.steps.OrderSteps.validateContract()
  And order is stored in database                       # mlocks.bdd.steps.OrderSteps.dbCheck()
Kafka event received: {"orderId":1,"type":"CREATED","ts":"2026-05-14T14:17:34.974324117Z"}
  And event "CREATED" is published to Kafka             # mlocks.bdd.steps.OrderSteps.kafkaCheck(java.lang.String)
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.625 s -- in mlocks.bdd.RunCucumberTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  9.233 s
[INFO] Finished at: 2026-05-14T12:16:58-03:00
[INFO] ------------------------------------------------------------------------

```


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

- **`GET` /api/orders/:orderId/enriched** (Retrieve order by primary key)

```bash
$ curl --location 'http://localhost:8081/api/orders/1/enriched'
```

<details>
<summary><b>Response</b></summary>

```json
{
  "order": {
    "id": 3,
    "sku": "ABCD",
    "amount": 100,
    "status": "CREATED",
    "createdAt": "2026-05-12T19:28:49.163356Z"
  },
  "payment": {
    "status": "UNKNOWN"
  }
}
```
</details>

---

- **`GET` /api/payments/:orderId** (Retrieve order payment by primary key)

```bash
$ curl --location 'http://localhost:8082/api/payments/1'
```

<details>
<summary><b>Response</b></summary>

```json
{
  "id": 1,
  "orderId": 1,
  "status": "AUTHORIZED",
  "createdAt": "2026-05-11T13:40:35.722187Z"
}
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