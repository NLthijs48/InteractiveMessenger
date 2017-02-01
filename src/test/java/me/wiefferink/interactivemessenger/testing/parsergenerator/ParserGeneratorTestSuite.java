package me.wiefferink.interactivemessenger.testing.parsergenerator;

import junit.framework.TestSuite;
import me.wiefferink.interactivemessenger.testing.Log;

import java.io.File;

public class ParserGeneratorTestSuite extends TestSuite {

	public ParserGeneratorTestSuite(File directory, boolean first) {
		super(directory.getName());

		// Create a testsuite for each directory
		File[] files = directory.listFiles();
		if(files == null) {
			Log.info("No files found at:", directory.getAbsolutePath());
			return;
		}
		for(File file : files) {
			if(file.isDirectory()) {
				addTest(new ParserGeneratorTestSuite(file, false));
			} else if(file.isFile() && !first) {
				addTest(new ParserGeneratorTestCase(file));
			}
		}
	}

}
