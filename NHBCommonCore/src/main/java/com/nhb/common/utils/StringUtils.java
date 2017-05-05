package com.nhb.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.RandomStringUtils;

import com.google.common.base.CaseFormat;

public final class StringUtils {

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

	public static void main(String[] args) {
		// System.out.println(getAllMatches("9c10d",
		// "[123456789(10)jqkat][hdcs]"));
		System.out.println(isRepresentNumber("123"));
	}
}
