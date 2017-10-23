package me.wiefferink.interactivemessenger.processing;

import me.wiefferink.interactivemessenger.Log;

/**
 * Class to store a limit
 */
public class Limit {
	public int left;
	public int depth;
	public boolean notified = false;
	public Message message;
	public long started;

	/**
	 * Set the initial limit
	 * @param count   The limit to use
	 * @param message The message this limit is started for
	 */
	public Limit(int count, Message message) {
		this.left = count;
		this.depth = 0;
		this.message = message;
		this.started = System.currentTimeMillis();
	}

	/**
	 * Decrease the limit
	 * @throws ReplacementLimitReachedException when the limit hits zero
	 */
	public void decrease() throws ReplacementLimitReachedException {
		this.left--;
		if(left <= 0) {
			if(!notified) {
				notified = true;
				Log.error("Reached replacement limit, probably has replacements loops, problematic message key: " + message.getKey() + ", first characters of the message: " + Message.getMessageStart(message, 200));
			}
			throw new ReplacementLimitReachedException(this);
		}
	}

	/**
	 * Increase the limit
	 */
	public void increase() {
		this.left++;
	}

	/**
	 * Check if the limit is reached
	 * @return true if the limit is reached, otherwise false
	 */
	public boolean reached() {
		return left <= 0;
	}

	@Override
	public String toString() {
		return "Limit(left: " + left + ", notified: " + notified + ", depth: " + depth + ", message.key: " + message.getKey() + ")";
	}
}
