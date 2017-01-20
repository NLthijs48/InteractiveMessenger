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
		return "InteractiveMessagePart(textMessageParts:"+super.toString()+", onClick:"+onClick+", clickContent:"+clickContent+", onHover:"+onHover+", hoverContent:"+hoverContent+", newline:"+newline+")";
	}
}