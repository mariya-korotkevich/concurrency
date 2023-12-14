package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private AtomicLong nextId = new AtomicLong(0L);

    private long nextId() {
        return nextId.getAndIncrement();
    }

    public long createOrder(List<Item> items) {
        long id = nextId();
        currentOrders.put(id, new Order(id, items));
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        Order updatedOrder = currentOrders.compute(orderId, (id, order) -> order.withPaymentInfo(paymentInfo));
        if (updatedOrder.checkStatus()) {
            deliver(updatedOrder);
        }
    }

    public void setPacked(long orderId) {
        Order updatedOrder = currentOrders.compute(orderId, (id, order) -> order.withIsPacked(true));
        if (updatedOrder.checkStatus()) {
            deliver(updatedOrder);
        }
    }

    private void deliver(Order order) {
        currentOrders.compute(order.getId(), (id, currentOrder) -> currentOrder.withStatus(Order.Status.DELIVERED));
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(Order.Status.DELIVERED);
    }
}
