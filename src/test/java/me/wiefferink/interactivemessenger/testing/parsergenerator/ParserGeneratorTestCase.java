package me.wiefferink.interactivemessenger.testing.parsergenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import junit.framework.TestCase;
import me.wiefferink.interactivemessenger.generators.TellrawGenerator;
import me.wiefferink.interactivemessenger.message.InteractiveMessage;
import me.wiefferink.interactivemessenger.parsers.YamlParser;
import me.wiefferink.interactivemessenger.testing.Log;
import me.wiefferink.interactivemessenger.testing.RunTests;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParserGeneratorTestCase extends TestCase {

	private File file;
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();

	public ParserGeneratorTestCase(File file) {
		super();
		// Cleanup .txt from the name
		String name = file.getName();
		if(name.endsWith(".txt") && name.length() > 4) {
			name = name.substring(0, name.length()-4);
		}
		setName(name);
		this.file = file;
	}

	@Override
	protected void runTest() {
		Log.info("\n");
		Log.info("┌───────────────────────────────────────────────────────────────────────────────");
		Log.info("│ Test:", RunTests.getName(file));
		Log.info("└───────────────────────────────────────────────────────────────────────────────");

		// Read input and output from the file
		List<String> input = new ArrayList<>();
		StringBuilder expectedOutputBuilder = null;
		boolean barrierFound = false;
		try(BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			String line;
			while((line = fileReader.readLine()) != null) {
				if(line.isEmpty()) {
					barrierFound = true;
					continue;
				}

				if(barrierFound) {
					if(expectedOutputBuilder == null) {
						expectedOutputBuilder = new StringBuilder();
					} else {
						expectedOutputBuilder.append("\n");
					}
					expectedOutputBuilder.append(line);
				} else {
					input.add(line);
				}
			}
		} catch(IOException e) {
			error("Failed to read file: "+ExceptionUtils.getStackTrace(e));
		}

		String expectedOutput = expectedOutputBuilder == null ? "" : expectedOutputBuilder.toString();

		// Check if the input and output is defined
		if(input.isEmpty()) {
			error("Input is not defined");
			return;
		}

		Log.info("  Input:");
		Log.printIndented(2, input);
		Log.info("  Expected output:");
		Log.printIndented(2, expectedOutput);

		// Parse into InteractiveMessage
		InteractiveMessage parsedMessage = YamlParser.parse(input);
		Log.info("  Parsed InteractiveMessage:", parsedMessage);

		// Generate tellraw Json
		List<String> actualOutputList = TellrawGenerator.generate(parsedMessage);
		String actualOutputString = "["+StringUtils.join(actualOutputList, ",")+"]";

		// Parse actual output into Json
		JsonElement actualOutputJson;
		try {
			actualOutputJson = gson.fromJson(actualOutputString, JsonElement.class);
		} catch(JsonSyntaxException e) {
			error("Generated Json output is invalid:", actualOutputString, "\n\n"+ExceptionUtils.getStackTrace(e));
			return;
		}
		Log.info("  Generated Json:");
		Log.printIndented(2, gson.toJson(actualOutputJson));

		// Parse expected output into Json
		JsonElement expectedOutputJson;
		try {
			expectedOutputJson = gson.fromJson(expectedOutput, JsonElement.class);
		} catch(JsonSyntaxException e) {
			error("Expected output is invalid Json:", ExceptionUtils.getStackTrace(e));
			return;
		}

		// Test if equal
		assertEquals(gson.toJson(expectedOutputJson), gson.toJson(actualOutputJson));
	}

	/**
	 * Fail the test and print the error message
	 * @param parts The message parts
	 */
	private void error(Object... parts) {
		Log.error(parts);
		fail(StringUtils.join(parts, " "));
	}

}
