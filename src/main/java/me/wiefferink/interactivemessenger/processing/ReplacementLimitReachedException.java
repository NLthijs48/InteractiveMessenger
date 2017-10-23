package me.wiefferink.interactivemessenger.processing;

public class ReplacementLimitReachedException extends Exception {
	private Limit limit;

	public ReplacementLimitReachedException(Limit limit) {
		this.limit = limit;
	}

	public Limit getLimit() {
		return limit;
	}
}
