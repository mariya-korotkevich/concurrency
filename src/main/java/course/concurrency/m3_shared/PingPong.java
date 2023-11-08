package course.concurrency.m3_shared;

import java.util.ArrayDeque;
import java.util.Queue;

public class PingPong {
    private static Queue<String> actions = new ArrayDeque<>();
    private static int count = 50;

    public static void main(String[] args) {
        actions.add("Ping");
        actions.add("Pong");
        actions.add("Pang");

        new Thread(() -> run("Ping")).start();
        new Thread(() -> run("Pong")).start();
        new Thread(() -> run("Pang")).start();
    }

    private static void run(String action){
        for (int i = 0; i < count; i++) {
            synchronized (PingPong.class) {
                while (!action.equals(actions.peek())){
                    try {
                        PingPong.class.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(action);
                actions.add(actions.poll());
                PingPong.class.notifyAll();
            }
        }
    }
}
