Feature: Order SSE stream

  Scenario: New order appears on stream in real time
    Given I am subscribed to order stream
    When I POST /api/orders with sku "STREAM1" and amount 5
    Then stream receives an order with sku "STREAM1" within 5 seconds