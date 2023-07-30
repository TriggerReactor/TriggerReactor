package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.concurrent.*;

final class EmptyTaskSupervisor implements TaskSupervisor {

    static final EmptyTaskSupervisor INSTANCE = new EmptyTaskSupervisor();

    @Override
    public <T> Future<T> submitSync(final Callable<T> callee) {
        return new Future<T>() {

            private boolean processed;

            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return processed;
            }

            @Override
            public T get() throws ExecutionException {
                final T identity;

                try {
                    identity = callee.call();
                    processed = true;
                } catch (final Exception e) {
                    throw new ExecutionException(e);
                }

                return identity;
            }

            @Override
            public T get(final long timeout, final TimeUnit unit) throws ExecutionException {
                return get();
            }
        };
    }

    @Override
    public void submitAsync(final Runnable run) {
        newThread(run, toString(), Thread.MIN_PRIORITY).start();
    }

    @Override
    public boolean isServerThread() {
        return true;
    }

    @Override
    public Thread newThread(final Runnable runnable, final String name, final int priority) {
        final Thread thread = new Thread(runnable);
        thread.setName(name);
        thread.setPriority(priority);

        return thread;
    }

    @Override
    public boolean equals(final Object that) {
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
