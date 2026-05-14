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
import {Observable, map, scan, shareReplay} from 'rxjs';
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-order-list',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './order-list.component.html',
    styleUrls: ['./order-list.component.css']
})
export class OrderListComponent implements OnInit {

    list$!: Observable<Order[]>;

    total$!: Observable<number>;
    pending$!: Observable<number>;
    completed$!: Observable<number>;
    failed$!: Observable<number>;

    live = false;
    sku = '';
    amount = 1;

    constructor(private orderService: OrderService) {
    }

    ngOnInit() {

        this.list$ = this.orderService.streamOrders().pipe(
            scan((orders, currentOrder) => {

                const existing =
                    orders.findIndex(o => o.id === currentOrder.id);

                if (existing >= 0) {
                    return orders.map(o =>
                        o.id === currentOrder.id ? currentOrder : o
                    );
                }

                return [currentOrder, ...orders].slice(0, 100);

            }, [] as Order[]),

            shareReplay(1)
        );

        this.total$ = this.list$.pipe(
            map(list => list.length)
        );

        this.pending$ = this.list$.pipe(
            map(list =>
                list.filter(o => o.status === 'PENDING').length
            )
        );

        this.completed$ = this.list$.pipe(
            map(list =>
                list.filter(o => o.status === 'COMPLETED').length
            )
        );

        this.failed$ = this.list$.pipe(
            map(list =>
                list.filter(o => o.status === 'FAILED').length
            )
        );

        this.live = true;
    }

    send() {
        this.orderService.create({sku: this.sku, amount: this.amount}).subscribe(() => {
            this.sku = '';
            this.amount = 1;
        });
    }
}