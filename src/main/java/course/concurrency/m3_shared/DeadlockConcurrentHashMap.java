package course.concurrency.m3_shared;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/*
compute
computeIfAbsent
merge
putVal
clear
 */
public class DeadlockConcurrentHashMap {
    public static void main(String[] args) {
        Map<Strange, Integer> map = new ConcurrentHashMap<>();

        new Thread(() -> {
            for (int i = 0; i < 1_000; i++) {
                map.put(new Strange(), i);
            }
        }).start();

        new Thread(() -> {
            for (int i = 0; i < 1_000; i++) {
                map.compute(new Strange(), (strange, integer) -> integer == null ? 0 : ++integer);
            }
        }).start();
    }

    private static class Strange {
        @Override
        public int hashCode() {
            return 1;
        }
    }
}
