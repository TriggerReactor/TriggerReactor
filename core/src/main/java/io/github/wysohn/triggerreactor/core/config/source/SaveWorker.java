package io.github.wysohn.triggerreactor.core.config.source;

import io.github.wysohn.triggerreactor.tools.ValidationUtil;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SaveWorker extends Thread {
    private final int buffer;
    private final Consumer<Exception> failureHandle;

    private final long maxFlushInterval = 1000L;

    private Set<GsonConfigSource> saveQueue = new HashSet<>();
    private long count = 0;
    private long lastFlush = System.currentTimeMillis();
    private volatile boolean running = true;

    public SaveWorker(int buffer, Consumer<Exception> failureHandle) {
        ValidationUtil.validate(buffer > 0, "buffer must be greater than 0");
        ValidationUtil.notNull(failureHandle);

        this.buffer = buffer;
        this.failureHandle = failureHandle;

        start();
    }

    public SaveWorker(int buffer) {
        this(buffer, Throwable::printStackTrace);
    }

    void flush(GsonConfigSource source) {
        synchronized (this) {
            saveQueue.add(source);
            count++;
            notify();
        }
    }

    void shutdown() {
        if (!running)
            return;

        running = false;
        synchronized (this) {
            notify();
        }
    }

    private boolean bufferFilled() {
        // either buffer is filled or interval is reached
        return count >= buffer || System.currentTimeMillis() - lastFlush >= maxFlushInterval;
    }

    public void saveNow() {
        Set<GsonConfigSource> copy;
        synchronized (this) {
            copy = new HashSet<>(saveQueue);
            saveQueue.clear();

            count = 0;
            lastFlush = System.currentTimeMillis();
        }

        List<Exception> exceptions = new LinkedList<>();
        for (GsonConfigSource source : copy) {
            try {
                synchronized (source.file) {
                    source.cacheToFile();
                }

            } catch (Exception e) {
                e.printStackTrace();
                exceptions.add(e);
            }
        }

        for (Exception t : exceptions) {
            failureHandle.accept(t);
        }
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.interrupted()) {
                if (count == 0 || !bufferFilled()) {
                    synchronized (this) {
                        wait();
                    }

                    continue;
                }

                if (running) {
                    saveNow();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            failureHandle.accept(e);
        } finally {
            saveNow();
        }
    }
}
