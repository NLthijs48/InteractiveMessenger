package me.wiefferink.interactivemessenger.processing;

public class Replacement {

	public static ReplacementProvider name(final String name, final Object replacement) {
		return new ReplacementProvider() {
			@Override
			public Object provideReplacement(String variable) {
				if(variable.equalsIgnoreCase(name)) {
					return replacement;
				} else {
					return null;
				}
			}
		};
	}

	public static ReplacementProvider prefix(final String prefix, final ReplacementProvider provider) {
		return new ReplacementProvider() {
			@Override
			public Object provideReplacement(String variable) {
				if(variable.startsWith(prefix)) {
					return provider.provideReplacement(variable.substring(prefix.length()));
				} else {
					return null;
				}
			}
		};
	}
}
