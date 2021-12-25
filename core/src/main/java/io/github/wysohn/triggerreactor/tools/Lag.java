package io.github.wysohn.triggerreactor.tools;

//https://bukkit.org/threads/get-server-tps.143410/
public class Lag implements Runnable {
    public int TICK_COUNT = 0;
    public long[] TICKS = new long[600];

    @Override
    public void run() {
        TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();

        TICK_COUNT += 1;
    }

    public long getElapsed(int tickID) {
        if (TICK_COUNT - tickID >= TICKS.length) {
        }

        long time = TICKS[(tickID % TICKS.length)];
        return System.currentTimeMillis() - time;
    }

    public double getTPS(int ticks) {
        if (TICK_COUNT < ticks) {
            return 20.0D;
        }
        int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
        long elapsed = System.currentTimeMillis() - TICKS[target];

        return ticks / (elapsed / 1000.0D);
    }

    public double getTPS() {
        return getTPS(100);
    }
}