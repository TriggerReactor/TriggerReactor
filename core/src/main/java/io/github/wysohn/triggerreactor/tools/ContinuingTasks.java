package io.github.wysohn.triggerreactor.tools;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ContinuingTasks {
    private final List<Runnable> tasks = new LinkedList<>();

    private ContinuingTasks() {

    }

    public void run(Consumer<Throwable> consumer) {
        for (Runnable task : tasks) {
            try {
                task.run();
            } catch (Exception ex) {
                consumer.accept(ex);
            }
        }
    }

    public static class Builder {
        private final ContinuingTasks tasks = new ContinuingTasks();

        public Builder append(Runnable run) {
            tasks.tasks.add(run);
            return this;
        }

        public void run() {
            tasks.run(Throwable::printStackTrace);
        }

        public void run(Consumer<Throwable> consumer) {
            tasks.run(consumer);
        }
    }
}
