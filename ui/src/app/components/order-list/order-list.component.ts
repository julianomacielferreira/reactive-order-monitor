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
import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OrderService} from '../../services/order.service';
import {Order} from '../../models/order.model';
import {Observable, scan} from 'rxjs';

@Component({
    selector: 'app-order-list',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './order-list.component.html'
})
export class OrderListComponent implements OnInit {

    list$!: Observable<Order[]>;
    live = false;

    constructor(private svc: OrderService) {
    }

    ngOnInit() {

        this.list$ = this.svc.streamOrders().pipe(
            scan((acc, cur) => {

                const existing =
                    acc.findIndex(o => o.id === cur.id);

                if (existing >= 0) {
                    return acc.map(o =>
                        o.id === cur.id ? cur : o
                    );
                }

                return [cur, ...acc].slice(0, 100);

            }, [] as Order[])
        );

        this.live = true;
    }
}