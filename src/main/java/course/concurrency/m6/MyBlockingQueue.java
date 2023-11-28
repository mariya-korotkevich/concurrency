package course.concurrency.m6;

public class MyBlockingQueue<T> {
    private final Object[] array;
    private final int capacity;
    private int size = 0;
    private int indexIn = -1;
    private int indexOut = -1;

    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
        array = new Object[capacity];
    }

    public synchronized void enqueue(T value) {
        while (size == capacity) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        indexIn = (++indexIn == capacity) ? 0 : indexIn;
        array[indexIn] = value;
        size++;
        notifyAll();
    }

    public synchronized T dequeue() {
        while (size == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        indexOut = (++indexOut == capacity) ? 0 : indexOut;
        size--;
        notifyAll();
        return (T) array[indexOut];
    }
}
