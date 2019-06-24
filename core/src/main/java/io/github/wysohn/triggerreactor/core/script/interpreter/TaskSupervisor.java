package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskSupervisor {
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
     * Execute the task asynchronously on separate thread. Regardless
     * of the caller thread (either server or separate thread), it will
     * always spawn a new thread that will run concurrently.
     *
     * @param call the task to be done on separate thread
     */
    void submitAsync(Runnable run);
}
