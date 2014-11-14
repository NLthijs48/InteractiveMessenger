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
 * @author NLThijs48
 * @author Tobias aka Phoenix | http://www.phoenix-iv.de
 */
public class FancyMessageFormatConverter {

	private static final char TAG_BEFORE = '[';
	private static final char TAG_AFTER  = ']';
	private static final char END_TAG_INDICATOR = '/';

	/** The special character that prefixes all basic chat formatting codes. */
	private static final char SIMPLE_FORMAT_CHAR = '\u00A7';

	/** Lookup table for all continuous tags (marked by []) */
	private static final HashMap<String, Tag> BRACKET_TAG_LIST  = new HashMap<String, Tag>();

	/** Lookup table for all interactive tags */
	private static final HashMap<String, Tag> INTERACTIVE_TAG_LIST  = new HashMap<String, Tag>();

	static {
		// Enlist all possible tags
		// (They go into a HashMap for lookup purposes)
		cacheTags(Color.class, BRACKET_TAG_LIST);
		cacheTags(FormatType.class, BRACKET_TAG_LIST);
		cacheTags(FormatCloseTag.class, BRACKET_TAG_LIST);
		// Interactive tags
		cacheTags(ClickType.class, BRACKET_TAG_LIST);
		cacheTags(HoverType.class, BRACKET_TAG_LIST);
	}


	/**
	 * Puts all constants in the given Tag class into the given lookup table.
	 */
	private static <T extends Tag> void cacheTags(Class<T> tags, HashMap<String, Tag> tagList) {
		for (Tag tag : tags.getEnumConstants()) {
			for(String key : tag.getTags()) {
				tagList.put(key, tag);
			}
		}
	}





	// TODO Maybe copy (parts of) the "quote" function of Jettison for JSON escaping?
	// http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.jettison/jettison/1.3.3/org/codehaus/jettison/json/JSONObject.java#945





	// ------------------------------------------------------------------------------------------
	// -------------------------------     Public / Interface     -------------------------------
	// ------------------------------------------------------------------------------------------


	// Wrapper to allow one string as parameter
	public String convertToJSON(final String line) {
		return convertToJSON(Arrays.asList(line));
	}


	public String convertToJSON(final Iterable<String> inputLines) {

		// Split lines at line breaks
		// In the end we will have a list with one line per element
		ArrayList<String> lines = new ArrayList<>();
		for(String line : inputLines) {
			lines.addAll(Arrays.asList(line.split("\\r?\\n")));
		}

		// Remove any special lines at the start (a real text line should be first)
		while(!lines.isEmpty() && isInteractiveTag(lines.get(0))) {
			lines.remove(0);
		}

		List<InteractiveMessagePart> message = new ArrayList<InteractiveMessagePart>();
		message.add(new InteractiveMessagePart());

		Color currentColor = null;
		Set<FormatType> currentFormatting = new HashSet<FormatType>();

		for (String line : lines) {
			InteractiveMessagePart messagePart;
			TaggedContent interactive = getInteractiveTag(line);
			if (interactive != null) {
				messagePart = message.get(message.size() - 1);
				// TODO Apply tag
			} else {
				// New text line
				messagePart = new InteractiveMessagePart();
				message.add(messagePart);
				// TODO Search for inline tags and apply them
			}
		}


//		while(!lines.isEmpty()) {
//			// parts that have different formatting/text but the same hover/click effects
//			List<InteractiveMessagePart> messageParts = new ArrayList<InteractiveMessagePart>();
//			String line = lines.get(0);
//			// hover/click line
//			if(hasSpecialTag(line)) {
//				if(!messageParts.isEmpty()) {
//					// TODO detect which hover/click action it is and apply it to all parts from the messageParts list
//				} else {
//					// remove special lines that appear before any text line is given
//					lines.remove(0);
//				}
//			} 
//			// text line
//			else {
//				// Split into pieces at places where formatting changes
//				while(!line.isEmpty()) {
//					int formatPosition = getNextTagPosition(line);
//					String toAdd = null;
//					boolean handleFormatTag = false;
//					if(formatPosition == -1) {
//						toAdd = line;
//						line = "";
//
//					} else {
//						toAdd = line.substring(0, formatPosition-1);
//						line = line.substring(formatPosition);			
//						handleFormatTag = true;
//					}
//					if(!toAdd.isEmpty()) {
//						InteractiveMessagePart part = new InteractiveMessagePart(toAdd);
//						part.addFormatting(currentFormatting);
//						part.color = currentColor;
//						messageParts.add(part);						
//					}
//					if(handleFormatTag) {
//						// Handle the formatting tag
//						ContinuousTag tag = null;
//						char[] characters = line.toCharArray();
//						// Get the actual tag (search the start and retrieve from map)
//						for(int i=0; i<characters.length; i++) {
//							if(characters[i] == FancyMessageFormatConverter.tagEnd) {
//								// make sure that there is something inside the tag, [] is nothing
//								if(i >= 2) {
//									String tagText = line.substring(1, i-1);
//									// Check if it is a valid tag
//									tag = bracketTagList.get(tagText.toLowerCase());
//									if(line.length() > i) {
//										line = line.substring(i+1);
//									} else {
//										line = "";
//									}
//								}
//							}
//						}
//						if(tag instanceof Color) {
//							currentColor = (Color)tag;
//						} else if (tag instanceof FormatType) {
//							FormatType formatType = (FormatType)tag;
//							// Check if it is a starting or closing tag
//							if(formatType.closing) {
//								// TODO check if this fails because of different inner variable 'closing'
//								currentFormatting.remove(formatType);
//							} else {
//								currentFormatting.add(formatType);
//							}
//						}
//					}
//				}				
//			}				
//			message.addAll(messageParts);			
//		}
		
		
		
		// TODO Convert to JSON string and return it
		return "";
	}





