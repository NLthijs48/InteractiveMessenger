package me.wiefferink.interactivemessenger.message;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Interactive message
 */
public class InteractiveMessage extends LinkedList<InteractiveMessagePart> {

	public InteractiveMessage(Collection<? extends InteractiveMessagePart> values) {
		super(values);
	}

	public InteractiveMessage() {
		super();
	}

	public InteractiveMessage copy() {
		return new InteractiveMessage(this);
	}

}
