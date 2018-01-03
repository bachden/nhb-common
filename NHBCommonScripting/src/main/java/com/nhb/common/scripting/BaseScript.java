package com.nhb.common.scripting;

import java.util.HashMap;
import java.util.Map;

import com.nhb.common.scripting.statics.ScriptLanguage;

import lombok.Getter;
import lombok.Setter;

public class BaseScript implements Script {

	@Getter
	private ScriptLanguage language;

	@Getter
	@Setter
	private String name;

	@Setter
	@Getter
	private String content;

	@Getter
	private final Map<String, String> compileArguments = new HashMap<>();

	protected BaseScript(ScriptLanguage language) {
		this.language = language;
	}
}
