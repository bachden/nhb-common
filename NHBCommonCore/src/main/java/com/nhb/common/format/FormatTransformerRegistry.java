package com.nhb.common.format;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface FormatTransformerRegistry {

	static FormatTransformerRegistry newInstance(FormatTransformerRegistry... registrys) {
		FormatTransformerRegistry result = new DefaultFormatTransformerRegistry();
		if (registrys != null) {
			for (FormatTransformerRegistry registry : registrys) {
				result.inherit(registry);
			}
		}
		return result;
	}

	/**
	 * Create new FormatTransformerRegistry, inherited from
	 * CommonTextTransformerRegistry, CommonNumberTransformerRegistry,
	 * CommonDateTransformerRegistry
	 * 
	 * @return
	 */
	static FormatTransformerRegistry newwDefault() {
		return newInstance(//
				CommonTextTransformerRegistry.newInstance(), //
				CommonNumberTransformerRegistry.newInstance(), //
				CommonDateTransformerRegistry.newInstance());
	}

	default List<FormatTransformer> getChain(String... transformerNames) {
		return this.getChain(Arrays.asList(transformerNames));
	}

	Map<String, FormatTransformer> getAll();

	List<FormatTransformer> getChain(List<String> transformerNames);

	FormatTransformer addTransformer(String name, FormatTransformer transformer);

	FormatTransformer removeTransformer(String name);

	default FormatTransformerRegistry inherit(FormatTransformerRegistry parent) {
		if (parent != null) {
			for (Entry<String, FormatTransformer> entry : parent.getAll().entrySet()) {
				this.addTransformer(entry.getKey(), entry.getValue());
			}
		}
		return this;
	}
}
