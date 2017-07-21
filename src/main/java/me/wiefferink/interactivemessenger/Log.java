package me.wiefferink.interactivemessenger;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.logging.Logger;


public class Log {

	private static Logger logger;

	/**
	 * Set the logger
	 * @param newLogger Logger to use
	 */
	public static void setLogger(Logger newLogger) {
		logger = newLogger;
	}

	/**
	 * Print an information message to the console
	 * @param message The message to print
	 */
	public static void info(Object... message) {
		String combined = StringUtils.join(message, " ");
		if(logger != null) {
			logger.info(combined);
		} else {
			System.out.println(combined);
		}
	}

	/**
	 * Print a warning to the console
	 * @param message The message to print
	 */
	public static void warn(Object... message) {
		String combined = StringUtils.join(message, " ");
		if(logger != null) {
			logger.warning(combined);
		} else {
			System.out.println("Warn: " + combined);
		}
	}

	/**
	 * Print an error to the console
	 * @param message The message to print
	 */
	public static void error(Object... message) {
		String combined = StringUtils.join(message, " ");
		if(logger != null) {
			logger.severe(combined);
		} else {
			System.err.println(combined);
		}
	}

	/**
	 * Indent a message
	 * @param depth   Number of indents to add, one indent is two spaces
	 * @param message Message to indent
	 * @return Original message with requested number of indents added
	 */
	public static Object[] indent(int depth, Object... message) {
		String[] lines = StringUtils.join(message, " ").split("\\r?\\n");
		for(int i = 0; i < lines.length; i++) {
			lines[i] = StringUtils.leftPad("", depth * 2, " ") + lines[i];
		}
		return lines;
	}

	/**
	 * Print a string indented with a certain number of spaces
	 * @param indent  The number of indents (each indent is 2 spaces)
	 * @param message Message to print (will be split on newline to apply indentation)
	 */
	public static void infoIndent(int indent, Object... message) {
		for(Object line : indent(indent, message)) {
			info(line);
		}
	}

	/**
	 * Print a list of strings to the output with indentation
	 * @param indent The number of indents (each indent is 2 spaces)
	 * @param lines  The string to print (will be split on newline to apply indentation)
	 */
	public static void infoIndent(int indent, Collection<String> lines) {
		for(String part : lines) {
			infoIndent(indent, part);
		}
	}

	/**
	 * Print a string indented with a certain number of spaces
	 * @param indent  The number of indents (each indent is 2 spaces)
	 * @param message Message to print (will be split on newline to apply indentation)
	 */
	public static void warnIndent(int indent, Object... message) {
		for(Object line : indent(indent, message)) {
			warn(line);
		}
	}

	/**
	 * Print a list of strings to the output with indentation
	 * @param indent The number of indents (each indent is 2 spaces)
	 * @param lines  The string to print (will be split on newline to apply indentation)
	 */
	public static void warnIndent(int indent, Collection<String> lines) {
		for(String part : lines) {
			warnIndent(indent, part);
		}
	}

	/**
	 * Print a string indented with a certain number of spaces
	 * @param indent  The number of indents (each indent is 2 spaces)
	 * @param message Message to print (will be split on newline to apply indentation)
	 */
	public static void errorIndent(int indent, Object... message) {
		for(Object line : indent(indent, message)) {
			error(line);
		}
	}

	/**
	 * Print a list of strings to the output with indentation
	 * @param indent The number of indents (each indent is 2 spaces)
	 * @param lines  The string to print (will be split on newline to apply indentation)
	 */
	public static void errorIndent(int indent, Collection<String> lines) {
		for(String part : lines) {
			errorIndent(indent, part);
		}
	}
}
