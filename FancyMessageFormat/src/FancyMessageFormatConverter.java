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
	public static final char tagClose = '/';
	public static FancyMessageFormatConverter instance = null;
	
	/** The special character that prefixes all chat formatting codes. */
	final char SIMPLE_FORMAT_CHAR = '\u00A7';

	/** Lookup table for all continuous tags (marked by []) */
	public HashMap<String, SimpleMessageTag> bracketTagList;

	// private constructor + static getInstance() method to make this class singleton
	private FancyMessageFormatConverter() {
		bracketTagList = new HashMap<String, SimpleMessageTag>();
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
			format.setClosing(true);
			for(String tag : format.getTags()) {
				bracketTagList.put(FancyMessageFormatConverter.tagClose + tag, format);
			}
		}
	}	
	public static FancyMessageFormatConverter getInstance() {
		if(instance == null) {
			instance = new FancyMessageFormatConverter();
		}
		return instance;
	}

	// Wrapper to allow one string as parameter
	public String convertToJSON(final String line) {
		return convertToJSON(Arrays.asList(line));
	}
	
	// TODO [esc] support for full function
	public String convertToJSON(final List<String> inputLines) {
		List<InteractiveMessagePart> message = new ArrayList<InteractiveMessagePart>();
		
		// Split lines at line breaks
		// In the end we will have a list with one line per element
		ArrayList<String> lines = new ArrayList<>();
		for(String line : inputLines) {
			lines.addAll(Arrays.asList(line.split("\\r?\\n")));
		}

		// Remove any special lines at the start (a real text line should be first)
		while(!lines.isEmpty() && hasSpecialTag(lines.get(0))) {
			lines.remove(0);
		}
		
		Color currentColor = null;
		Set<FormatType> currentFormatting = new HashSet<FormatType>();
		while(!lines.isEmpty()) {
			// parts that have different formatting/text but the same hover/click effects
			List<InteractiveMessagePart> messageParts = new ArrayList<InteractiveMessagePart>();
			String line = lines.get(0);
			// hover/click line
			if(hasSpecialTag(line)) {
				if(!messageParts.isEmpty()) {
					// TODO detect which hover/click action it is and apply it to all parts from the messageParts list
				} else {
					// remove special lines that appear before any text line is given
					lines.remove(0);
				}
			} 
			// text line
			else {
				// Split into pieces at places where formatting changes
				while(!line.isEmpty()) {
					int formatPosition = getNextTagPosition(line);
					String toAdd = null;
					boolean handleFormatTag = false;
					if(formatPosition == -1) {
						toAdd = line;
						line = "";

					} else {
						toAdd = line.substring(0, formatPosition-1);
						line = line.substring(formatPosition);			
						handleFormatTag = true;
					}
					if(!toAdd.isEmpty()) {
						InteractiveMessagePart part = new InteractiveMessagePart(toAdd);
						part.addFormatting(currentFormatting);
						part.color = currentColor;
						messageParts.add(part);						
					}
					if(handleFormatTag) {
						// Handle the formatting tag
						SimpleMessageTag tag = null;
						char[] characters = line.toCharArray();
						// Get the actual tag (search the start and retrieve from map)
						for(int i=0; i<characters.length; i++) {
							if(characters[i] == FancyMessageFormatConverter.tagEnd) {
								// make sure that there is something inside the tag, [] is nothing
								if(i >= 2) {
									String tagText = line.substring(1, i-1);
									// Check if it is a valid tag
									tag = bracketTagList.get(tagText.toLowerCase());
									if(line.length() > i) {
										line = line.substring(i+1);
									} else {
										line = "";
									}
								}
							}
						}
						if(tag instanceof Color) {
							currentColor = (Color)tag;
						} else if (tag instanceof FormatType) {
							FormatType formatType = (FormatType)tag;
							// Check if it is a starting or closing tag
							if(formatType.closing) {
								// TODO check if this fails because of different inner variable 'closing'
								currentFormatting.remove(formatType);
							} else {
								currentFormatting.add(formatType);
							}
						}
					}
				}				
			}				
			message.addAll(messageParts);			
		}
		
		
		// TODO Convert to JSON string and return it
		
		return "";
	}
	
	/**
	 * Get the next tag that occurs in the line
	 * @param line The line you want to get the first tag from
	 * @return The first tag occurring in the line or if none are there null
	 */
	private int getNextTagPosition(String line) {
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
	
	
	/**
	 * Check if the specified line is just text or a special formatting line
	 * @param line The line that should be checked
	 * @return true if the line is a special line, false if it is just text
	 */
	private boolean hasSpecialTag(String line) {
		// Remove leading whitespace
		String trimmedLine = line.replaceAll("^\\s+", "");
		
		for(ClickType clickType : ClickType.values()) {
			for(String tag : clickType.getTags()) {
				if(trimmedLine.startsWith(tag)) {
					return true;
				}
			}
		}
		return false;		
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
		public void addFormatting(Collection<FormatType> types) {
			formatTypes.addAll(types);
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
		boolean closing = false;

		FormatType(char bytecode, String jsonKey, String... tags) {
			this.bytecode = bytecode;
			this.jsonKey = jsonKey;
			this.tags = tags;
		}

		@Override
		public String[] getTags() {
			return tags;
		}
		
		public void setClosing(boolean closing) {
			this.closing = closing;
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