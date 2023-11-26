package course.concurrency.m5_streams;

import java.util.concurrent.*;

public class ThreadPoolTask {

    // Task #1
    public ThreadPoolExecutor getLifoExecutor() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>() {
            @Override
            public boolean offer(Runnable runnable) {
                return offerFirst(runnable);
            }
        });
    }

    // Task #2
    public ThreadPoolExecutor getRejectExecutor() {
        return new ThreadPoolExecutor(8, 8, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
                new ThreadPoolExecutor.DiscardPolicy());
    }
}
