package me.wiefferink.interactivemessenger.message;

import me.wiefferink.interactivemessenger.message.enums.Color;
import me.wiefferink.interactivemessenger.message.enums.Format;

import java.util.*;

/**
 * Holds a string with basic (non-interactive) formatting.
 */
public class TextMessagePart {
	private String text = "";
	private Color color = Color.WHITE;
	private Set<Format> formatting = EnumSet.noneOf(Format.class);

	/**
	 * Set the text
	 * @param text The text to set (if null text will be set to an empty string)
	 * @return this
	 */
	public TextMessagePart text(String text) {
		if(text == null) {
			text = "";
		}
		this.text = text;
		return this;
	}

	/**
	 * Append to the current text
	 * @param toAppend The string to append (null is ignored)
	 * @return this
	 */
	public TextMessagePart appendText(String toAppend) {
		if(toAppend != null) {
			text += toAppend;
		}
		return this;
	}

	/**
	 * Get the text of this part
	 * @return The text, never null
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set the color
	 * @param color The color to set (if null it defaults to white)
	 * @return this
	 */
	public TextMessagePart color(Color color) {
		if(color == null) {
			color = Color.WHITE;
		}
		this.color = color;
		return this;
	}

	/**
	 * Get the color
	 * @return The color, never null
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Add formatting
	 * @param formatting The formatting to add
	 * @return this
	 */
	public TextMessagePart format(Format... formatting) {
		if(formatting != null) {
			this.formatting.addAll(Arrays.asList(formatting));
		}
		return this;
	}

	/**
	 * Add formatting
	 * @param formatting The formatting to add
	 * @return this
	 */
	public TextMessagePart format(Collection<Format> formatting) {
		if(formatting != null) {
			this.formatting.addAll(formatting);
		}
		return this;
	}

	/**
	 * Get the formatting of this part
	 * @return The formattting of this part, never null
	 */
	public Set<Format> getFormatting() {
		return formatting;
	}

	/**
	 * Check if this part has formatting
	 * @return true if this part has formatting, otherwise false
	 */
	public boolean hasFormatting() {
		return !(color == Color.WHITE && formatting.isEmpty());
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("TextMessagePart(text:"+text);
		if(color != Color.WHITE) {
			result.append(", color:").append(color);
		}
		if(!formatting.isEmpty()) {
			result.append(", formatting:").append(formatting);
		}
		result.append(")");
		return result.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof TextMessagePart)) {
			return false;
		}
		TextMessagePart part = (TextMessagePart)o;
		return Objects.equals(text, part.text) &&
				color == part.color &&
				Objects.equals(formatting, part.formatting);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, color, formatting);
	}

}
