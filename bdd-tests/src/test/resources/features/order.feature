Feature: Create order

  Scenario: Valid order is persisted and event published
    Given the system is clean
    When I POST /api/orders with sku "ABC" and amount 100
    Then response status is 201
    And response matches OpenAPI spec
    And order is stored in database
    And event "CREATED" is published to Kafka