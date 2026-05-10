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

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaProducer {
    private final KafkaSender<String, String> sender;
    private final Tracer tracer;
    private final Propagator propagator;

    public KafkaProducer(KafkaSender<String, String> sender, Tracer tracer, Propagator propagator) {
        this.sender = sender;
        this.tracer = tracer;
        this.propagator = propagator;
    }

    public Mono<Void> send(String topic, String key, String value) {
        return Mono.defer(() -> {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            // get current span from the WebFlux request
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                // inject B3 headers into Kafka
                propagator.inject(
                        currentSpan.context(),
                        record.headers(),
                        (Headers headers, String k, String v) ->
                                headers.add(k, v.getBytes(StandardCharsets.UTF_8))
                );
            }

            return sender.send(Mono.just(SenderRecord.create(record, null))).then();
        });
    }
}