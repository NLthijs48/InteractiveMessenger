import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
		cacheTags(BRACKET_TAG_LIST, Color.class);
		cacheTags(BRACKET_TAG_LIST, FormatType.class);
		cacheTags(BRACKET_TAG_LIST, FormatCloseTag.class);
		cacheTags(BRACKET_TAG_LIST, ControlTag.class);
		// Interactive tags
		cacheTags(INTERACTIVE_TAG_LIST, ClickType.class);
		cacheTags(INTERACTIVE_TAG_LIST, HoverType.class);
	}


	/**
	 * Puts all constants in the given Tag class into the given lookup table.
	 */
	private static <T extends Tag> void cacheTags(HashMap<String, Tag> tagList, Class<T> tags) {
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
	public static String convertToJSON(final String line) {
		return convertToJSON(Arrays.asList(line));
	}


	public static String convertToJSON(final Iterable<String> inputLines) {
		// Split lines at line breaks
		// In the end we will have a list with one line per element
		ArrayList<String> lines = new ArrayList<>();
		for (String line : inputLines) {
			lines.addAll(Arrays.asList(line.split("\\r?\\n")));
		}

		// Remove any special lines at the start (a real text line should be first)
		while (!lines.isEmpty() && isTaggedInteractive(lines.get(0))) {
			lines.remove(0);
		}

		LinkedList<InteractiveMessagePart> message = parse(lines);
		StringBuilder sb = new StringBuilder();
		if (message.size() == 1) {
			sb.append(message.getFirst().toJSON());
		} else {
			sb.append("{text=\"\",extra:[");
			for (InteractiveMessagePart messagePart : message) {
				sb.append(messagePart.toJSON());
				sb.append(',');
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("]}");
		}


		return sb.toString();
	}





	// ------------------------------------------------------------------------------------------
	// -------------------------------     Private functions      -------------------------------
	// ------------------------------------------------------------------------------------------


	private static LinkedList<InteractiveMessagePart> parse(final Iterable<String> inputLines) {
		LinkedList<InteractiveMessagePart> message = new LinkedList<InteractiveMessagePart>();

		Color currentColor = Color.WHITE;
		Set<FormatType> currentFormatting = new HashSet<FormatType>();

		lineLoop: for (String line : inputLines) {
			InteractiveMessagePart messagePart;
			TaggedContent interactiveTag = getInteractiveTag(line);
			boolean isTextLine = interactiveTag == null;
			boolean isHoverLine = false;

			if (isTextLine) {
				messagePart = new InteractiveMessagePart();
				message.add(messagePart);
			}
			else /* if Interactive formatting */ {
				messagePart = message.getLast();
				Tag tag = interactiveTag.tag;
				if (tag instanceof ClickType) {
					messagePart.clickType = (ClickType) interactiveTag.tag;
					messagePart.clickContent = interactiveTag.subsequentContent;
				} else if (tag instanceof HoverType) {
					line = interactiveTag.subsequentContent;
					isHoverLine = true;
					if (messagePart.hoverType != tag) {
						// Hover type changed
						messagePart.hoverContent = new LinkedList<TextMessagePart>();
						messagePart.hoverType = (HoverType) tag;
					}
					// Add hover content below
				}
			}

			if (isTextLine || isHoverLine) {
				// Parse inline tags

				Color currentLineColor = currentColor;
				Set<FormatType> currentLineFormatting = currentFormatting;
				LinkedList<TextMessagePart> targetList = messagePart.content;
				boolean parseBreak = true;
				if (isHoverLine) {
					// Reset - use own
					currentLineColor = Color.WHITE;
					currentLineFormatting = new HashSet<FormatType>();
					targetList = messagePart.hoverContent;
					parseBreak = false;

					// Add line break after previous hover line
					if (!targetList.isEmpty()) {
						targetList.getLast().text += '\n';
					}
				}

				// Split into pieces at places where formatting changes
				while (!line.isEmpty()) {
					String textToAdd = null;
					TaggedContent nextTag = getNextTag(line, parseBreak);
					boolean tagged = nextTag != null;

					if (!tagged) {
						textToAdd = line;
						line = "";
					} else {
						textToAdd = nextTag.precedingContent;
						line = nextTag.subsequentContent;
					}

					if (!textToAdd.isEmpty()) {
						// Add a text part with the correct formatting
						TextMessagePart part = new TextMessagePart();
						part.text = textToAdd;
						part.formatTypes = new HashSet<FormatType>(currentLineFormatting);
						part.color = currentLineColor;
						targetList.add(part);
					}

					// Handle the change in formatting if a Tag has been detected (this needs to be after creating the InteractiveMessagePart)
					if (tagged) {
						// Handle the formatting tag
						Tag tag = nextTag.tag;
						if (tag instanceof Color) {
							currentLineColor = (Color) tag;
						} else if (tag instanceof FormatType) {
							currentLineFormatting.add((FormatType) tag);
						} else if (tag instanceof FormatCloseTag) {
							currentLineFormatting.remove(((FormatCloseTag) tag).closes);
						} else if (tag == ControlTag.BREAK) {
							targetList.getLast().text += '\n';
							continue lineLoop;
						}
					}
				}

				if (!isHoverLine) {
					// Adapt global attributes
					currentColor = currentLineColor;
				}
			}
		}

		return message;
	}


	/**
	 * Searches and returns the first continuous tag found in the given String.
	 * @return The tag (plus its preceding and subsequent content) if found.
	 *         Null if nothing is found.
	 */
	private static TaggedContent getNextTag(String line, boolean parseBreak) {
		for (int startIndex = 0; startIndex < line.length(); startIndex++) {
			int start = line.indexOf(TAG_BEFORE, startIndex);
			if (start != -1) {
				int end = line.indexOf(TAG_AFTER, start);
				if (end != -1) {
					String inBetween = line.substring(start+1, end).toLowerCase();
					if (BRACKET_TAG_LIST.containsKey(inBetween)) {
						Tag tag = BRACKET_TAG_LIST.get(inBetween);
						if (tag == ControlTag.ESCAPE) {
							// Ignore next char
							line = line.substring(0, start) + line.substring(end + 1);
							startIndex = start;
						} else if (!parseBreak && tag == ControlTag.ESCAPE) {
							// Ignore break
							startIndex = end + 1;
						} else {
							String previousContent = line.substring(0, start);
							String subsequentContent = line.substring(end + 1);
							return new TaggedContent(previousContent, tag, subsequentContent);
						}
					} else {
						startIndex = start;
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


	private static boolean isTaggedInteractive(String line) {
		return getInteractiveTag(line) != null;
	}


	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places.
	 * @param string A String
	 * @return  A String correctly formatted for insertion in a JSON text.
	 */
	/*
	 * Copyright (c) 2002 JSON.org
	 * Licensed under the Apache License, Version 2.0
	 */
	private static String quoteStringJson(String string) {
		if (string == null || string.length() == 0) {
			return "\"\"";
		}

		char         c = 0;
		int          i;
		int          len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);
		String       t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			c = string.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				sb.append('\\');
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if (c < ' ') {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('"');
		return sb.toString();
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
	}


	/**
	 * Holds a string with basic (non-interactive) formatting.
	 */
	private static class TextMessagePart {
		String text = "";
		Color color = Color.WHITE;
		Set<FormatType> formatTypes = new HashSet<FormatType>();


		String toJSON() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append("text:").append(quoteStringJson(text));
			if (color != Color.WHITE) {
				sb.append(",color:").append(color.jsonValue);
			}
			for (FormatType formatting : formatTypes) {
				sb.append(',');
				sb.append(formatting.jsonKey).append(':');
				sb.append("true");
			}
			sb.append('}');
			return sb.toString();
		}

		boolean hasFormatting() {
			return !(color == Color.WHITE && formatTypes.isEmpty());
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
		LinkedList<TextMessagePart> hoverContent = null;


		String toJSON() {
			StringBuilder sb = new StringBuilder();
			if (content.size() == 1) {
				// Add attributes to TextMessagePart object
				sb.append(content.getFirst().toJSON());
				sb.deleteCharAt(sb.length() - 1);
			} else {
				sb.append('{');
				sb.append("text=\"\",extra:[");
				for (TextMessagePart textPart : content) {
					sb.append(textPart.toJSON());
					sb.append(',');
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(']');
			}
			if (clickType != null) {
				sb.append(',');
				sb.append("clickEvent:{");
					sb.append("action:").append(clickType.getJsonKey()).append(',');
					sb.append("value:").append(quoteStringJson(clickContent));
				sb.append('}');
			}
			if (hoverType != null) {
				sb.append(',');
				sb.append("hoverEvent:{");
					sb.append("action:").append(hoverType.getJsonKey()).append(',');
					sb.append("value:");
					if (hoverContent.size() == 1) {
						TextMessagePart hoverPart = hoverContent.getFirst();
						if (hoverPart.hasFormatting()) {
							sb.append(hoverPart.toJSON());
						} else {
							sb.append(quoteStringJson(hoverPart.text));
						}
					} else {
						sb.append('[');
						for (TextMessagePart hoverPart : hoverContent) {
							sb.append(hoverPart.toJSON());
							sb.append(',');
						}
						sb.deleteCharAt(sb.length() - 1);
						sb.append(']');
					}
				sb.append('}');
			}
			sb.append('}');
			return sb.toString();
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
		final String jsonValue;
		final String[] tags;

		private Color(char bytecode) {
			this.bytecode = bytecode;
			this.jsonValue = this.name().toLowerCase();
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
		private final String[] tags;

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

		private final String[] tags;

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

		private final String jsonKey;
		private final String[] tags;

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
			return "show_text";
		}
	}

}
