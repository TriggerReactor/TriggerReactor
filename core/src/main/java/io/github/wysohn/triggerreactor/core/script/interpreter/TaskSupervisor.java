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
     * @param run the task to be done on separate thread
     */
    void submitAsync(Runnable run);

    boolean isServerThread();

    /**
     * Simply create a new thread yet <b>not</b> start it.
     * This is extra interface to test the {@link io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager}
     * without actually starting the thread as needed.
     * @param runnable the task to be done on separate thread
     * @param name the name of the thread
     * @param priority the priority of the thread {@link Thread}.X_PRIORITY
     * @return the thread instance. Must be started manually.
     */
    Thread newThread(Runnable runnable, String name, int priority);
}
