package io.github.wysohn.triggerreactor.tools;

@FunctionalInterface
public interface ErrorProneRunnable {
    void run() throws Exception;
}