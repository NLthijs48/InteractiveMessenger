package me.wiefferink.interactivemessenger.parsers;

import me.wiefferink.interactivemessenger.generators.ConsoleGenerator;
import me.wiefferink.interactivemessenger.message.InteractiveMessage;
import me.wiefferink.interactivemessenger.message.InteractiveMessagePart;
import me.wiefferink.interactivemessenger.message.TextMessagePart;
import me.wiefferink.interactivemessenger.message.enums.*;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlParser {

	public static final char ESCAPE_CHAR = '\\';
	public static final char SIMPLE_FORMAT_RESET_CHAR = 'r';
    public static final Pattern tagPattern = Pattern.compile("(\\[[/a-zA-Z1-9_]+?\\])|([&" + Pattern.quote(ChatColor.COLOR_CHAR + "") + "][0-9a-zA-Z])|(\\\\n)");

	// Lookup table for all continuous enums ([<tag>])
	private static final HashMap<String, Object> BRACKET_TAGS = new HashMap<String, Object>() {{
		// Colors
		cacheTags(this, Color.class);
		put("grey", Color.GRAY);
		put("darkgrey", Color.DARK_GRAY);
		put("dark_grey", Color.DARK_GRAY);

		// Formatting
		cacheTags(this, Format.class);
		for(Format tag : Format.values()) {
			put((tag.name().charAt(0)+"").toLowerCase(), tag);
		}
		put("strike", Format.STRIKETHROUGH);

		// Control
		cacheTags(this, Control.class);
	}};

	// Lookup table for all interactive enums (<tag>:)
	private static final HashMap<String, Object> INTERACTIVE_TAGS = new HashMap<String, Object>() {{
		cacheTags(this, Click.class);
		cacheTags(this, Hover.class);
	}};

	// Lookup table for all native enums (&<tag> and §<tag>)
	private static final HashMap<String, Object> NATIVE_TAGS = new HashMap<String, Object>() {{
		put("0", Color.BLACK);
		put("1", Color.DARK_BLUE);
		put("2", Color.DARK_GREEN);
		put("3", Color.DARK_AQUA);
		put("4", Color.DARK_RED);
		put("5", Color.DARK_PURPLE);
		put("6", Color.GOLD);
		put("7", Color.GRAY);
		put("8", Color.DARK_GRAY);
		put("9", Color.BLUE);
		put("a", Color.GREEN);
		put("b", Color.AQUA);
		put("c", Color.RED);
		put("d", Color.LIGHT_PURPLE);
		put("e", Color.YELLOW);
		put("f", Color.WHITE);

		put("l", Format.BOLD);
		put("m", Format.STRIKETHROUGH);
		put("o", Format.ITALIC);
		put("n", Format.UNDERLINE);
		put("k", Format.OBFUSCATE);

		put("r", Control.RESET);
	}};

	/**
	 * Puts all values of the given Enum into the given lookup table.
	 */
	private static <T extends Enum> void cacheTags(HashMap<String, Object> tagList, Class<T> tags) {
		for(Enum tag : tags.getEnumConstants()) {
			tagList.put(tag.name().toLowerCase(), tag);
			tagList.put(tag.name().toLowerCase().replace("_", ""), tag);
		}
	}

	/**
	 * Parse input lines into an InteractiveMessage
	 * @param input          The input to parse
	 * @return InteractiveMessage representing the parsed input
	 */
	public static InteractiveMessage parse(List<String> input) {
		return parse(input, true);
	}

	/**
	 * Parse input lines into an InteractiveMessage
	 * @param input The input to parse
	 * @param doInteractives true to parse interactive enums (hover, click, etc.), false to skip them
	 * @return InteractiveMessage representing the parsed input
	 */
	public static InteractiveMessage parse(List<String> input, boolean doInteractives) {
		InteractiveMessage message = new InteractiveMessage();

		Color currentColor = null;
		Set<Format> currentFormatting = new HashSet<>();

		lineLoop:
		for(String line : input) {
			InteractiveMessagePart messagePart;
			TaggedContent interactiveTag = getInteractiveTag(line);
			boolean isTextLine = interactiveTag == null;
			if(!doInteractives && !isTextLine) {
				continue;
			}
			boolean isHoverLine = false;

			if(isTextLine) {
				messagePart = new InteractiveMessagePart();
				message.add(messagePart);
			} else /* if Interactive formatting */ {
				if(message.isEmpty()) {
					continue;
				}
				messagePart = message.getLast();
				Object tag = interactiveTag.tag;
				if(tag instanceof Click) {
					messagePart.onClick = (Click)interactiveTag.tag;
					messagePart.clickContent = interactiveTag.subsequentContent;
				} else if(tag instanceof Hover) {
					line = interactiveTag.subsequentContent;
					isHoverLine = true;
					if(messagePart.onHover != tag) {
						// Hover type changed
						messagePart.hoverContent = new LinkedList<>();
						messagePart.onHover = (Hover)tag;
					}
					// Add hover content below
				}
			}

			if(isTextLine || isHoverLine) {
				// Parse inline enums
				Color currentLineColor = currentColor;
				Set<Format> currentLineFormatting = currentFormatting;
				LinkedList<TextMessagePart> targetList = messagePart;
				if(isHoverLine) {
					// Reset - use own
					currentLineColor = null;
					currentLineFormatting = new HashSet<>();
					targetList = messagePart.hoverContent;

					// Add line break after previous hover line
					if(!targetList.isEmpty()) {
						targetList.getLast().text += '\n';
					}
				}

				// Split into pieces at places where formatting changes
				while(!line.isEmpty()) {
					String textToAdd;
                    TaggedContent nextTag = getNextTag(line);
                    boolean tagged = nextTag != null;

					if(!tagged) {
						textToAdd = line;
						line = "";
					} else {
						textToAdd = nextTag.precedingContent;
						line = nextTag.subsequentContent;
					}

					// Add a text part with the correct formatting
					if(!textToAdd.isEmpty()) {
						TextMessagePart part = new TextMessagePart();
						part.text = unescape(textToAdd);
						part.formatting = new HashSet<>(currentLineFormatting);
						part.color = currentLineColor;
						targetList.add(part);
					}

					// Handle the change in formatting if a Tag has been detected (this needs to be after creating the InteractiveMessagePart)
					if(tagged) {
						// Handle the formatting tag
						Object tag = nextTag.tag;
						if(tag instanceof Color) {
							currentLineColor = (Color)tag;
						} else if(tag instanceof Format) {
							if(nextTag.closing) {
								currentLineFormatting.remove(tag);
							} else {
								currentLineFormatting.add((Format)tag);
							}
						} else if(tag == Control.BREAK) {
							if(isHoverLine && !targetList.isEmpty()) {
								targetList.getLast().text += "\n";
							} else {
								messagePart.newline = true;
								currentLineFormatting.clear();
                                // TODO: Remove this and support multiple line breaks and even content after a break?
                                continue lineLoop;
							}
						} else if(tag == Control.RESET) {
							currentLineFormatting.clear();
							currentLineColor = Color.WHITE;
						}
					}
				}

				if(!isHoverLine) {
					// Adapt global attributes
					currentColor = currentLineColor;
				}
			}
		}

		// Cleanup empty parts (some parts only affected the formatting of next parts, but do not have actual content)
		Iterator<InteractiveMessagePart> it = message.iterator();
		while(it.hasNext()) {
			InteractiveMessagePart check = it.next();
			if(check.size() == 0 && !check.newline) {
				it.remove();
			}
		}
		return message;
	}

	/**
	 * Insert a message at the specified position
	 * @param message The current message
	 * @param insert  The message to insert
	 * @param line    The line number to insert at
	 * @param start   The start of the variable to replace
	 * @param end     The end of the variable to replace
	 */
	public static void insertMessage(List<String> message, List<String> insert, int line, int start, int end) {
		if(insert == null || line < 0 || line >= message.size() || start < 0 || end < 0) {
			return;
		}
		String lineContent = message.remove(line);
		if(start > lineContent.length() || end > lineContent.length()) {
			message.add(line, lineContent); // Repair damage
			return;
		}
		if(isTaggedInteractive(lineContent)) {
			lineContent = lineContent.replace("", "");
			// TODO check we can get rid of the dependency on ConsoleGenerator
			message.add(line, lineContent.substring(0, start) + ConsoleGenerator.generate(YamlParser.parse(insert)) + lineContent.substring(end));
			return;
		}

		// Find interactive lines meant for this message
		List<String> interactives = new ArrayList<>();
		while(line < message.size() && isTaggedInteractive(message.get(line))) {
			interactives.add(message.remove(line));
		}

		// Split the line and add the parts
		int at = line;
		if(start > 0) {
			message.add(line, lineContent.substring(0, start));
			at++;
			message.addAll(at, interactives);
			at += interactives.size();
		}
		message.addAll(at, insert);
		at += insert.size();
		message.addAll(at, interactives);
		at += interactives.size();
		if(end < lineContent.length()) {
			message.add(at, lineContent.substring(end));
			at++;
			message.addAll(at, interactives);
		}
	}

	/**
	 * Searches and returns the first continuous tag found in the given String.
	 * @return The tag (plus its preceding and subsequent content) if found.
	 * Null if nothing is found.
	 */
    private static TaggedContent getNextTag(String line) {
        // Find matching formatting and control sequences
        Matcher matcher = tagPattern.matcher(line);
		while(matcher.find()) {
            // Check if escaped, backslashes escape backslashes, so uneven number actually escapes the found match
            int backslashes = 0;
            int index = matcher.start() - 1;
            while (index >= 0 && line.charAt(index) == ESCAPE_CHAR) {
                backslashes++;
                index--;
            }
            if (backslashes % 2 == 1) {
                continue;
            }

            // Process the found match
            boolean closing = false;
			Object tag;
            if (matcher.group().equals("\\n")) {
                tag = Control.BREAK;
            } else if (matcher.group().startsWith("&") || matcher.group().startsWith(ChatColor.COLOR_CHAR + "")) {
				tag = NATIVE_TAGS.get(matcher.group().charAt(1)+"".toLowerCase());
			} else {
				String content = matcher.group().substring(1, matcher.group().length()-1).toLowerCase();
				tag = BRACKET_TAGS.get(content);

				// Try closing tag
				if(tag == null && content.length() > 0 && content.charAt(0) == '/') {
					tag = BRACKET_TAGS.get(content.substring(1));
					closing = true;
					// Control enums cannot be closed
					if(tag instanceof Control) {
						continue;
					}
				}
			}

			// Continue search if we found something like [abc] or &w that is not a tag
			if(tag != null) {
				return new TaggedContent(line.substring(0, matcher.start()), tag, line.substring(matcher.end()), closing);
			}
		}
		return null;
	}

	/**
	 * If the given line defines an interactive property (e.g. "hover: myText")
	 * the tag / property will be returned. Otherwise null is returned.
	 * @param line The line to try and get the interactive tag for
	 * @return TaggedContent object with the interactive tag type and split or null if not found
	 */
	private static TaggedContent getInteractiveTag(String line) {
		for(int index = 0; index < line.length(); index++) {
			char c = line.charAt(index);
			if(c == ' ' || c == '\t') {
				// Ignore (Skip spacing)
			} else {
				int end = line.indexOf(": ", index);
				if(end != -1) {
					String inBetween = line.substring(index, end).toLowerCase();
					if(INTERACTIVE_TAGS.containsKey(inBetween)) {
						Object tag = INTERACTIVE_TAGS.get(inBetween);
						String subsequentContent = line.substring(end+2);
						return new TaggedContent(null, tag, subsequentContent, false);
					}
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * Check if a line is an advanced declaration like hover or command
	 * @param line The line to check
	 * @return true if the line is interactive, false when it is a text line
	 */
	private static boolean isTaggedInteractive(String line) {
		return getInteractiveTag(line) != null;
	}

	/**
	 * - Splits lines at line breaks (creating a new line in the Array).
	 * - Removes empty lines at the beginning.
	 * - Removes lines with properties in front of the first text-line.
	 * @param inputLines The lines to clean
	 * @return cleaned lines
	 */
	private static ArrayList<String> cleanInputString(List<String> inputLines) {
		// Split li|nes at line breaks
		// In the end we will have a list with one line per element
		ArrayList<String> lines = new ArrayList<>();
		for(String line : inputLines) {
			lines.addAll(Arrays.asList(line.split("\\r?\\n")));
		}

		// Remove any special lines at the start (a real text line should be first)
		while(!lines.isEmpty() && isTaggedInteractive(lines.get(0))) {
			lines.remove(0);
		}

		return lines;
	}

	// Characters that should be escaped
	private static final List<String> toEscape = Arrays.asList(
			ESCAPE_CHAR+"",    // Used for escapes, must be first
			"%",            // Variables
            "[",            // Formatting and control enums
            "&",            // Traditional color formatting
			ChatColor.COLOR_CHAR+"" // Legacy formatting enums
	);

	/**
	 * Escape a (user provided) string for including it in a message
	 * @param message The message part to escape
	 * @return The escaped message part
	 */
	public static String escape(String message) {
		for(String escape : toEscape) {
			message = message.replace(escape, ESCAPE_CHAR+escape);
		}
		return message;
	}

	/**
	 * Reverse the escaping of control characters in a (user provided) string
	 * @param message The message part to escape
	 * @return The escaped message part
	 */
	public static String unescape(String message) {
		for(String escape : toEscape) {
			message = message.replace(ESCAPE_CHAR+escape, escape);
		}
		return message;
	}


	/**
	 * Represents a tag that has been found in a line
	 */
	private static class TaggedContent {
		final String precedingContent;
		final Object tag;
		final boolean closing;
		final String subsequentContent;

		/**
		 * Constructor
		 * @param pre Content that appeared before the tag
		 * @param tag The Tag that has been found (formatting or control)
		 * @param sub Content found after the tag
		 */
		public TaggedContent(String pre, Object tag, String sub, boolean closing) {
			this.precedingContent = pre;
			this.tag = tag;
			this.subsequentContent = sub;
			this.closing = closing;
		}
	}

}
