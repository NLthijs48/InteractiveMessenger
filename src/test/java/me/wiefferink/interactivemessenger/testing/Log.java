package me.wiefferink.interactivemessenger.testing;

import org.apache.commons.lang.StringUtils;

import java.util.List;


public class Log {

	/**
	 * Print an information message to the console
	 * @param message The message to print
	 */
	public static void info(Object... message) {
		System.out.println(StringUtils.join(message, " "));
	}

	/**
	 * Print an error to the console
	 * @param message The message to print
	 */
	public static void error(Object... message) {
		System.err.println(StringUtils.join(message, " "));
	}

	/**
	 * Print a string indented with a certain number of spaces
	 * @param indent The number of indents (each indent is 2 spaces)
	 * @param print  The string to print (will be split on newline to apply indentation)
	 */
	public static void printIndented(int indent, String print) {
		String indentation = "";
		for(int i = 0; i < indent; i++) {
			indentation += "  ";
		}
		String[] parts = print.split("\\r?\\n");
		for(String part : parts) {
			info(indentation+part);
		}
	}

	/**
	 * Print a list of strings to the output with indentation
	 * @param indent The number of indents (each indent is 2 spaces)
	 * @param print  The string to print (will be split on newline to apply indentation)
	 */
	public static void printIndented(int indent, List<String> print) {
		for(String part : print) {
			printIndented(indent, part);
		}
	}
}
