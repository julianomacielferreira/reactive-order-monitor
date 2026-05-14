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

import io.cucumber.java.en.*;
import okhttp3.*;
import okhttp3.sse.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamSteps {
    
    private final String baseUrl = "http://localhost:8081";
    private final List<String> events = new CopyOnWriteArrayList<>();
    private EventSource source;

    @Given("I am subscribed to order stream")
    public void subscribe() {
        events.clear();
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(Duration.ofSeconds(30))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/api/orders/stream")
                .header("Accept", "text/event-stream")
                .build();

        source = EventSources.createFactory(client).newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource es, String id, String type, String data) {
                events.add(data);
            }
        });
    }

    @Then("stream receives an order with sku {string} within {int} seconds")
    public void verify(String sku, int seconds) {
        await().atMost(Duration.ofSeconds(seconds)).untilAsserted(() -> {
            boolean found = events.stream().anyMatch(e -> e.contains("\"sku\":\"" + sku + "\""));
            assertTrue(found, "SSE did not receive sku " + sku + ". Events: " + events);
        });
        source.cancel();
    }
}
