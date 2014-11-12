import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * FancyMessageFormat converter, a library that enables to convert
 * messages in the FancyMessageFormat to Minecraft's bulky tellraw
 * format.
 *
 * TODO: Choose license (MIT License?)
 *
 * @author NLThijs48
 * @author Tobias aka Phoenix
 */
public class FancyMessageFormatConverter {

	public static final char tagStart = '[';
	public static final char tagEnd = ']';
	
	/** The special character that prefixes all chat formatting codes. */
	final char SIMPLE_FORMAT_CHAR = '\u00A7';

	/** Lookup table for all continuous tags (marked by []) */
	public static HashMap<String, SimpleMessageTag> bracketTagList = new HashMap<String, SimpleMessageTag>();

	static {
		// Enlist all possible bracket-tags
		// (They go into a HashMap for lookup purposes)
		for(Color color : Color.values()) {
			for(String tag : color.getTags()) {
				bracketTagList.put(tag, color);
			}
		}
		for(FormatType format : FormatType.values()) {
			for(String tag : format.getTags()) {
				bracketTagList.put(tag, format);
			}
		}
	}
	
	
	// convertToJSON("My \n != [break] Message")
	
	/*
	 * "Any text with [TAG] formatting
	 *  that spreads accross multiple lines.
	 *      hover: the second line has a hover
	 *  At the end of every line (even tough you cannot
	 *  see it here) you will find a line break (\n).  "
	 *  
	 *  "This is another line"
	 *  "    hover: The 5th line is above me. Is also hovers"
	 *  "Another line"
	 *  
	 *  ->
	 *  "Any..."
	 *  "that..."
	 *  "At..."
	 *  "see.."
	 *  "This..."
	 *  
	 *  ->
	 *  
	 *  "Any... \nthat... \nAt...
	 */
	
	
	public static String convertToJSON(final String line) {
		List<String> list = new ArrayList<String>();
		list.add(line);
		return convertToJSON(list);
	}
	
	public static String convertToJSON(final List<String> inputLines) {
		ArrayList<String> lines = new ArrayList<>();
		for(String line : inputLines) {
			// Split lines at line breaks
			// In the end we will have a list with one line per element
			lines.addAll(Arrays.asList(line.split("\\r?\\n")));
		}
		
		
		List<InteractiveMessagePart> message = new ArrayList<InteractiveMessagePart>();
				
		for(String line : lines) {
			//
			
			int nextTagPosition = getNextTagPosition(line);
			
		}
		
		
		// TextMessagePart.toJSON(); {text="text",bold=true}
		// return {extra=[{messagePart},{messagePart2}]}
		

		return "";
	}
	
	/**
	 * Get the next tag that occurs in the line
	 * @param line The line you want to get the first tag from
	 * @return The first tag occurring in the line or if none are there null
	 */
	private static int getNextTagPosition(String line) {
		char[] characters = line.toCharArray();
		// search the front of a tag
		for(int i=0; i<characters.length; i++) {
			if(characters[i] == FancyMessageFormatConverter.tagStart) {
				// search the end of the tag
				for(int j=i+1; j<characters.length; j++) {
					if(characters[j] == FancyMessageFormatConverter.tagEnd) {
						// make sure that there is something inside the tag, [] is nothing
						if(j-i > 2) {
							String tagText = line.substring(i+1, j-1);
							// Check if it is a valid tag
							if(bracketTagList.get(tagText.toLowerCase()) != null) {
								return i;
							}
						}
					}
				}
			}
		}
		return -1;
	}
	
	
	private String getSpecialTag(String line) {
		String trimmedLine = line.trim();
		
		for(ClickType clickType : ClickType.values()) {
			for(String tag : clickType.getTags()) {
				if(trimmedLine.startsWith(clickType.jsonKey)) {
					
				}
			}
		}
		
		
		
		
		return "";		
	}


	private class TextMessagePart {
		String text = "";
		Color color = Color.WHITE;
		
		Set<FormatType> formatTypes;
		
		public TextMessagePart(String text) {
			formatTypes = new HashSet<FormatType>();
			this.text = text;
		}
		
		public void addFormatting(FormatType... types) {
			for(FormatType type : types) {
				formatTypes.add(type);
			}
		}
		
		/**
		 * C
		 */
		String toJSON() {
			return "";
			// TODO
		}
	}
	
	private class InteractiveMessagePart extends TextMessagePart {
		ClickType clickType = null;		
		String clickContent = "";

		List<TextMessagePart> hover = new ArrayList<TextMessagePart>();
		
		public InteractiveMessagePart(String text) {
			super(text);
		}
		
		@Override
		String toJSON() {
			// TODO
			// Creates a single JSON array, which can be added in the "extra" section
			// of our main array
			return "";
		}
	}
	
	
	// --------------------------------- Tags ---------------------------------

	interface Tag {
		String[] getTags();
	}

	interface SimpleMessageTag extends Tag {
		/** The character that defines upcoming formatting in a native (non-JSON) message. */
		char getNativeFormattingCode();
	}

	interface InteractiveMessageTag extends Tag {
		String getJsonKey();
	}
	
	static enum Color implements SimpleMessageTag {
		WHITE('f'),
		BLACK('0'),
		BLUE('9'),
		DARK_BLUE('1'),
		GREEN('a'),
		DARK_GREEN('2'),
		AQUA('b'),
		DARK_AQUA('3'),
		RED('c'),
		DARK_RED('4'),
		LIGHT_PURPLE('d'),
		DARK_PURPLE('5'),
		YELLOW('e'),
		GOLD('6'),
		GRAY('7'),
		DARK_GRAY('8');
		
		final char bytecode;
		final String jsonKey;
		final String[] tags;
		
		private Color(char bytecode) {
			this.bytecode = bytecode;
			this.jsonKey = this.name().toLowerCase();
			this.tags = new String[] {this.name().toLowerCase()};
		}

		@Override
		public String[] getTags() {
			return tags;
		}
		
		@Override
		public char getNativeFormattingCode() {
			return bytecode;
		}

	}
	

	static enum FormatType implements SimpleMessageTag {
		BOLD('l', "bold", "b", "bold"),
		ITALIC('o', "italic", "i", "italic"),
		UNDERLINE('n', "underlined", "u", "underline"),
		STRIKETHROUGH('s', "strikethrough", "s", "strikethrough"),
		OBFUSCATE('k', "obfuscated", "obfuscate");

		final char bytecode;
		final String jsonKey;
		final String[] tags;

		FormatType(char bytecode, String jsonKey, String... tags) {
			this.bytecode = bytecode;
			this.jsonKey = jsonKey;
			this.tags = tags;
		}

		@Override
		public String[] getTags() {
			return tags;
		}

		@Override
		public char getNativeFormattingCode() {
			return bytecode;
		}
	}
	
	/**
	 * Types of clicking
	 */
	static enum ClickType implements InteractiveMessageTag {
		LINK("open_url", "link: "),
		COMMAND("run_command", "command: "),
		SUGGEST("suggest_command", "suggest: ");

		final String jsonKey;
		final String[] tags;

		private ClickType(String jsonKey, String... tags) {
			this.jsonKey = jsonKey;
			this.tags = tags;
		}

		@Override
		public String[] getTags() {
			return tags;
		}

		@Override
		public String getJsonKey() {
			return jsonKey;
		}
	}

	static enum HoverType implements InteractiveMessageTag {
		HOVER;

		@Override
		public String[] getTags() {
			return new String[] {"hover: "};
		}

		@Override
		public String getJsonKey() {
			return "hoverEvent";
		}
	}
	
	
}