/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskSupervisor {
    /**
     * Run task on the server thread. Usually it happens via scheduler.
     *
     * @param runnable the Runnable to run
     */
    void runTask(Runnable runnable);

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

    /**
     * Check if the current Thread is the Server
     *
     * @return
     */
    boolean isServerThread();

    /**
     * Simply create a new thread yet <b>not</b> start it.
     * This is extra interface to test the
     * {@link io.github.wysohn.triggerreactor.core.manager.trigger.repeating.RepeatingTriggerManager}
     * without actually starting the thread as needed.
     *
     * @param runnable the task to be done on separate thread
     * @param name     the name of the thread
     * @param priority the priority of the thread {@link Thread}.X_PRIORITY
     * @return the thread instance. Must be started manually.
     */
    Thread newThread(Runnable runnable, String name, int priority);

    /**
     * This will run in separate thread.
     *
     * @param task
     * @param mills
     */
    default void runTaskLater(Runnable task, long mills) {
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
    default void runTaskLater(Runnable task) {
        runTaskLater(task, 0L);
    }
}
