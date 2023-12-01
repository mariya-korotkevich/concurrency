package course.concurrency.m6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyBlockingQueueTest {
    private static final int CAPACITY = 20;
    private MyBlockingQueue<Integer> queue;

    @BeforeEach
    void init() {
        queue = new MyBlockingQueue<>(CAPACITY);
    }

    @Test
    void shouldReadSameObjectThatWasWritten() {
        Integer expected = 1;
        queue.enqueue(expected);

        Integer actual = queue.dequeue();

        assertEquals(expected, actual);
    }

    @Test
    void shouldReadInFifoOrder() {
        List<Integer> expected = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        expected.forEach(queue::enqueue);

        expected.forEach((i -> assertEquals(i, queue.dequeue())));
    }

    @Test
    void loadTest() throws InterruptedException {
        int count = 5000;
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < count; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                queue.enqueue(1);
            });
            executorService.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                queue.dequeue();
            });
        }
        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(0, queue.getSize());
    }

    @Test
    void shouldWriteInOneThreadAndReadInAnotherThreadInFifoOrder() throws InterruptedException {
        ExecutorService executors = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(1);

        int count = 4000;
        executors.execute(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < count; i++) {
                queue.enqueue(i);
            }
        });

        List<Integer> actual = new ArrayList<>();
        executors.execute(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < count; i++) {
                actual.add(queue.dequeue());
            }
        });
        latch.countDown();

        executors.shutdown();
        executors.awaitTermination(10, TimeUnit.SECONDS);

        for (int i = 0; i < count; i++) {
            assertEquals(i, actual.get(i));
        }
    }

    @Test
    void shouldReadingThreadWaitingWhileQueueEmpty() throws InterruptedException {
        Thread readingThread = new Thread(() -> queue.dequeue());
        Thread writingThread = new Thread(() -> queue.enqueue(1));

        readingThread.start();
        Thread.sleep(10);
        assertEquals(Thread.State.WAITING, readingThread.getState());

        writingThread.start();
        Thread.sleep(10);

        assertEquals(Thread.State.TERMINATED, readingThread.getState());
    }

    @Test
    void shouldWritingThreadWaitingWhileQueueFull() throws InterruptedException {
        Thread writingThread = new Thread(() -> {
            for (int i = 0; i < CAPACITY + 1; i++) {
                queue.enqueue(i);
            }
        });
        Thread readingThread = new Thread(() -> queue.dequeue());

        writingThread.start();
        Thread.sleep(10);

        assertEquals(Thread.State.WAITING, writingThread.getState());

        readingThread.start();
        Thread.sleep(10);

        assertEquals(Thread.State.TERMINATED, writingThread.getState());
    }
}
