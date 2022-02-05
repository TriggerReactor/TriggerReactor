/*
 *     Copyright (C) 2021 wysohn and contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.wysohn.triggerreactor.bukkit.main;

import dagger.Lazy;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.main.IThrowableHandler;
import io.github.wysohn.triggerreactor.core.script.interpreter.TaskSupervisor;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BukkitTaskSupervisor implements TaskSupervisor {
    @Inject
    Lazy<Server> server;
    @Inject
    @Named("PluginInstance")
    Object pluginInstance;
    @Inject
    IGameController gameController;
    @Inject
    IThrowableHandler throwableHandler;

    @Inject
    public BukkitTaskSupervisor() {

    }

    @Override
    public boolean isServerThread() {
        return server.get().isPrimaryThread();
    }

    @Override
    public void runTask(Runnable runnable) {
        server.get().getScheduler().runTask((Plugin) pluginInstance, runnable);
    }

    @Override
    public void submitAsync(Runnable run) {
        try {
            new Thread(run).start();
        } catch (Exception ex) {
            throwableHandler.handleException((Object) null, ex);
        }
    }

    @Override
    public <T> Future<T> submitSync(Callable<T> call) {
        if (this.isServerThread()) {
            return new Future<T>() {
                private boolean done = false;

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
                    return done;
                }

                @Override
                public T get() throws ExecutionException {
                    T out = null;
                    try {
                        out = call.call();
                        done = true;
                    } catch (Exception e) {
                        throw new ExecutionException(e);
                    }
                    return out;
                }

                @Override
                public T get(long arg0, TimeUnit arg1) throws ExecutionException {
                    T out = null;
                    try {
                        out = call.call();
                        done = true;
                    } catch (Exception e) {
                        throw new ExecutionException(e);
                    }
                    return out;
                }

            };
        } else {
            return gameController.callSyncMethod(call);
        }
    }
}
