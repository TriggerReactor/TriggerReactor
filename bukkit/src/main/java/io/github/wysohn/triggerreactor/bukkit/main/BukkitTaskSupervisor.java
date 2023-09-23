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

package io.github.wysohn.triggerreactor.bukkit.main;

import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.*;

@Singleton
public class BukkitTaskSupervisor implements TaskSupervisor {
    @Inject
    private JavaPlugin plugin;

    protected static final ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool();

    @Override
    public boolean isServerThread() {
        boolean result = false;

        synchronized (this) {
            result = Bukkit.isPrimaryThread();
        }

        return result;
    }

    @Override
    public Thread newThread(Runnable runnable, String name, int priority) {
        Thread thread = new Thread(runnable, name);
        thread.setPriority(priority);
        return thread;
    }

    /**
     * Create a future that will be executed upon creation.
     *
     * @param call
     * @param <T>
     * @return
     */
    private <T> Future<T> immediateFuture(Callable<T> call) {
        return new Future<T>() {
            private T result = null;
            private Exception exception = null;

            {
                try {
                    result = call.call();
                } catch (Exception e) {
                    exception = e;
                }
            }

            @Override
            public boolean cancel(boolean arg0) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public T get() throws ExecutionException {
                if (exception != null)
                    throw new ExecutionException(exception);

                return result;
            }

            @Override
            public T get(long arg0, TimeUnit arg1)
                    throws ExecutionException {
                if (exception != null)
                    throw new ExecutionException(exception);

                return result;
            }

        };
    }

    @Override
    public <T> Future<T> submitSync(Callable<T> call) {
        if (!plugin.isEnabled()) {
            throw new IllegalStateException("Plugin is not enabled. If you see this error while" +
                    " the server is shutting down, you can simply ignore this.");
        }

        if (this.isServerThread()) {
            return immediateFuture(call);
        } else {
            return Bukkit.getScheduler().callSyncMethod(plugin, call);
        }
    }

    @Override
    public void submitAsync(Runnable run) {
        CACHED_THREAD_POOL.submit(run);
    }

    @Override
    public void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

}
