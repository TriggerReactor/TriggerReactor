package io.github.wysohn.triggerreactor.tools;

@FunctionalInterface
public interface ErrorProneRunnable {
	public void run() throws Exception;
}