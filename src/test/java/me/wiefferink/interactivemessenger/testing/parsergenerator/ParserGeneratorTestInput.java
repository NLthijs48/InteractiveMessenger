package me.wiefferink.interactivemessenger.testing.parsergenerator;

import com.google.common.base.Charsets;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParserGeneratorTestInput {

	public List<String> input;
	public String expectedConsoleOutput;
	public String expectedTellrawOutput;

	private ParserGeneratorTestInput() {
		input = new ArrayList<>();
		expectedConsoleOutput = "";
		expectedTellrawOutput = "";
	}

	/**
	 * Load test input from a fle
	 * @param file The file to load it from
	 * @return ParserGeneratorTestInput with content from the file
	 * @throws IOException if the file cannot be read
	 */
	public static ParserGeneratorTestInput from(File file) throws IOException {
		ParserGeneratorTestInput result = new ParserGeneratorTestInput();

		// Read input and output from the file
		StringBuilder expectedTellrawOutputBuilder = null;
		StringBuilder expectedConsoleOutputBuilder = null;
		boolean canIncrement = false;
		int state = 0; // 0: reading input,
		try(BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8))) {
			String line;
			while((line = fileReader.readLine()) != null) {
				// (part of) empty line barrier
				if(line.isEmpty()) {
					if(canIncrement) {
						state++;
						canIncrement = false;
					}
					continue;
				}

				canIncrement = true;
				// Input line
				if(state == 0) {
					result.input.add(line);
				}
				// Expected tellraw output line
				else if(state == 1) {
					if(expectedConsoleOutputBuilder == null) {
						expectedConsoleOutputBuilder = new StringBuilder();
					} else {
						expectedConsoleOutputBuilder.append("\n");
					}
					expectedConsoleOutputBuilder.append(line);
				}
				// Expected console output line
				else if(state == 2) {
					if(expectedTellrawOutputBuilder == null) {
						expectedTellrawOutputBuilder = new StringBuilder();
					} else {
						expectedTellrawOutputBuilder.append("\n");
					}
					expectedTellrawOutputBuilder.append(line);
				}
			}
		}

		result.expectedConsoleOutput = expectedConsoleOutputBuilder == null ? "" : expectedConsoleOutputBuilder.toString();
		result.expectedTellrawOutput = expectedTellrawOutputBuilder == null ? "" : expectedTellrawOutputBuilder.toString();
		return result;
	}

}
