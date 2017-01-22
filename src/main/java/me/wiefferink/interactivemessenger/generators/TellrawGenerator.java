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


	/**
	 * Parses the given message to a JSON array that can be
	 * used with the tellraw command and the like.
	 * @param message The parsed InteractiveMessage.
	 * @return JSON string that can be send to a player (multiple means line breaks have been used)
	 */
	public static List<String> generate(InteractiveMessage message) {
		List<String> result = new ArrayList<>();

		List<InteractiveMessagePart> combine = new ArrayList<>(); // Part that are combined to a line
		while(!message.isEmpty()) {
			InteractiveMessagePart part = message.removeFirst();
			combine.add(part);
			if(part.newline || message.isEmpty()) {
				if(combine.size() == 1) {
					StringBuilder nextLine = new StringBuilder();
					toJSON(combine.get(0), nextLine);
					result.add(nextLine.toString());
				} else if(combine.size() > 1) {
					StringBuilder nextLine = new StringBuilder("{\"text\":\"\",\"extra\":[");
					for(int i = 0; i < combine.size(); i++) {
						// Skip possibly last empty InteractiveMessagePart which has newline set
						if(!combine.get(i).isEmpty()) {
							if(i != 0) {
								nextLine.append(",");
							}
							toJSON(combine.get(i), nextLine);
						}
					}
					nextLine.append("]}");
					result.add(nextLine.toString());
				}
				combine.clear();
			}
		}
		return result;
	}


	/**
	 * Get a JSON component for this message part
	 * @param sb The StringBuilder to append the result to
	 * @return The StringBuilder where the JSON has been appended to
	 */
	private static StringBuilder toJSON(InteractiveMessagePart part, StringBuilder sb) {
		// Error case, should never happen, print something as safeguard
		if(part.size() == 0) {
			sb.append("{\"text\":\"\"}");
			return sb;
		}

		if(part.size() == 1) {
			// Add attributes to TextMessagePart object
			toJSON(part.getFirst(), sb);
			sb.deleteCharAt(sb.length()-1);
		} else {
			sb.append('{');
			sb.append("\"text\":\"\",\"extra\":[");
			for(TextMessagePart textPart : part) {
				toJSON(textPart, sb);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(']');
		}

		// Add click action
		if(part.onClick != null) {
			sb.append(',');
			sb.append("\"clickEvent\":{");
			sb.append("\"action\":\"").append(clickJsonKey.get(part.onClick)).append("\",");
			sb.append("\"value\":");
			printJsonString(part.clickContent, sb);
			sb.append('}');
		}

		// Add hover action
		if(part.onHover != null) {
			sb.append(',');
			sb.append("\"hoverEvent\":{");
			sb.append("\"action\":\"").append(hoverJsonKey.get(part.onHover)).append("\",");
			sb.append("\"value\":");
			if(part.hoverContent.size() == 1) {
				TextMessagePart hoverPart = part.hoverContent.getFirst();
				if(hoverPart.hasFormatting()) {
					toJSON(hoverPart, sb);
				} else {
					printJsonString(hoverPart.text, sb);
				}
			} else {
				sb.append('[');
				for(TextMessagePart hoverPart : part.hoverContent) {
					toJSON(hoverPart, sb);
					sb.append(',');
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append(']');
			}
			sb.append('}');
		}
		sb.append('}');
		return sb;
	}

	/**
	 * Get a JSON component for this message part
	 * @param sb The StringBuilder to append the JSON result to
	 * @return The StringBuilder where the result has been appended to
	 */
	private static StringBuilder toJSON(TextMessagePart part, StringBuilder sb) {
		sb.append('{');

		// Text
		sb.append("\"text\":");
		printJsonString(part.text, sb);

		// Color
		if(part.color != null && part.color != Color.WHITE) {
			sb.append(",\"color\":\"").append(part.color.name().toLowerCase()).append("\"");
		}

		// Formatting
		for(Format formatting : part.formatting) {
			sb.append(",\"");
			sb.append(formatJsonKey.get(formatting)).append("\":");
			sb.append("true");
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
