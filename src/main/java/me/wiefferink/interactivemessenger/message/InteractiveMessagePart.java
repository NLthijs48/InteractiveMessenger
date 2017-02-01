package me.wiefferink.interactivemessenger.message;

import me.wiefferink.interactivemessenger.message.enums.Click;
import me.wiefferink.interactivemessenger.message.enums.Hover;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Holds a string with interactive formatting.
 */
public class InteractiveMessagePart extends LinkedList<TextMessagePart> {

	public boolean newline = false;

	// Click
	public Click onClick = null;
	public String clickContent = "";

	// Hover
	public Hover onHover = null;
	public LinkedList<TextMessagePart> hoverContent = null;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("InteractiveMessagePart(textMessageParts:"+super.toString());
		if(onClick != null) {
			result.append(", onClick:").append(onClick);
		}
		if(clickContent != null && !clickContent.isEmpty()) {
			result.append(", clickContent:").append(clickContent);
		}
		if(onHover != null) {
			result.append(", onHover:").append(onHover);
		}
		if(hoverContent != null) {
			result.append(", hoverContent:").append(hoverContent);
		}
		if(newline) {
			result.append(", newline:").append(newline);
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof InteractiveMessagePart)) {
			return false;
		}
		InteractiveMessagePart part = (InteractiveMessagePart)o;
		return newline == part.newline &&
				onClick == part.onClick &&
				Objects.equals(clickContent, part.clickContent) &&
				onHover == part.onHover &&
				Objects.equals(hoverContent, part.hoverContent) &&
				super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(newline, onClick, clickContent, onHover, hoverContent, super.hashCode());
	}

}