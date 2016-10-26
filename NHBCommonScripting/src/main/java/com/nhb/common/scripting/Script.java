package com.nhb.common.scripting;

import java.util.Map;

import com.nhb.common.scripting.statics.ScriptLanguage;

public interface Script {

	String getName();

	String getContent();

	ScriptLanguage getLanguage();

	Map<String, String> getCompileArguments();
}
