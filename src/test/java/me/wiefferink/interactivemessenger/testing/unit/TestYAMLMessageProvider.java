package me.wiefferink.interactivemessenger.testing.unit;

import me.wiefferink.interactivemessenger.source.YAMLMessageProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class TestYAMLMessageProvider {

	private static YAMLMessageProvider provider;

	@BeforeClass
	public static void initMessage() {
		URL resource = TestYAMLMessageProvider.class.getClassLoader().getResource("processing/messages.yml");
		assertNotNull("Resource file messages.yml not found in the jar", resource);
		provider = new YAMLMessageProvider(new File(resource.getFile()));
	}

	/**
	 * Compare the result of a MessageProvider with an expected list of values
	 * @param message  Key of the message to test
	 * @param expected List of values that are expected
	 */
	private void test(String message, String... expected) {
		assertEquals(Arrays.asList(expected), provider.getMessage(message));
	}

	@Test
	public void simple() {
		test("single", "Hello world!");
		test("multiple", "Hello ", "world!");
	}

	@Test
	public void multilineWithNewliens() {
		test("multilineWithNewlines1", "Hello world!\nSecond line\n");
		test("multilineWithNewlines2", "Hello world!\nSecond line");
		test("multilineWithNewlines3", "Hello world!\nSecond line\n\n");
	}

	@Test
	public void multilineWithoutNewliens() {
		test("multilineWithoutNewlines1", "Hello world! More text\n");
		test("multilineWithoutNewlines2", "Hello world! More text");
		test("multilineWithoutNewlines3", "Hello world! More text\n\n");
	}
}