	// ------------------------------------------------------------------------------------------
	// -------------------------------     Private functions      -------------------------------
	// ------------------------------------------------------------------------------------------


//	/**
//	 * Get the next tag that occurs in the line
//	 * @param line The line you want to get the first tag from
//	 * @return The first tag occurring in the line or if none are there null
//	 */
//	private int getNextTagPosition(String line) {
//		char[] characters = line.toCharArray();
//		// search the front of a tag
//		for(int i=0; i<characters.length; i++) {
//			if(characters[i] == FancyMessageFormatConverter.TAG_BEFORE) {
//				// search the end of the tag
//				for(int j=i+1; j<characters.length; j++) {
//					if(characters[j] == FancyMessageFormatConverter.TAG_AFTER) {
//						// make sure that there is something inside the tag, [] is nothing
//						if(j-i > 2) {
//							String tagText = line.substring(i+1, j-1);
//							// Check if it is a valid tag
//							if(BRACKET_TAG_LIST.get(tagText.toLowerCase()) != null) {
//								return i;
//							}
//						}
//					}
//				}
//			}
//		}
//		return -1;
//	}


//	/**
//	 * Check if the specified line is just text or a special formatting line
//	 * @param line The line that should be checked
//	 * @return true if the line is a special line, false if it is just text
//	 */
//	private boolean hasSpecialTag(String line) {
//		// Remove leading whitespace
//		String trimmedLine = line.replaceAll("^\\s+", "");
//		
//		for(ClickType clickType : ClickType.values()) { // TODO Misses hover
//			for(String tag : clickType.getTags()) {
//				if(trimmedLine.startsWith(tag)) { // TODO Case-insensitive
//					return true;
//				}
//			}
//		}
//		return false;		
//	}


