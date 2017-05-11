package io.github.wysohn.triggerreactor.core.interpreter;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import io.github.wysohn.triggerreactor.main.TriggerReactor;

public abstract class Executor {
    public static final int STOP = 0;
    public static final int WAIT = 1;
    public static final int COOLDOWN = 2;

    /**
     *
     * @param context
     * @param args
     * @return usually null; return code to intercept execution
     */
    protected abstract Integer execute(Object context, Object... args);

    /**
     * This will run in separate thread.
     * @param task
     * @param mills
     */
    public static void runTaskLater(Runnable task, long mills){
        new Thread(new Runnable(){
            @Override
            public void run() {
                if(mills > 0){
                    try {
                        Thread.sleep(mills);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                task.run();
            }
        }).start();
    }

    /**
     * This will run in separate thread.
     * @param task
     * @param mills
     */
    public static void runTaskLater(Runnable task){
        runTaskLater(task, 0L);
    }

    private static class BukkitAdapter implements Callable<Void>{
        private final Runnable run;
        private BukkitAdapter(Runnable run) {
            super();
            this.run = run;
        }

        @Override
        public Void call() throws Exception {
            BukkitTask task = Bukkit.getScheduler().runTask(TriggerReactor.getInstance(), run);
            while(Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId())){
                Thread.yield();
            }
            return null;
        }
    }

    /**
     * This will block the thread until the bukkit task is done. So do not call this on main thread.
     * @param task
     */
    public static void runBukkitTask(Runnable task){
        BukkitAdapter adapter = new BukkitAdapter(task);
        Future<Void> future = TriggerReactor.cachedThreadPool.submit(adapter);
        while(!future.isDone())
            Thread.yield();
    }

    public static <T> Future<T> runBukkitTaskForFuture(Callable<T> call){
        return Bukkit.getScheduler().callSyncMethod(TriggerReactor.getInstance(), call);
    }
}
