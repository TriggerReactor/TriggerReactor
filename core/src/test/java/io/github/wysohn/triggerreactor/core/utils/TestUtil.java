package io.github.wysohn.triggerreactor.core.utils;

import java.util.function.Predicate;

import org.junit.Assert;

import io.github.wysohn.triggerreactor.tools.ErrorProneRunnable;

public class TestUtil {
	//assert that a runnable threw an error
	public static void assertError(ErrorProneRunnable run)
	{
		try {
			run.run();
		}
		catch (Exception e) {
			return;
		}
		Assert.fail("runnable did not throw any exception");
	}
	
	//assert that a runnable threw an error message with the content Error: + expectedMessage
	public static void assertError(ErrorProneRunnable run, String expectedMessage)
	{
		try {
			assertError(run, message -> message.equals("Error: " + expectedMessage));
		} catch (AssertionError e) {
			if (e.getMessage().equals("runnable did not throw any exception")) {
				throw e;
			} else {
				Assert.fail(e.getMessage() + ", expected: \"" + expectedMessage + "\"");
			}
		}
	}
	
	//assert that a runnable threw an error message that matches the predicate
	public static void assertError(ErrorProneRunnable run, Predicate<String> messageTest)
	{
		try {
			run.run();
		}
		catch (Exception e) {
			if (messageTest.test(e.getCause().getMessage())) return;
			Assert.fail("Exeption message predicate failed to match message: \"" + e.getCause().getMessage() + "\"");
		}
		Assert.fail("runnable did not throw any exception");
	}
}