	/**
	 * Searches and returns the first continuous tag found in the given String.
	 * @return The tag (plus its preceding and subsequent content) if found.
	 *         Null if nothing is found.
	 */
	private static TaggedContent getNextTag(String line) {
		for (int startIndex = 0; startIndex < line.length(); startIndex++) {
			int start = line.indexOf(TAG_BEFORE, startIndex);
			if (start != -1) {
				int end = line.indexOf(TAG_AFTER, start);
				if (end != -1) {
					String inBetween = line.substring(start+1, end).toLowerCase();
					if (BRACKET_TAG_LIST.containsKey(inBetween)) {
						String previousContent = line.substring(0, start);
						Tag tag = BRACKET_TAG_LIST.get(inBetween);
						String subsequentContent = line.substring(end + 1);
						return new TaggedContent(previousContent, tag, subsequentContent);
					} else {
						startIndex = start + 1;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		return null;
	}


	/**
	 * If the given line defines an interactive property (e.g. "hover: myText")
	 * the tag / property will be returned. Otherwise null is returned.
	 */
	private static TaggedContent getInteractiveTag(String line) {
		for (int index = 0; index < line.length(); index++) {
			char c = line.charAt(index);
			if (c == ' ' || c == '\t') {
				// Ignore (Skip spacing)
			} else {
				int end = line.indexOf(": ", index);
				String inBetween = line.substring(index, end).toLowerCase();
				if (INTERACTIVE_TAG_LIST.containsKey(inBetween)) {
					Tag tag = INTERACTIVE_TAG_LIST.get(inBetween);
					String subsequentContent = line.substring(end + 2);
					return new TaggedContent(null, tag, subsequentContent);
				}
			}
		}
		return null;
	}


	private static boolean isInteractiveTag(String line) {
		return getInteractiveTag(line) != null;
	}





	// ------------------------------------------------------------------------------------------
	// -------------------------------       Helper classes       -------------------------------
	// ------------------------------------------------------------------------------------------


	private static class TaggedContent {
		final String precedingContent;
		final Tag tag;
		final String subsequentContent;

		public TaggedContent(String pre, Tag tag, String sub) {
			this.precedingContent = pre;
			this.tag = tag;
			this.subsequentContent = sub;
		}

		public boolean noPreContent() {
			return precedingContent.isEmpty();
		}
	}


	/**
	 * Holds a string with basic (non-interactive) formatting.
	 */
	private static class TextMessagePart {
		String text = "";
		Color color = Color.WHITE;

		Set<FormatType> formatTypes = new HashSet<FormatType>();

		/**
		 * Converts this message into a MC-JSON array.
		 */
		String toJSON() {
			// TODO Create JSON array
			return "";
		}
	}


	/**
	 * Holds a string with interactive formatting.
	 */
	private static class InteractiveMessagePart extends TextMessagePart {
		ClickType clickType = null;		
		String clickContent = "";
		ArrayList<TextMessagePart> hover = new ArrayList<TextMessagePart>();

		@Override
		String toJSON() {
			// TODO Create JSON array
			return "";
		}
	}



	// --------------------------------------- Tags ---------------------------------------


	interface Tag {
		String[] getTags();
	}


	/**
	 * Indicates formatting that is applied until explicitly stopped.
	 * Can also be used in simple Minecraft messages (Non-JSON).
	 */
	interface ContinuousTag extends Tag {
		/**
		 * The character that defines upcoming formatting in a native (non-JSON) Minecraft message.
		 */
		char getNativeFormattingCode();
	}


	/**
	 * Indicates formatting that allows cursor interaction. Requires the
	 * Minecraft JSON / tellraw format.
	 */
	interface InteractiveMessageTag extends Tag {
		String getJsonKey();
	}


	static enum Color implements ContinuousTag {
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


	static enum FormatType implements ContinuousTag {
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


	static enum FormatCloseTag implements Tag {
		BOLD_END(FormatType.BOLD),
		ITALIC_END(FormatType.ITALIC),
		UNDERLINE_END(FormatType.UNDERLINE),
		STRIKETHROUGH_END(FormatType.STRIKETHROUGH),
		OBFUSCATE_END(FormatType.OBFUSCATE);

		/** Formatting that is stopped at this point */
		final FormatType closes;
		final String[] tags;

		private FormatCloseTag(FormatType openingTag) {
			this.closes = openingTag;

			// Auto-generate close tags
			tags = new String[closes.tags.length];
			for (int i = 0; i < tags.length; i++) {
				tags[i] = END_TAG_INDICATOR + closes.tags[i];
			}
		}

		@Override
		public String[] getTags() {
			return tags;
		}

	}


	static enum ControlTag implements Tag {
		BREAK("break"),
		ESCAPE("esc");

		final String[] tags;

		private ControlTag(String... tags) {
			this.tags = tags;
		}

		@Override
		public String[] getTags() {
			return tags;
		}
		
	}


	/**
	 * Types of clicking
	 */
	static enum ClickType implements InteractiveMessageTag {
		LINK("open_url", "link"),
		COMMAND("run_command", "command"),
		SUGGEST("suggest_command", "suggest");

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
			return new String[] {"hover"};
		}

		@Override
		public String getJsonKey() {
			return "hoverEvent";
		}
	}

}