package me.wiefferink.interactivemessenger.testing;

import junit.framework.Test;
import junit.framework.TestSuite;
import me.wiefferink.interactivemessenger.testing.parsergenerator.ParserGeneratorTestSuite;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Arrays;

public class RunTests {

	public static final File testDirectory = new File(StringUtils.join(Arrays.asList("src", "test", "java", "me", "wiefferink", "interactivemessenger", "testing"), File.separator));
	public static final File parserGenerator = new File(testDirectory.getAbsolutePath()+File.separator+"parsergenerator");

	public static Test suite() {
		TestSuite suite = new TestSuite("InteractiveMessenger tests");
		suite.addTest(new ParserGeneratorTestSuite(parserGenerator, true));
		return suite;
	}

	/**
	 * Get a name for a directory or test
	 * @param file The location of the directory/test
	 * @return Directory structure leading to the given file from the base test directory
	 */
	public static String getName(File file) {
		return file.getAbsolutePath().substring(testDirectory.getAbsolutePath().length()+1);
	}
}
