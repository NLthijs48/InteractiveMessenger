package me.wiefferink.interactivemessenger.message;

import me.wiefferink.interactivemessenger.message.enums.Click;
import me.wiefferink.interactivemessenger.message.enums.Hover;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Holds a string with interactive formatting.
 */
public class InteractiveMessagePart extends LinkedList<TextMessagePart> {

	private boolean newline = false;

	// Click
	private Click onClick = null;
	private String clickContent = null;

	// Hover
	private Hover onHover = null;
	private LinkedList<TextMessagePart> hoverContent = null;

	/**
	 * Start a new line after this part
	 * @return this
	 */
	public InteractiveMessagePart newline() {
		newline = true;
		return this;
	}

	/**
	 * Don't start a new line after this part
	 * @return this
	 */
	public InteractiveMessagePart noNewline() {
		newline = false;
		return this;
	}

	/**
	 * Start or don't start a new line after this part
	 * @param newline true to start a new line after this part, otherwise false
	 * @return this
	 */
	public InteractiveMessagePart newline(boolean newline) {
		this.newline = newline;
		return this;
	}

	/**
	 * Check if the next part should start on a new line
	 * @return true if the next part should start on a new line, otherwise false
	 */
	public boolean hasNewline() {
		return newline;
	}

	/**
	 * Set the click action
	 * @param onClick The click action to use or null for none
	 * @return this
	 */
	public InteractiveMessagePart onClick(Click onClick) {
		this.onClick = onClick;
		return this;
	}

	/**
	 * Get the click action
	 * @return The click action or null for none
	 */
	public Click getOnClick() {
		return onClick;
	}

	/**
	 * Set the click content
	 * @param clickContent The content used by the click action
	 * @return this
	 */
	public InteractiveMessagePart onClickContent(String clickContent) {
		if(clickContent == null) {
			clickContent = "";
		}
		this.clickContent = clickContent;
		return this;
	}

	/**
	 * Get the click content
	 * @return The click content
	 */
	public String getOnClickContent() {
		if(clickContent == null) {
			clickContent = "";
		}
		return clickContent;
	}

	/**
	 * Set the hover action
	 * @param onHover The hover action to use or null for none
	 * @return this
	 */
	public InteractiveMessagePart onHover(Hover onHover) {
		this.onHover = onHover;
		return this;
	}

	/**
	 * Get the hover action
	 * @return The hover action or null for none
	 */
	public Hover getOnHover() {
		return onHover;
	}

	/**
	 * Set the hover content
	 * @param hoverContent The content used by the hover action, never null
	 * @return this
	 */
	public InteractiveMessagePart onHoverContent(List<TextMessagePart> hoverContent) {
		this.hoverContent = new LinkedList<>();
		if(hoverContent != null) {
			this.hoverContent.addAll(hoverContent);
		}
		return this;
	}

	/**
	 * Get the hover content
	 * @return The hover content, never null
	 */
	public LinkedList<TextMessagePart> getOnHoverContent() {
		if(hoverContent == null) {
			hoverContent = new LinkedList<>();
		}
		return hoverContent;
	}

	/**
	 * Check if there are interactive actions defined
	 * @return true if there is a hover or click event addd, false if it is only text
	 */
	public boolean isInteractive() {
		return onHover != null || onClick != null;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("InteractiveMessagePart(textMessageParts:"+super.toString());
		if(onClick != null) {
			result.append(", onClick:").append(onClick);
		}
		if(clickContent != null && !clickContent.isEmpty()) {
			result.append(", onClickContent:").append(clickContent);
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
        result.append(")");
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