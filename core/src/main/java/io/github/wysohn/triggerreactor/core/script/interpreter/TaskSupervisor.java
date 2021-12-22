package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskSupervisor {
    /**
     * This will run in separate thread.
     *
     * @param task
     * @param mills
     */
    static void runTaskLater(Runnable task, long mills) {
        new Thread(() -> {
            if (mills > 0) {
                try {
                    Thread.sleep(mills);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            task.run();
        }).start();
    }

    /**
     * This will run in separate thread.
     *
     * @param task
     * @param mills
     */
    static void runTaskLater(Runnable task) {
        runTaskLater(task, 0L);
    }

    /**
     * Execute the task synchronously if possible. The task Supervisor
     * should check if the caller thread is Server or not and schedule
     * the task appropriately.
     *
     * @param call the task to be done on server thread
     * @return future instance
     */
    <T> Future<T> submitSync(Callable<T> call);

    /**
     * Run task in next server tick
     * @param runnable
     */
    void runTask(Runnable runnable);

    /**
     * Execute the task asynchronously on separate thread. Regardless
     * of the caller thread (either server or separate thread), it will
     * always spawn a new thread that will run concurrently.
     *
     * @param run the task to be done on separate thread
     */
    void submitAsync(Runnable run);

    boolean isServerThread();
}
