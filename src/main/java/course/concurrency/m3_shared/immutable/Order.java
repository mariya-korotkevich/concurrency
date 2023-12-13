package course.concurrency.m3_shared.immutable;

import java.util.Collections;
import java.util.List;

import static course.concurrency.m3_shared.immutable.Order.Status.IN_PROGRESS;
import static course.concurrency.m3_shared.immutable.Order.Status.NEW;

public final class Order {

    public enum Status { NEW, IN_PROGRESS, DELIVERED }

    private final Long id;
    private final List<Item> items;
    private final PaymentInfo paymentInfo;
    private final boolean isPacked;
    private final Status status;

    public static Order newOrder(long id, List<Item> items) {
        return new Order(id, items, null, false, NEW);
    }

    public static Order updatedOrder(Order order, PaymentInfo paymentInfo) {
        return new Order(order.id, order.items, paymentInfo, order.isPacked, IN_PROGRESS);
    }

    public static Order updatedOrder(Order order, boolean isPacked) {
        return new Order(order.id, order.items, order.paymentInfo, isPacked, IN_PROGRESS);
    }

    public static Order updatedOrder(Order order, Status status) {
        return new Order(order.id, order.items, order.paymentInfo, order.isPacked, status);
    }

    private Order(Long id, List<Item> items, PaymentInfo paymentInfo, boolean isPacked, Status status) {
        this.id = id;
        this.items = items;
        this.paymentInfo = paymentInfo;
        this.isPacked = isPacked;
        this.status = status;
    }

    public boolean checkStatus() {
        return items != null && !items.isEmpty() && paymentInfo != null && isPacked;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public boolean isPacked() {
        return isPacked;
    }

    public Status getStatus() {
        return status;
    }
}
