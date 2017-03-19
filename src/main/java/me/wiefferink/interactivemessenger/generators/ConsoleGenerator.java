package me.wiefferink.interactivemessenger.generators;

import me.wiefferink.interactivemessenger.message.InteractiveMessage;
import me.wiefferink.interactivemessenger.message.InteractiveMessagePart;
import me.wiefferink.interactivemessenger.message.TextMessagePart;
import me.wiefferink.interactivemessenger.message.enums.Color;
import me.wiefferink.interactivemessenger.message.enums.Format;
import org.bukkit.ChatColor;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

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
		Color activeColor = Color.WHITE;
		Set<Format> activeFormatting = EnumSet.noneOf(Format.class);
		for(InteractiveMessagePart interactivePart : message) {
			for(TextMessagePart textPart : interactivePart) {
				// Use reset if there is formatting active we need to get rid of
				if(!textPart.getFormatting().containsAll(activeFormatting)) {
					result.append(ChatColor.RESET);
					activeColor = Color.WHITE;
					activeFormatting.clear();
				}

				// Color
				if(activeColor != textPart.getColor()) {
					result.append(ChatColor.COLOR_CHAR).append(colorCode.get(textPart.getColor()));
					activeColor = textPart.getColor();
				}

				// Formatting
				Set<Format> formattingToAdd = EnumSet.noneOf(Format.class);
				formattingToAdd.addAll(textPart.getFormatting());
				formattingToAdd.removeAll(activeFormatting);
				for(Format format : formattingToAdd) {
					result.append(ChatColor.COLOR_CHAR).append(formatCode.get(format));
					activeFormatting.add(format);
				}

				// Text
				result.append(textPart.getText());
			}

            // Add newlines
			if(interactivePart.hasNewline()) {
				result.append("\n");
            }
        }
		return result.toString();
	}

}
