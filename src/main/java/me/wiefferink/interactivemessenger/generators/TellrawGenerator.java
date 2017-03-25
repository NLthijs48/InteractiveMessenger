package me.wiefferink.interactivemessenger.generators;

import me.wiefferink.interactivemessenger.message.InteractiveMessage;
import me.wiefferink.interactivemessenger.message.InteractiveMessagePart;
import me.wiefferink.interactivemessenger.message.TextMessagePart;
import me.wiefferink.interactivemessenger.message.enums.Click;
import me.wiefferink.interactivemessenger.message.enums.Color;
import me.wiefferink.interactivemessenger.message.enums.Format;
import me.wiefferink.interactivemessenger.message.enums.Hover;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TellrawGenerator {

	/**
	 * Map Format to the JSON keys used in tellraw (https://minecraft.gamepedia.com/Commands#Raw_JSON_text)
	 */
	private static final EnumMap<Format, String> formatJsonKey = new EnumMap<Format, String>(Format.class) {{
		put(Format.BOLD, "bold");
		put(Format.ITALIC, "italic");
		put(Format.UNDERLINE, "underlined");
		put(Format.STRIKETHROUGH, "strikethrough");
		put(Format.OBFUSCATE, "obfuscated");
	}};

	/**
	 * Map Click to the JSON keys used in tellraw (https://minecraft.gamepedia.com/Commands#Raw_JSON_text)
	 */
	private static final EnumMap<Click, String> clickJsonKey = new EnumMap<Click, String>(Click.class) {{
		put(Click.COMMAND, "run_command");
		put(Click.LINK, "open_url");
		put(Click.SUGGEST, "suggest_command");
	}};

	/**
	 * Map Hover to the JSON keys used in tellraw (https://minecraft.gamepedia.com/Commands#Raw_JSON_text)
	 */
	private static final EnumMap<Hover, String> hoverJsonKey = new EnumMap<Hover, String>(Hover.class) {{
		put(Hover.HOVER, "show_text");
	}};

	/* Thoughts about printing JSON that is as small as possible
	 *
	 * Message structure:
	 *   InteractiveMessage:
	 *     line (groups of InteractiveMessageParts without newlines, except possibly at the end)
	 *       InteractiveMessageParts:
	 *         TextMessagePart
	 *         more...
	 *       more...
	 *     more...
	 *
	 * What to do:
	 *   InteractiveMessage:
	 *     - create list to hold the lines
	 *     - print the lines to this list
	 *   line:
	 *     - single InteractiveMessagePart: print the part
	 *     - multiple InteractiveMessageParts: print an array with the parts
	 *   InteractiveMessagePart:
	 *     - single TextMessagePart: print the part
	 *     - multiple TextMessageParts: print an array with the parts
	 *     - with hover/click wrap like this: {text: "", extra: <TextMessageParts>, hoverEvent: <>, clickEvent: <>}
	 *   TextMessagePart:
	 *     - without formatting: "<text>"
	 *     - with formatting: {text: <text>, bold: <boolean>, color: <color>, ...}
	 *
	 * Take into account:
	 *   - when a line has a single InteractiveMessagePart, without hover/click, which has a single TextMessagePart
	 *     without formatting it will be printed as simple string and needs to be wrapped in an array
	 *     (/tellraw "hi" does not work)
	 *   - If there is only one TextMessagePart it might be cobined with his InteractiveMessagePart parent
	 *     (this is challenging in practice though)
	 *
	 */

	/**
	 * Parses the given message to a JSON array that can be
	 * used with the tellraw command and the like.
	 * @param message The parsed InteractiveMessage.
	 * @return JSON string that can be send to a player (multiple means line breaks have been used)
	 */
	public static List<String> generate(InteractiveMessage message) {
		message = message.copy();
		// Resulting JSON strings, each one should be printed on a new line (separate /tellraw command to provide 1.7 compatibility)
		List<String> result = new ArrayList<>();

		// Combine InteractiveMessageParts without newlines into a group
		List<InteractiveMessagePart> combine = new ArrayList<>();
		while(!message.isEmpty()) {
			InteractiveMessagePart part = message.removeFirst();
			// Skip empty newline parts
			if(!(part.hasNewline() && part.isEmpty())) {
				combine.add(part);
			}
			// If we need to go to the next line or we got all the parts, print it
			if(part.hasNewline() || message.isEmpty()) {
				String lineResult = "\"\"";
				if(combine.size() == 1) {
					lineResult = toJson(combine.get(0), new StringBuilder()).toString();
				} else if(combine.size() > 1) {
					StringBuilder nextLine = new StringBuilder("{\"text\":\"\",\"extra\":[");
					for(int i = 0; i < combine.size(); i++) {
						if(i != 0) {
							nextLine.append(",");
						}
						toJson(combine.get(i), nextLine);
					}
					nextLine.append("]}");
					lineResult = nextLine.toString();
				}
				// Handle bare string case (wrap in array)
				if(lineResult.charAt(0) == '"' && lineResult.charAt(lineResult.length()-1) == '"') {
					lineResult = '['+lineResult+']';
				}
				result.add(lineResult);
				combine.clear();
			}
		}
		return result;
	}


	/**
	 * Get a JSON component for this message part
	 * @param part The InteractiveMessagePart to be printed
	 * @param sb The StringBuilder to append the result to
	 * @return The StringBuilder where the JSON has been appended to
	 */
	private static StringBuilder toJson(InteractiveMessagePart part, StringBuilder sb) {
		// Error case, should never happen, print something as safeguard
		if(part.size() == 0) {
			sb.append("\"\"");
			return sb;
		}

		// Only wrap if there are interactive parts to be added
		String finalCloser = "";
		String arrayCloser = "";
		boolean isInExtra = false;
		if(part.isInteractive() || part.size() > 1) {
			sb.append("{\"text\":");
			finalCloser = "}";
			isInExtra = true;
		}

		// Add TextMessageParts
		if(part.size() == 1) {
			if(part.getFirst().hasFormatting() && isInExtra) {
				sb.append("\"\",\"extra\":[");
			}
			toJson(part.getFirst(), sb);
			if(part.getFirst().hasFormatting() && isInExtra) {
				sb.append("]");
			}
		} else {
			if(!isInExtra) {
				sb.append("{\"text\":");
			}
			sb.append("\"\",\"extra\":[");
			for(TextMessagePart textPart : part) {
				toJson(textPart, sb);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1); // Remove trailing comma
			sb.append("]");
			if(!isInExtra) {
				sb.append("}");
			}
		}
		sb.append(arrayCloser);

		// Add click action
		if(part.getOnClick() != null) {
			sb.append(',');
			sb.append("\"clickEvent\":{");
			sb.append("\"action\":\"");
			sb.append(clickJsonKey.get(part.getOnClick()));
			sb.append("\",");
			sb.append("\"value\":");
			printJsonString(part.getOnClickContent(), sb);
			sb.append('}');
		}

		// Add hover action
		if(part.getOnHover() != null && !part.getOnHoverContent().isEmpty()) {
			sb.append(',');
			sb.append("\"hoverEvent\":{");
			sb.append("\"action\":\"");
			sb.append(hoverJsonKey.get(part.getOnHover()));
			sb.append("\",");
			sb.append("\"value\":");
			if(part.getOnHoverContent().size() == 1) {
				toJson(part.getOnHoverContent().getFirst(), sb);
			} else {
				sb.append("{\"text\":\"\",\"extra\":[");
				for(TextMessagePart hoverPart : part.getOnHoverContent()) {
					toJson(hoverPart, sb);
					sb.append(',');
				}
				sb.deleteCharAt(sb.length()-1); // Remove trailing comma
				sb.append("]}");
			}
			sb.append('}');
		}

		sb.append(finalCloser);
		return sb;
	}

	/**
	 * Get a JSON component for this message part
	 * @param part The TextMessagePart to print
	 * @param sb The StringBuilder to append the JSON result to
	 * @return The StringBuilder where the result has been appended to
	 */
	private static StringBuilder toJson(TextMessagePart part, StringBuilder sb) {
		// Simple string
		if(!part.hasFormatting()) {
			printJsonString(part.getText(), sb);
			return sb;
		}

		// String with formatting
		sb.append('{');

		// Text
		sb.append("\"text\":");
		printJsonString(part.getText(), sb);

		// Color
		if(part.getColor() != Color.WHITE) {
			sb.append(",\"color\":\"");
			sb.append(part.getColor().name().toLowerCase());
			sb.append("\"");
		}

		// Formatting
		for(Format formatting : part.getFormatting()) {
			sb.append(",\"");
			sb.append(formatJsonKey.get(formatting));
			sb.append("\":true");
		}

		sb.append('}');
		return sb;
	}

	/**
	 * Produce a string in double quotes with backslash sequences in all the right places.
	 * @param string The string to print
	 * @param sb     The StringBuilder to add the quoted string to
	 * @return sb
	 */
	private static StringBuilder printJsonString(String string, StringBuilder sb) {
		if(string == null || string.length() == 0) {
			sb.append("\"\"");
			return sb;
		}

		sb.append('"');
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			switch(c) {
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
					if(c <= '\u001F' || (c >= '\u007F' && c <= '\u009F') || (c >= '\u2000' && c <= '\u20FF')) {
						String ss = Integer.toHexString(c);
						sb.append("\\u");
						for(int k = 0; k < 4-ss.length(); k++) {
							sb.append('0');
						}
						sb.append(ss.toUpperCase());
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb;
	}

}
