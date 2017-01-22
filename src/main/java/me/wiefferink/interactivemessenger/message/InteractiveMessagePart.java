package me.wiefferink.interactivemessenger.message;

import me.wiefferink.interactivemessenger.message.enums.Click;
import me.wiefferink.interactivemessenger.message.enums.Hover;

import java.util.LinkedList;

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
}