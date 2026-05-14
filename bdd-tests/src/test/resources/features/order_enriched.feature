Feature: Enriched order

  Scenario: Order returns with payment status from payment-service
    Given an order exists with sku "ENR1" and amount 10
    And payment for the last order is completed
    When I GET /api/orders/{id}/enriched for the last order
    Then response status is 200
    And response contains payment.status "AUTHORIZED"
    And response matches OpenAPI spec