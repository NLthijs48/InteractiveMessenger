package me.wiefferink.interactivemessenger.processing;

/**
 * Provide replacement for a class to insert into messages
 */
public interface ReplacementProvider {
	/**
	 * Get the replacement for a variable
	 * @param variable The variable to replace
	 * @return The replacement for the variable, or null if empty
	 */
	Object provideReplacement(String variable);
}
