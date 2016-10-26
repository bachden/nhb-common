package com.nhb.common.scripting.statics;

import java.util.HashSet;
import java.util.Set;

public enum ScriptLanguage {

	GROOVY("groovy", "gradle"), JAVASCRIPT("js"), PYTHON("py");

	private final Set<String> extensions = new HashSet<>();

	private ScriptLanguage(String... exts) {
		if (exts != null) {
			for (String ext : exts) {
				this.extensions.add(ext);
			}
		}
	}

	public static ScriptLanguage fromExtension(String extension) {
		if (extension != null) {
			for (ScriptLanguage value : values()) {
				if (value.extensions.contains(extension)) {
					return value;
				}
			}
		}
		return null;
	}
}
