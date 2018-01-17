package io.github.wysohn.triggerreactor.core.script.interpreter;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import io.github.wysohn.triggerreactor.core.main.TriggerReactor;

public abstract class SynchronizableTask {

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

    public static <T> Future<T> runSyncTaskForFuture(Callable<T> call){
        return TriggerReactor.getInstance().callSyncMethod(call);
    }
}
