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

import brave.Span;
import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContextOrSamplingFlags;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import mlocks.payment.model.OrderEvent;
import mlocks.payment.model.Payment;
import mlocks.payment.repository.PaymentRepository;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    private final KafkaReceiver<String, OrderEvent> receiver;
    private final PaymentRepository paymentRepository;
    private final Tracing tracing;

    private Disposable subscription;

    // B3 propagation support
    private final Propagation<String> propagation = Propagation.B3_STRING;

    private final Propagation.Getter<Headers, String> getter =
            (headers, key) -> {
                var h = headers.lastHeader(key);
                return h != null
                        ? new String(h.value(), StandardCharsets.UTF_8)
                        : null;
            };

    public PaymentProcessor(
            KafkaReceiver<String, OrderEvent> receiver,
            PaymentRepository paymentRepository,
            Tracing tracing
    ) {
        this.receiver = receiver;
        this.paymentRepository = paymentRepository;
        this.tracing = tracing;
    }

    @PostConstruct
    public void start() {

        subscription = receiver.receive()
                .publishOn(Schedulers.boundedElastic())
                .flatMap(this::processRecord, 10)
                .doOnError(e -> log.error("Stream failed", e))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
                .subscribe();
    }

    private Mono<Void> processRecord(ReceiverRecord<String, OrderEvent> record) {

        // Extract trace context from Kafka headers
        TraceContextOrSamplingFlags extracted =
                propagation.extractor(getter)
                        .extract(record.headers());

        // Create child span
        Span span = tracing.tracer()
                .nextSpan(extracted)
                .name("kafka.receive")
                .start();

        try (var scope = tracing.tracer().withSpanInScope(span)) {

            OrderEvent event = record.value();

            span.tag("order.id", String.valueOf(event.orderId()));

            Payment authorized = new Payment(event.orderId(), "AUTHORIZED");

            return paymentRepository.findByOrderId(event.orderId())
                    .switchIfEmpty(
                            paymentRepository.save(authorized)
                                    .doOnSuccess(p -> log.info("Payment created for order {}", event.orderId()))
                    )
                    .then(
                            Mono.fromRunnable(() -> {
                                record.receiverOffset().acknowledge();
                                log.info("Offset acknowledged for order {}", event.orderId());
                            })
                    )
                    .doOnError(e -> {
                        span.error(e);

                        log.error("Failed processing order {}, sending to DLT", event.orderId(), e);

                        // avoid poison-pill loop
                        record.receiverOffset().acknowledge();
                    })
                    .doFinally(signalType -> span.finish())
                    .then();
        }
    }

    @PreDestroy
    public void stop() {

        if (subscription != null) {
            subscription.dispose();
        }
    }
}
