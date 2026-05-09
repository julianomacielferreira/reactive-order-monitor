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
package mlocks.payment.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import mlocks.payment.model.OrderEvent;
import mlocks.payment.model.Payment;
import mlocks.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);
    private final KafkaReceiver<String, OrderEvent> receiver;
    private final PaymentRepository paymentRepository;
    private Disposable subscription;

    public PaymentProcessor(KafkaReceiver<String, OrderEvent> receiver, PaymentRepository paymentRepository) {
        this.receiver = receiver;
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    public void start() {

        // This integrates Kafka with Spring WebFlux for reactive messaging, using Reactor Kafka for non-blocking message handling.
        subscription = receiver.receive()
                .publishOn(Schedulers.boundedElastic()) // do not block Kafka poll thread
                .flatMap(this::processRecord, 10) // concurrency 10, controls backpressure
                .doOnError(e -> log.error("Stream failed", e))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5))) // restart on broker outage
                .subscribe();
    }

    private Mono<Void> processRecord(ReceiverRecord<String, OrderEvent> record) {

        OrderEvent event = record.value();

        return paymentRepository.findByOrderId(event.orderId())
                .switchIfEmpty(
                        paymentRepository.save(new Payment(event.orderId(), "AUTHORIZED"))
                                .doOnSuccess(p -> log.info("Payment created for order {}", event.orderId()))
                )
                .then(Mono.fromRunnable(() -> record.receiverOffset().acknowledge())) // commit only after DB success
                .onErrorResume(e -> {
                    log.error("Failed processing order {}, sending to DLT", event.orderId(), e);
                    // in real project, send to orders.DLT topic here
                    record.receiverOffset().acknowledge(); // avoid poison pill loop
                    return Mono.empty();
                }).then();
    }

    @PreDestroy
    public void stop() {

        if (subscription != null) subscription.dispose();
    }
}
