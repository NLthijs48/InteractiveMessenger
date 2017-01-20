package me.wiefferink.interactivemessenger.generators;

import me.wiefferink.interactivemessenger.message.InteractiveMessage;
import me.wiefferink.interactivemessenger.message.InteractiveMessagePart;
import me.wiefferink.interactivemessenger.message.TextMessagePart;
import me.wiefferink.interactivemessenger.message.enums.Color;
import me.wiefferink.interactivemessenger.message.enums.Format;
import org.bukkit.ChatColor;

import java.util.EnumMap;

public class ConsoleGenerator {

	/**
	 * Map Color to the native formatting codes used in chat (https://minecraft.gamepedia.com/Formatting_codes)
	 */
	private static final EnumMap<Color, Character> colorCode = new EnumMap<Color, Character>(Color.class) {{
		put(Color.BLACK, '0');
		put(Color.DARK_BLUE, '1');
		put(Color.DARK_GREEN, '2');
		put(Color.DARK_AQUA, '3');
		put(Color.DARK_RED, '4');
		put(Color.DARK_PURPLE, '5');
		put(Color.GOLD, '6');
		put(Color.GRAY, '7');
		put(Color.DARK_GRAY, '8');
		put(Color.BLUE, '9');
		put(Color.GREEN, 'a');
		put(Color.AQUA, 'b');
		put(Color.RED, 'c');
		put(Color.LIGHT_PURPLE, 'd');
		put(Color.YELLOW, 'e');
		put(Color.WHITE, 'f');
	}};

	/**
	 * Map Format to the native formatting codes used in chat (https://minecraft.gamepedia.com/Formatting_codes)
	 */
	private static final EnumMap<Format, Character> formatCode = new EnumMap<Format, Character>(Format.class) {{
		put(Format.BOLD, 'l');
		put(Format.ITALIC, 'o');
		put(Format.UNDERLINE, 'n');
		put(Format.STRIKETHROUGH, 's');
		put(Format.OBFUSCATE, 'k');
	}};


	/**
	 * Parses the given message to a String containing control characters
	 * for formatting that can be used for console outputs, but also for normal player
	 * messages.
	 * <p>
	 * The returned message will only contain colors, bold, italic, underlining and 'magic'
	 * characters. Hovers and other advanced tellraw enums will be skipped.
	 * @param message The parsed InteractiveMessage
	 * @return Plain message that can be send
	 */
	public static String generate(InteractiveMessage message) {
		StringBuilder result = new StringBuilder();
		for(InteractiveMessagePart part : message) {
			toSimpleString(part, result);
		}

		return result.toString();
	}

	/**
	 * Append the message content to the StringBuilder
	 * @param sb The StringBuilder to append the message to
	 */
	private static StringBuilder toSimpleString(InteractiveMessagePart part, StringBuilder sb) {
		for(TextMessagePart textPart : part) {
			toSimpleString(textPart, sb);
		}

		if(part.newline) {
			sb.append("\n");
		}
		return sb;
	}

	/**
	 * Get a simple colored/formatted string
	 * @param sb The StringBuilder to append the result to
	 * @return StringBuilder with the message appended
	 */
	private static StringBuilder toSimpleString(TextMessagePart part, StringBuilder sb) {
		// Color
		if(part.color != null) {
			sb.append(ChatColor.COLOR_CHAR).append(colorCode.get(part.color));
		}
		// Formatting
		for(Format format : part.formatting) {
			sb.append(ChatColor.COLOR_CHAR).append(formatCode.get(format));
		}
		// Text
		sb.append(part.text);
		return sb;
	}

}
