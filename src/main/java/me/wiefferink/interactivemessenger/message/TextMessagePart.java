package me.wiefferink.interactivemessenger.message;

import me.wiefferink.interactivemessenger.message.enums.Color;
import me.wiefferink.interactivemessenger.message.enums.Format;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds a string with basic (non-interactive) formatting.
 */
public class TextMessagePart {
	public String text = "";
	public Color color = null;
	public Set<Format> formatting = new HashSet<>();

	/**
	 * Check if this part has formatting
	 * @return true if this part has formatting, otherwise false
	 */
	public boolean hasFormatting() {
		return !(color == Color.WHITE && formatting.isEmpty());
	}

	@Override
	public String toString() {
		return "TextMessagePart(text:"+text+", color:"+color+", formatting:"+formatting.toString()+")";
	}
}
