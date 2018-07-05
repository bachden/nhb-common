package com.nhb.common.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.base.CaseFormat;
import com.nhb.common.format.FormatTransformer;
import com.nhb.common.format.FormatTransformerRegistry;
import com.nhb.common.format.GlobalFormatTransformerRegistry;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StringUtils {

	@Data
	@Builder
	public static class StringFormatOption {

		private boolean autoFormatNumber;
		private DecimalFormat decimalFormat;

		public DecimalFormat getDecimalFormat() {
			if (this.decimalFormat == null) {
				this.decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
				this.decimalFormat.getDecimalFormatSymbols().setGroupingSeparator(',');
			}
			return this.decimalFormat;
		}
	}

	private StringUtils() {
		// just prevent other can create new instance...
	}

	public static List<String> getAllMatches(String text, String regex) {
		List<String> matches = new ArrayList<String>();
		Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);
		while (m.find()) {
			matches.add(m.group(1));
		}
		return matches;
	}

	public static final String toCamelCase(String inputString) {
		if (inputString != null) {
			return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, inputString);
		}
		return null;
	}

	public static final String upperCaseFirstLetter(String inputString) {
		if (inputString == null) {
			return null;
		}
		return Character.toUpperCase(inputString.charAt(0)) + inputString.substring(1);
	}

	public static final String lowerCaseFirstLetter(String inputString) {
		if (inputString == null) {
			return null;
		}
		return Character.toLowerCase(inputString.charAt(0)) + inputString.substring(1);
	}

	public static final String randomString(int length) {
		String result = RandomStringUtils.random(length,
				"}|:'\"vwxyzABCDabcNOu345enopkl90~!@#$%mWXYZ6*()d12<>?IJKLM_+G78^&PQRSqrstH{EFTUVfghij");
		return result;
	}

	public static final boolean isPrinable(String str) {
		return !match(str, "\\p{C}");
	}

	public static final boolean match(String string, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(string);
		return matcher.find();
	}

	public static String implode(Object... elements) {
		if (elements != null) {
			StringBuilder sb = new StringBuilder();
			for (Object ele : elements) {
				sb.append(ele);
			}
			return sb.toString();
		}
		return null;
	}

	public static String implodeWithGlue(String glue, Object... elements) {
		if (elements != null && glue != null) {
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;
			for (Object ele : elements) {
				if (!isFirst) {
					sb.append(glue);
				} else {
					isFirst = false;
				}
				sb.append(ele);
			}
			return sb.toString();
		}
		return null;
	}

	public static String implodeWithGlue(String glue, List<?> elements) {
		if (elements != null && glue != null) {
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;
			for (Object ele : elements) {
				if (!isFirst) {
					sb.append(glue);
				} else {
					isFirst = false;
				}
				sb.append(ele);
			}
			return sb.toString();
		}
		return null;
	}

	public static boolean isRepresentNumber(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		boolean foundDot = false;
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c == '.') {
				if (foundDot) {
					return false;
				}
				foundDot = true;
			} else if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	private static final String[] REGEX_SPECIAL_CHARS = new String[] { "\\", ".", "*", "+", "-", "[", "]", "(", ")",
			"$", "^", "|", "{", "}", "?" };

	public static final String normalizeForRegex(String key) {
		String result = key;
		for (String c : REGEX_SPECIAL_CHARS) {
			result = result.replaceAll("\\" + c, "\\\\\\" + c);
		}
		return result;
	}

	/**
	 * Transform using GlobalFormatTransformerRegistry singleton instance
	 * 
	 * @param source
	 * @param args
	 * @return
	 */
	public static String transform(String source, Object args) {
		return transform(source, args, GlobalFormatTransformerRegistry.getInstance());
	}

	public static String transform(String source, Object args, FormatTransformerRegistry transformerRegistry) {
		if (source != null) {
			if (transformerRegistry == null) {
				return format(source, args, null);
			}

			Pattern pattern = Pattern.compile("\\{\\{([^\\{\\}]+)\\}\\}");
			Matcher matcher = pattern.matcher(source);
			List<String[]> matchedGroups = new LinkedList<>();
			while (matcher.find()) {
				matchedGroups.add(new String[] { matcher.group(0), matcher.group(1) });
			}

			if (matchedGroups.isEmpty()) {
				return source;
			}

			String result = source;
			for (String[] matchedGroup : matchedGroups) {
				final String key = matchedGroup[0];
				String argName = matchedGroup[1];

				String[] arr = argName.trim().split("\\s*>\\s*");

				Object value = ObjectUtils.getValueByPath(args, arr[0]);
				if (value != null) {
					if (arr.length > 1) {
						String[] transformerNames = Arrays.copyOfRange(arr, 1, arr.length);
						List<FormatTransformer> chain = transformerRegistry.getChain(transformerNames);
						for (FormatTransformer transformer : chain) {
							value = transformer.transform(value);
						}
					}
					result = result.replaceAll(normalizeForRegex(key), PrimitiveTypeUtils.getStringValueFrom(value));
				}
			}

			return result;
		}
		return null;
	}

	public static String format(String pattern, Object args, StringFormatOption option) {
		List<String> matches = getAllMatches(pattern, "\\{\\{[a-zA-Z0-9_]+\\}\\}");
		Set<String> keys = new HashSet<>();
		for (String matche : matches) {
			keys.add(matche.substring(2, matche.length() - 2));
		}
		String result = new String(pattern);
		for (String key : keys) {
			Object value = ObjectUtils.getValueByPath(args, key);
			if (value instanceof Number && option != null && option.isAutoFormatNumber()) {
				value = option.getDecimalFormat().format(value);
			}
			try {
				String valueString = PrimitiveTypeUtils.getStringValueFrom(value);
				result = result.replaceAll("\\{\\{" + key + "\\}\\}", valueString);
			} catch (Exception ex) {
				log.error("Error while inject value for key: `{}`, value={}", key, value);
				throw ex;
			}
		}
		return result;
	}

	@Data
	static class TestVO {
		private String name;
		private int age;
	}

	public static void main(String[] args) {
		// String pattern = "Name: {{name}} -> age: {{age}}";
		// TestVO obj = new TestVO();
		// obj.setName("Bách Hoàng Nguyễn");
		// obj.setAge(28);
		// System.out.println("Formatted string: "
		// + format(pattern, obj,
		// StringFormatOption.builder().autoFormatNumber(true).build()));
		//
		// Map<String, Object> map = new HashMap<>();
		// map.put("name", "abc");
		// map.put("age", 280029340);
		//
		// System.out.println("Formatted string: "
		// + format(pattern, map,
		// StringFormatOption.builder().autoFormatNumber(true).build()));
	}
}
