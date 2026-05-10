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
import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { bufferTime, mergeAll, retry, share } from 'rxjs/operators';
import { Order, OrderRequest } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {

    constructor(private http: HttpClient, private zone: NgZone) {}

    streamOrders(): Observable<Order> {

        return new Observable<Order>(observer => {
            const es = new EventSource('/api/orders/stream');

            es.onmessage = e => {
                // force back into Angular zone so change detection fires
                this.zone.run(() => observer.next(JSON.parse(e.data)));
            };

            es.onerror = err => {
                this.zone.run(() => observer.error(err));
            };

            return () => es.close();
        }).pipe(
            bufferTime(500),  // backpressure – matches your Reactor limitRate
            mergeAll(),
            retry({ delay: 2000 }),
            share() // one SSE connection for all subscribers
        );
    }

    create(order: OrderRequest) {
        return this.http.post<Order>('/api/orders', order);
    }
}