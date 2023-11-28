package course.concurrency.m6;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyBlockingQueueTest {
    private final MyBlockingQueue<Integer> queue = new MyBlockingQueue<>(20);

    @Test
    void addOneElement() {
        Integer expected = 1;
        queue.enqueue(expected);

        Integer actual = queue.dequeue();

        assertEquals(expected, actual);
    }

    @Test
    void addElementsMoreThanCapacity() {
        int count = 40;

        List<Integer> actual = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            queue.enqueue(i);
            actual.add(queue.dequeue());
        }

        for (int i = 0; i < count; i++) {
            assertEquals(i, actual.get(i));
        }
    }

    @Test
    void oneReadOneWriteThreads() throws InterruptedException {
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
    void manyReadOneWriteThreads() throws InterruptedException {
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

        ArrayBlockingQueue<Integer> actual = new ArrayBlockingQueue<>(count);
        for (int i = 0; i < 4; i++) {
            executors.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (int j = 0; j < count / 4; j++) {
                    actual.add(queue.dequeue());
                }
            });
        }

        latch.countDown();

        executors.shutdown();
        executors.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(count, actual.size());
    }

}
