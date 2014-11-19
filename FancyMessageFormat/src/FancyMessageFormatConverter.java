import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
		cacheTags(ClickType.class, INTERACTIVE_TAG_LIST);
		cacheTags(HoverType.class, INTERACTIVE_TAG_LIST);
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

		LinkedList<InteractiveMessagePart> message = new LinkedList<InteractiveMessagePart>();

		Color currentColor = null;
		Set<FormatType> currentFormatting = new HashSet<FormatType>();

		for(String line : lines) {
			TaggedContent interactiveTag = getInteractiveTag(line);
			// hover / click line
			if(interactiveTag != null) {
				// TODO: Handle control tags
				InteractiveMessagePart interactivePart = message.getLast();
				if(interactiveTag.tag instanceof ClickType) {
					interactivePart.clickType = (ClickType)interactiveTag.tag;
					interactivePart.clickContent = interactiveTag.subsequentContent;
				} else if (interactiveTag.tag instanceof HoverType) {
					// Generate a list with TextMessagePart's to apply to all hovers 
					LinkedList<TextMessagePart> hoverParts = new LinkedList<TextMessagePart>();
					// Split into pieces at places where formatting changes
					while(!line.isEmpty()) {
						String toAdd = null;
						boolean handleFormatTag = false;
						TaggedContent nextTag = getNextTag(line);
						if(nextTag == null) {
							toAdd = line;
							line = "";
						} else {
							toAdd = nextTag.precedingContent;
							line = nextTag.subsequentContent;
							handleFormatTag = true;
						}
						// Add a text part with the correct formatting
						if(!toAdd.isEmpty()) {
							TextMessagePart part = new TextMessagePart();
							part.text = toAdd;
							part.formatTypes = new HashSet<FormatType>(currentFormatting);
							part.color = currentColor;
							hoverParts.add(part);
						}
						// Handle the change in formatting if a Tag has been detected (this needs to be after creating the InteractiveMessagePart)
						if(handleFormatTag) {
							// Handle the formatting tag
							if(nextTag.tag instanceof Color) {
								currentColor = (Color)nextTag.tag;
							} else if (nextTag.tag instanceof FormatType) {
								currentFormatting.add((FormatType)nextTag.tag);
							} else if(nextTag.tag instanceof FormatCloseTag) {
								currentFormatting.remove(((FormatCloseTag)nextTag.tag).closes);
							}
							// TODO: Handle control tags
						}
					}

					// Apply the hover text to all messageParts
					if(!hoverParts.isEmpty()) {
						HoverType tagType = (HoverType) interactiveTag.tag;
						if(interactivePart.hoverType != null && interactivePart.hoverType == tagType) {
							// TODO: Add newline, another hover line in the source should start a new line
							interactivePart.hoverContent.addAll(hoverParts);
						}
						interactivePart.hoverContent = hoverParts;
						interactivePart.hoverType = (HoverType)interactiveTag.tag;
					}
				}
			} 
			// text line
			else {
				InteractiveMessagePart interactivePart = new InteractiveMessagePart();
				message.add(interactivePart);

				// Split into pieces at places where formatting changes
				while(!line.isEmpty()) {
					String toAdd = null;
					boolean handleFormatTag = false;
					TaggedContent nextTag = getNextTag(line);
					if(nextTag == null) {
						toAdd = line;
						line = "";
					} else {
						toAdd = nextTag.precedingContent;
						line = nextTag.subsequentContent;
						handleFormatTag = true;
					}
					// Add a text part with the correct formatting
					if(!toAdd.isEmpty()) {
						TextMessagePart part = new TextMessagePart();
						part.text = toAdd;
						part.formatTypes = new HashSet<FormatType>(currentFormatting);
						part.color = currentColor;
						interactivePart.content.add(part);
					}
					// Handle the change in formatting if a Tag has been detected (this needs to be after creating the InteractiveMessagePart)
					if(handleFormatTag) {
						// Handle the formatting tag
						if(nextTag.tag instanceof Color) {
							currentColor = (Color)nextTag.tag;
						} else if (nextTag.tag instanceof FormatType) {
							currentFormatting.add((FormatType)nextTag.tag);
						} else if(nextTag.tag instanceof FormatCloseTag) {
							currentFormatting.remove(((FormatCloseTag)nextTag.tag).closes);
						}
						// TODO: Handle control tags
					}
				}
			}
		}


		// TODO Convert to JSON string and return it
		return "";
	}





	// ------------------------------------------------------------------------------------------
	// -------------------------------     Private functions      -------------------------------
	// ------------------------------------------------------------------------------------------


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
				if (end != -1) {
					String inBetween = line.substring(index, end).toLowerCase();
					if (INTERACTIVE_TAG_LIST.containsKey(inBetween)) {
						Tag tag = INTERACTIVE_TAG_LIST.get(inBetween);
						String subsequentContent = line.substring(end + 2);
						return new TaggedContent(null, tag, subsequentContent);
					}
				}
				return null;
			}
		}
		return null;
	}


	private static boolean isInteractiveTag(String line) {
		return getInteractiveTag(line) != null;
	}


	// TODO Maybe copy (parts of) the "quote" function of Jettison for JSON escaping?
	// http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.jettison/jettison/1.3.3/org/codehaus/jettison/json/JSONObject.java#945






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


		String toJSON() {
			// TODO Create JSON array
			return "";
		}
	}


	/**
	 * Holds a string with interactive formatting.
	 */
	private static class InteractiveMessagePart {

		LinkedList<TextMessagePart> content = new LinkedList<TextMessagePart>();

		// Click
		ClickType clickType    = null;
		String    clickContent = "";

		// Hover
		HoverType hoverType = null;
		LinkedList<TextMessagePart> hoverContent = new LinkedList<TextMessagePart>();


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
