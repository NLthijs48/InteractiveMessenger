package me.wiefferink.interactivemessenger.testing.ingame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import me.wiefferink.interactivemessenger.generators.TellrawGenerator;
import me.wiefferink.interactivemessenger.message.InteractiveMessage;
import me.wiefferink.interactivemessenger.parsers.YamlParser;
import me.wiefferink.interactivemessenger.testing.Log;
import me.wiefferink.interactivemessenger.testing.parsergenerator.ParserGeneratorTestInput;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestCase implements Comparable<TestCase> {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();

	enum TestResult {
		SUCCESS,
		FAILED

	}

	private File testFile;
	private TestResult result;
	private TestRun testRun;

	public TestCase(TestRun testRun, File testFile) {
		this.testRun = testRun;
		this.testFile = testFile;
	}

	/**
	 * Get the name of the test case (path leading to the file)
	 * @return Name of this testcase
	 */
	public String getName() {
		String result = testFile.getAbsolutePath().substring(IngameTest.resourcesFolder.getAbsolutePath().length()+1);
		if(result.startsWith("parsergenerator"+File.separator)) {
			result = result.substring(15+File.separator.length());
		}
		result = result.replace("\\", "/");
		if(result.endsWith(".txt")) {
			result = result.substring(0, result.length()-4);
		}
		return result;
	}

	/**
	 * Show the test case
	 */
	public void show() {
		// Bump away last case
		testRun.cleanScreen();
		testRun.info(getName());
		ParserGeneratorTestInput data;
		try {
			data = ParserGeneratorTestInput.from(testFile);
		} catch(IOException e) {
			testRun.error("Failed to load test data:", testFile.getAbsolutePath(), "\n", ExceptionUtils.getStackTrace(e));
			setupFailed();
			return;
		}

		// Show input
		if(data.input.isEmpty()) {
			testRun.error("Input is empty:", testFile.getAbsolutePath());
			setupFailed();
			return;
		}

		testRun.info("Input of", data.input.size(), "line"+(data.input.size() == 1 ? "" : "s")+":");
		for(String inputPart : data.input) {
			testRun.message(inputPart);
		}

		// Show output
		InteractiveMessage parsedMessage = YamlParser.parse(data.input);
		List<String> tellRawOutput = TellrawGenerator.generate(parsedMessage);
		testRun.info("Output of", +tellRawOutput.size(), "line"+(tellRawOutput.size() == 1 ? "" : "s")+":");
		boolean result = true;
		int line = 1;
		for(String jsonMessage : tellRawOutput) {
			// Parse actual output into Json
			JsonElement tellRawJsonOutput;
			try {
				tellRawJsonOutput = gson.fromJson(jsonMessage, JsonElement.class);
				testRun.console("Message structure:", parsedMessage);
				testRun.console("Output line", line+":");
				Log.printIndented(2, gson.toJson(tellRawJsonOutput));
				result &= Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+testRun.getTester().getName()+" "+jsonMessage);
			} catch(JsonSyntaxException e) {
				result = false;
				testRun.error("Generated Json output is invalid:", e.getMessage(), ExceptionUtils.getStackTrace(e));
				testRun.error("Raw json:", jsonMessage);
			} catch(Exception e) {
				testRun.error("Exception at invoking /tellraw:", e.getMessage(), ExceptionUtils.getStackTrace(e));
				result = false;
			}
			line++;
		}

		if(!result) {
			setupFailed();
			return;
		}

		// Ask if the output is correct (no hover because popup is above output)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+testRun.getTester().getName()
				+" ["+
				// Prefix
				"    {"+
				"        \"text\": \">>> Result: \","+
				"        \"color\": \"blue\","+
				"        \"bold\": true"+
				"    },"+
				// Correct button
				"    {"+
				"        \"text\": \"[Correct]\","+
				"        \"color\": \"dark_green\","+
				"        \"bold\": true,"+
				"        \"clickEvent\": {"+
				"            \"action\": \"run_command\","+
				"            \"value\": \"/testinteractivemessenger correct\""+
				"        }"+
				"    },"+
				// Space
				"    {"+
				"        \"text\": \" \""+
				"    },"+
				// Wrong button
				"    {"+
				"        \"text\": \"[Wrong]\","+
				"        \"color\": \"dark_red\","+
				"        \"bold\": true,"+
				"        \"clickEvent\": {"+
				"            \"action\": \"run_command\","+
				"            \"value\": \"/testinteractivemessenger wrong\""+
				"        }"+
				"    },"+
				// Progress
				"    {"+
				"        \"text\": \" "+(testRun.getProgress()+1)+"/"+testRun.getTestCases().size()+"\","+
				"        \"color\": \"blue\","+
				"        \"bold\": true"+
				"    }"+
				"]");
	}

	/**
	 * Show test case setup failed message with continue button
	 */
	private void setupFailed() {
		// Sending/parsing already failed, show continue button
		setResult(TestResult.FAILED);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+testRun.getTester().getName()
				+" ["+
				"    {"+
				"        \"text\": \">>> Test case setup failed! \","+
				"        \"color\": \"dark_red\","+
				"        \"bold\": true"+
				"    },"+
				"    {"+
				"        \"text\": \"[Continue]\","+
				"        \"color\": \"blue\","+
				"        \"bold\": true,"+
				"        \"clickEvent\": {"+
				"            \"action\": \"run_command\","+
				"            \"value\": \"testinteractivemessenger continue\""+
				"        },"+
				"        \"hoverEvent\": {"+
				"            \"action\": \"show_text\","+
				"            \"value\": {"+
				"                \"text\": \"\","+
				"                \"extra\": ["+
				"                    {"+
				"                        \"text\": \"<Continue to the next test case>\","+
				"                        \"color\": \"blue\""+
				"                    }"+
				"                ]"+
				"            }"+
				"        }"+
				"    }"+
				"]");
	}

	/**
	 * The feedback of the player
	 * @param result The result given by the player
	 */
	public void result(TestResult result) {
		this.result = result;
	}

	/**
	 * Set the text result
	 * @param result The result to set
	 */
	public void setResult(TestResult result) {
		this.result = result;
	}

	/**
	 * Get the result of the test
	 * @return The result of the test
	 */
	public TestResult getResult() {
		return result;
	}

	@Override
	public int compareTo(TestCase otherTestCase) {
		return otherTestCase.getName().compareTo(getName());
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}
}
