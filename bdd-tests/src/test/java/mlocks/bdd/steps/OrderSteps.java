/*
 * The MIT License
 *
 * Copyright 2026 juliano.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mlocks.bdd.steps;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderSteps {

    private int lastOrderId;
    private static final String BASE_URL = "http://localhost:8081";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/reactive_order_monitor";
    private static final String DB_USER = "reactive";
    private static final String DB_PASSWORD = "qwerty";
    private static final String KAFKA_TOPIC = "orders";
    private final OpenApiValidationFilter apiFilter = new OpenApiValidationFilter("openapi/order-api.yaml");

    private Response response;

    @Given("the system is clean")
    public void clean() throws SQLException {

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            connection.createStatement().execute("TRUNCATE TABLE orders RESTART IDENTITY CASCADE");
        }
    }

    @When("I POST \\/api\\/orders with sku {string} and amount {int}")
    public void post(String sku, int amount) {

        response = given()
                .baseUri(BASE_URL)
                .filter(apiFilter)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "sku", sku,
                        "amount", amount
                ))
                .when()
                .post("/api/orders")
                .then()
                .extract()
                .response();
    }

    @Then("response status is {int}")
    public void status(int code) {
        assertEquals(code, response.statusCode());
    }

    @Then("response matches OpenAPI spec")
    public void validateContract() {
        assertTrue(
                response.contentType().contains("application/json"),
                "Response is not JSON"
        );
    }

    @Then("order is stored in database")
    public void dbCheck() {

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {

                    try (Connection connection =
                                 DriverManager.getConnection(
                                         DB_URL,
                                         DB_USER,
                                         DB_PASSWORD
                                 )) {

                        PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM orders WHERE sku = ?");
                        statement.setString(1, "ABC");

                        ResultSet resultSet = statement.executeQuery();

                        resultSet.next();

                        assertEquals(1, resultSet.getInt(1));
                    }
                });
    }

    @Then("event {string} is published to Kafka")
    public void kafkaCheck(String event) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "bdd-test-" + UUID.randomUUID());
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            consumer.subscribe(Collections.singletonList(KAFKA_TOPIC));

            await().atMost(Duration.ofSeconds(20))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(() -> {

                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                        boolean found = false;

                        for (ConsumerRecord<String, String> record : records) {

                            System.out.println("Kafka event received: " + record.value());

                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode node = mapper.readTree(record.value());

                            found = event.equals(node.get("type").asText());

                            if (found) break;
                        }

                        assertTrue(found, event + " event not found in Kafka topic");
                    });
        }
    }

    @Given("an order exists with sku {string} and amount {int}")
    public void createOrder(String sku, int amount) {

        response = given()
                .baseUri(BASE_URL)
                .filter(apiFilter)
                .contentType(ContentType.JSON)
                .body(Map.of("sku", sku, "amount", amount))
                .when().post("/api/orders")
                .then().extract().response();

        lastOrderId = response.jsonPath().getInt("id");

        assertEquals(201, response.statusCode());
    }

    @Given("payment for the last order is completed")
    public void paymentCompleted() {

        // payment-service listens to Kafka and updates status.
        // Wait until enriched endpoint returns AUTHORIZED
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {

            Response r = given().baseUri(BASE_URL).get("/api/orders/" + lastOrderId + "/enriched");

            assertEquals(200, r.statusCode());
            assertEquals("AUTHORIZED", r.jsonPath().getString("payment.status"));
        });
    }

    @When("I GET \\/api\\/orders\\/\\{id}\\/enriched for the last order")
    public void getEnriched() {
        
        response = given()
                .baseUri(BASE_URL)
                .filter(apiFilter)
                .when()
                .get("/api/orders/" + lastOrderId + "/enriched")
                .then()
                .extract().response();
    }

    @Then("response contains payment.status {string}")
    public void checkPayment(String status) {
        assertEquals(status, response.jsonPath().getString("payment.status"));
    }
}