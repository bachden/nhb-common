package com.nhb.common.scripting.groovy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.nhb.common.scripting.BaseScript;
import com.nhb.common.scripting.statics.ScriptLanguage;
import com.nhb.common.utils.Converter;

import lombok.Getter;
import lombok.Setter;

public class GroovyScript extends BaseScript {
	
	@Getter
	@Setter
	private boolean indy = false;
	
	@Getter
	@Setter
	private ClassLoader clazzLoader = null;

	public GroovyScript() {
		super(ScriptLanguage.GROOVY);
	}

	public GroovyScript(String content) {
		this();
		this.setContent(content);

		byte[] sha512;
		try {
			sha512 = MessageDigest.getInstance("SHA-512").digest(content.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Cannot encrypt content to make name with SHA-512", e);
		}

		String name = Converter.bytesToHex(sha512);
		this.setName(name);
	}

	public GroovyScript(String content, String name) {
		this();
		this.setContent(content);
		this.setName(name);
	}
}
