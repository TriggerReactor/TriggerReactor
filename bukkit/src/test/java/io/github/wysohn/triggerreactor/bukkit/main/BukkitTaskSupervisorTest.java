package io.github.wysohn.triggerreactor.bukkit.main;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BukkitTaskSupervisorTest {

    BukkitTaskSupervisor bukkitTaskSupervisor = new BukkitTaskSupervisor();

    @Test
    public void submitAsync() throws Exception {
        // arrange
        Task[] tasks = new Task[3000];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new Task();
        }

        // act
        for (Task task : tasks) {
            bukkitTaskSupervisor.submitAsync(task);
        }

        Thread.sleep(5000L);
        BukkitTaskSupervisor.CACHED_THREAD_POOL.shutdown();

        // assert
        for (Task task : tasks) {
            assertTrue(task.done);
        }
    }

    private static class Task implements Runnable {
        private boolean done = false;

        @Override
        public void run() {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            done = true;
        }
    }
}
