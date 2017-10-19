package me.wiefferink.interactivemessenger.testing.unit;

import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.source.MessageProvider;
import me.wiefferink.interactivemessenger.source.YAMLMessageProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class TestMessage {

	@BeforeClass
	public static void initMessage() {
		URL resource = TestMessage.class.getClassLoader().getResource("processing/messages.yml");
		assertNotNull("Resource file messages.yml not found in the jar", resource);
		MessageProvider provider = new YAMLMessageProvider(new File(resource.getFile()));
		Message.init(provider, null);
	}

	@Test
	public void staticEmpty() {
		Message message = Message.empty();
		assertTrue(message.isEmpty());
		assertTrue(message.get().isEmpty());
		assertTrue(message.getPlain().isEmpty());
		assertTrue(message.getSingle().isEmpty());
		assertTrue(message.getSingleRaw().isEmpty());
		assertTrue(message.getRaw().isEmpty());
	}

	private void testFromString(String message, String... expected) {
		assertEquals(Arrays.asList(expected), Message.fromString(message).getRaw());
	}

	@Test
	public void staticFromString() {
		testFromString(null);
		testFromString("", "");
		testFromString("a", "a");
		testFromString("abc", "abc");
	}

	private void testFromList(List<String> message, String... expected) {
		assertEquals(Arrays.asList(expected), Message.fromList(message).getRaw());
	}

	@Test
	public void staticFromList() {
		testFromList(null);
		testFromList(Collections.<String>emptyList());
		testFromList(Collections.singletonList("a"), "a");
		testFromList(Arrays.asList("", ""), "", "");
		testFromList(Arrays.asList("abc", "def"), "abc", "def");
		testFromList(Arrays.asList("abc", "", "def"), "abc", "", "def");
	}

	private void testFromKey(String key, String... expected) {
		assertEquals(Arrays.asList(expected), Message.fromKey(key).getRaw());
	}

	@Test
	public void staticFromKey() {
		testFromKey(null);
		testFromKey("doestNotExist");
		testFromKey("single", "Hello world!");
		testFromKey("multiple", "Hello ", "world!");

		testFromKey("multilineWithNewlines1", "Hello world!\nSecond line\n");
		testFromKey("multilineWithNewlines2", "Hello world!\nSecond line");
		testFromKey("multilineWithNewlines3", "Hello world!\nSecond line\n\n");

		testFromKey("multilineWithoutNewlines1", "Hello world! More text\n");
		testFromKey("multilineWithoutNewlines2", "Hello world! More text");
		testFromKey("multilineWithoutNewlines3", "Hello world! More text\n\n");
	}

	@Test
	public void isEmpty() {
		assertTrue(Message.fromString(null).isEmpty());
		assertTrue(Message.fromString("").isEmpty());
		assertFalse(Message.fromString("a").isEmpty());

		assertTrue(Message.fromList(new ArrayList<String>()).isEmpty());
		assertTrue(Message.fromList(Collections.singletonList("")).isEmpty());
		assertFalse(Message.fromList(Collections.singletonList("a")).isEmpty());
		assertTrue(Message.fromList(Arrays.asList("", "")).isEmpty());
		assertFalse(Message.fromList(Arrays.asList("a", "")).isEmpty());
		assertFalse(Message.fromList(Arrays.asList("", "a")).isEmpty());
	}

	@Test
	public void prefix() {
		assertFalse(Message.empty().prefix().isEmpty());
		assertTrue(Message.empty().prefix(false).isEmpty());
		assertFalse(Message.empty().prefix(true).isEmpty());
	}

	@Test
	public void append() {
		// Append a String
		assertEquals(Collections.singletonList("abc"), Message.empty().append("abc").getRaw());
		assertEquals(Arrays.asList("abc", "def"), Message.empty().append("abc").append("def").getRaw());

		// Append a List
		assertEquals(Collections.emptyList(), Message.empty().append(Collections.<String>emptyList()).getRaw());
		assertEquals(Collections.singletonList("abc"), Message.empty().append(Collections.singletonList("abc")).getRaw());
		assertEquals(Arrays.asList("a", "bc"), Message.empty().append(Arrays.asList("a", "bc")).getRaw());
		assertEquals(Arrays.asList("a", "bc", "de", "f"), Message.empty().append(Arrays.asList("a", "bc")).append(Arrays.asList("de", "f")).getRaw());

		// Append a Message
		// TODO test if appended message has replacement already done? (they should be)
		assertEquals(Collections.emptyList(), Message.empty().append(Message.empty()).getRaw());
		assertEquals(Collections.singletonList("abc"), Message.empty().append(Message.fromString("abc")).getRaw());
		assertEquals(Arrays.asList("a", "bc"), Message.empty().append(Message.fromList(Arrays.asList("a", "bc"))).getRaw());
		assertEquals(Arrays.asList("a", "bc", "de", "f"), Message.empty().append(Message.fromList(Arrays.asList("a", "bc"))).append(Message.fromList(Arrays.asList("de", "f"))).getRaw());
	}

	@Test
	public void prepend() {
		// Append a String
		assertEquals(Collections.singletonList("abc"), Message.empty().prepend("abc").getRaw());
		assertEquals(Arrays.asList("def", "abc"), Message.empty().prepend("abc").prepend("def").getRaw());

		// Append a List
		assertEquals(Collections.emptyList(), Message.empty().prepend(Collections.<String>emptyList()).getRaw());
		assertEquals(Collections.singletonList("abc"), Message.empty().prepend(Collections.singletonList("abc")).getRaw());
		assertEquals(Arrays.asList("a", "bc"), Message.empty().prepend(Arrays.asList("a", "bc")).getRaw());
		assertEquals(Arrays.asList("de", "f", "a", "bc"), Message.empty().prepend(Arrays.asList("a", "bc")).prepend(Arrays.asList("de", "f")).getRaw());

		// Append a Message
		// TODO test if prepended message has replacement already done? (they should be)
		assertEquals(Collections.emptyList(), Message.empty().prepend(Message.empty()).getRaw());
		assertEquals(Collections.singletonList("abc"), Message.empty().prepend(Message.fromString("abc")).getRaw());
		assertEquals(Arrays.asList("a", "bc"), Message.empty().prepend(Message.fromList(Arrays.asList("a", "bc"))).getRaw());
		assertEquals(Arrays.asList("de", "f", "a", "bc"), Message.empty().prepend(Message.fromList(Arrays.asList("a", "bc"))).prepend(Message.fromList(Arrays.asList("de", "f"))).getRaw());
	}


}
