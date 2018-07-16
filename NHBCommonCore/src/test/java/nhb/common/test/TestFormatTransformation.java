package nhb.common.test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.nhb.common.format.CommonNumberTransformerRegistry;
import com.nhb.common.format.GlobalFormatTransformerRegistry;
import com.nhb.common.utils.StringUtils;

public class TestFormatTransformation {

	public static void main(String[] args) {
		String str = "My name is {{ name > nameTransform }}, " //
				+ "{{ age }} years old, " //
				+ "monthly salary {{ salary > decrement10% > thousandSeparate}} {{currency > upperCase}}, " //
				+ "health {{health > percentage}}, " //
				+ "date: {{today > localOnlyDate}}, " //
				+ "time: {{today > localOnlyTime12}}, " //
				+ "fulltime: {{today > localFullTime24}}, " //
				+ "fulltime gmt: {{today > gmtFullTime12}}";

		Map<String, Object> data = new HashMap<>();
		data.put("name", "nguyễn hoàng bách");
		data.put("age", 30);
		data.put("salary", 10000000.97);
		data.put("currency", "VND");
		data.put("health", "0.9756");
		data.put("today", Calendar.getInstance().getTime());

		GlobalFormatTransformerRegistry.getInstance().addTransformer("decrement10%",
				CommonNumberTransformerRegistry.newXEvalExpTransformer("0.9 * x"));

		GlobalFormatTransformerRegistry.getInstance().addAlias("nameTransform", "lowerCase > capitalize",
				"stripAccents");

		System.out.println(StringUtils.transform(str, data));
	}
}
