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
package mlocks.orders.controller;

import mlocks.orders.dto.EnrichedOrder;
import mlocks.orders.dto.OrderRequest;
import mlocks.orders.dto.PaymentStatus;
import mlocks.orders.model.*;
import mlocks.orders.repository.AuditRepository;
import mlocks.orders.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orders;
    private final AuditRepository audits;
    private final ObjectMapper mapper;
    private final WebClient paymentClient = WebClient.create("http://localhost:8082/api");
    private final Sinks.Many<Order> orderSink = Sinks.many().replay().latest();

    @Autowired
    private KafkaProducer kafkaProducer;

    public OrderController(OrderRepository orders, AuditRepository audits, ObjectMapper mapper) {

        this.orders = orders;
        this.audits = audits;
        this.mapper = mapper;
    }

    // POST – returns Mono, non-blocking write + event publish
    @PostMapping
    public Mono<ResponseEntity<Order>> create(@RequestBody OrderRequest req) {

        Order toSave = new Order(req.sku(), req.amount(), "CREATED");

        return orders.save(toSave)
                .flatMap(saved -> {
                    // audit for regulated traceability
                    Mono<Void> audit = audits.save(new Audit(saved.id(), "CREATED")).then();

                    // publish event as JSON string
                    String json;
                    try {
                        json = mapper.writeValueAsString(new OrderEvent(saved.id(), "CREATED"));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }

                    Mono<Void> send = kafkaProducer.send("orders", saved.id().toString(), json);

                    return Mono.when(audit, send)
                            .then(Mono.fromRunnable(() ->
                                    orderSink.tryEmitNext(saved)))
                            .thenReturn(saved);
                })
                .map(o -> ResponseEntity.status(201).body(o))
                .timeout(Duration.ofSeconds(2)) // fail fast, do not block thread
                .onErrorResume(e -> Mono.just(ResponseEntity.status(503).build()));
    }

    // GET stream – Flux with backpressure
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Order> stream() {

        return orderSink.asFlux()
                .mergeWith(
                        orders.findAllByOrderByCreatedAtDesc()
                )
                .onBackpressureBuffer(50);
    }

    // GET enriched – Mono.zip combines two async calls
    @GetMapping("/{id}/enriched")
    public Mono<EnrichedOrder> enriched(@PathVariable Long id) {

        Mono<Order> orderMono = orders.findById(id);
        Mono<PaymentStatus> paymentMono = paymentClient.get()
                .uri("/payments/{id}", id)
                .retrieve()
                .bodyToMono(PaymentStatus.class)
                .onErrorReturn(new PaymentStatus("UNKNOWN"));

        return Mono.zip(orderMono, paymentMono)
                .map(tuple -> new EnrichedOrder(tuple.getT1(), tuple.getT2()));
    }
}