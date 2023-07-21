package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

final class EmptyTaskSupervisor implements TaskSupervisor {

    static final EmptyTaskSupervisor INSTANCE = new EmptyTaskSupervisor();

    @Override
    public <T> Future<T> submitSync(final Callable<T> callee) {
        // TODO(Sayakie)
        return null;
    }

    @Override
    public void submitAsync(final Runnable run) {
        // TODO(Sayakie)
    }

    @Override
    public boolean isServerThread() {
        // TODO(Sayakie)
        return true;
    }

    @Override
    public Thread newThread(final Runnable runnable, final String name, final int priority) {
        // TODO(Sayakie)
        return null;
    }

    @Override
    public boolean equals(final Object that) {
        // TODO(Sayakie)
        return this == that;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "EmptyTaskSupervisor";
    }

}
