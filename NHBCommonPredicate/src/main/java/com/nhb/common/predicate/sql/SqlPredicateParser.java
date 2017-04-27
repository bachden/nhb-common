package com.nhb.common.predicate.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.predicate.FilteredObject;
import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.PredicateBuilder;
import com.nhb.common.predicate.Predicates;
import com.nhb.common.utils.Initializer;

public class SqlPredicateParser {

	private static final Logger logger = LoggerFactory.getLogger(SqlPredicateParser.class);

	private static final char ESCAPE = '\\';
	private static final char APOSTROPHE = '\'';

	private static final char OPENING_PARENTHESES = '(';
	private static final char CLOSING_PARENTHESES = ')';
	private static final char COMMA = ',';

	private static final String AND = "and";
	private static final String OR = "or";
	private static final String NOT = "not";

	private static final List<String> LOGIC_OPERATORS = new ArrayList<>();
	static {
		LOGIC_OPERATORS.add(AND);
		LOGIC_OPERATORS.add(OR);
		LOGIC_OPERATORS.add(NOT);
	}

	private static final String IN = "in";
	private static final String NOT_IN = "not_in";

	private static final String BETWEEN = "between";
	private static final String NOT_BETWEEN = "not_between";

	private static final String LIKE = "like";
	private static final String NOT_LIKE = "not_like";

	private static final String EQUALS = "=";
	private static final String NOT_EQUALS = "!=";
	private static final String GREATER_THAN = ">";
	private static final String LESS_THAN = "<";
	private static final String GREATER_OR_EQUALS = ">=";
	private static final String LESS_OR_EQUALS = "<=";

	private static final List<String> EQUALITY_OPERATORS = new ArrayList<>();
	static {
		EQUALITY_OPERATORS.add(EQUALS);
		EQUALITY_OPERATORS.add(GREATER_THAN);
		EQUALITY_OPERATORS.add(NOT_EQUALS);
		EQUALITY_OPERATORS.add(GREATER_OR_EQUALS);
		EQUALITY_OPERATORS.add(LESS_THAN);
		EQUALITY_OPERATORS.add(LESS_OR_EQUALS);

		Collections.sort(EQUALITY_OPERATORS, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if (o1.length() == o2.length()) {
					return 0;
				}
				return o1.length() < o2.length() ? 1 : -1;
			}
		});
	}

	public static void main(String[] args) {
		Initializer.bootstrap(SqlPredicateParser.class);
		// String sql = "gender='female' and (age not between 16 and 25 or
		// (age>=35 or age<13) and salary not in (1000, 2000, 3000, 4000)) or
		// name like 'Mc \\'Oco\\'nner'";
		String sql = "gender = 'female' and not (age between 16.0 and 25.0 or age >= 35.0 or age <= 13.0 and salary not in (1000.0, 2000.0, 3000.0, 4000.0) or name like 'Mc \\'Oco\\'nner' and money = 100)";
		logger.debug("{}", parse(sql));
	}

	public static Predicate parse(String sql) {
		if (sql == null) {
			return null;
		}
		logger.debug("Input string: " + sql);

		List<String> extracted = extractString(sql);
		logger.debug("extracted: " + extracted);
		String sql1 = removeUnnecessarySpaces(extracted.get(0))//
				.replaceAll("not in", NOT_IN) //
				.replaceAll("not like", NOT_LIKE) //
				.replaceAll("not between", NOT_BETWEEN);

		logger.debug("SQL: " + sql1);
		logger.debug("Params: ");
		for (int i = 1; i < extracted.size(); i++) {
			logger.debug("\t$" + i + ": " + extracted.get(i));
		}
		List<String> tokens = split(sql1);
		logger.debug("Tokens before normalize: " + tokens);
		normalize(tokens);
		logger.debug("Tokens: " + tokens);

		List<String> prefixTokens = toPrefix(tokens);
		logger.debug("Tokens in prefix mode: " + prefixTokens);

		return toPredicate(prefixTokens, extracted);
	}

	private static Predicate toPredicate(List<String> prefixTokens, List<String> params) {
		if (prefixTokens != null) {
			Stack<Object> stack = new Stack<>();
			while (prefixTokens.size() > 0) {
				String token = prefixTokens.remove(0);
				if (!isOperator(token)) {
					stack.push(token);
				} else {
					FilteredObject entity = PredicateBuilder.newFilteredObject();
					switch (token) {
					case AND: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						if (obj1 instanceof Predicate) {
							if (obj2 instanceof Predicate) {
								stack.push(Predicates.and((Predicate) obj1, (Predicate) obj2));
							} else {
								stack.push(Predicates.and((Predicate) obj1, entity.is((String) obj2).build()));
							}
						} else {
							if (obj2 instanceof Predicate) {
								stack.push(Predicates.and(entity.is((String) obj1).build(), (Predicate) obj2));
							} else {
								stack.push(Predicates.and(entity.is((String) obj1).build(),
										entity.is((String) obj2).build()));
							}
						}
						break;
					}
					case OR: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						if (obj1 instanceof Predicate) {
							if (obj2 instanceof Predicate) {
								stack.push(Predicates.or((Predicate) obj1, (Predicate) obj2));
							} else {
								stack.push(Predicates.or((Predicate) obj1, entity.is((String) obj2).build()));
							}
						} else {
							if (obj2 instanceof Predicate) {
								stack.push(Predicates.or(entity.is((String) obj1).build(), (Predicate) obj2));
							} else {
								stack.push(Predicates.or(entity.is((String) obj1).build(),
										entity.is((String) obj2).build()));
							}
						}
						break;
					}
					case NOT: {
						Object entry = stack.pop();
						if (entry instanceof Predicate) {
							Predicate predicate = (Predicate) entry;
							stack.push(Predicates.not(predicate));
						} else {
							stack.push(Predicates.not((String) entry));
						}
						break;
					}
					case BETWEEN: {
						Object upper = stack.pop();
						Object lower = stack.pop();
						Object attribute = stack.pop();
						stack.push(Predicates.between((String) attribute, Double.valueOf((String) lower),
								Double.valueOf((String) upper), true, true));
						break;
					}
					case NOT_BETWEEN: {
						Object upper = stack.pop();
						Object lower = stack.pop();
						Object attribute = stack.pop();
						stack.push(Predicates.notBetween((String) attribute, Double.valueOf((String) lower),
								Double.valueOf((String) upper), true, true));
						break;
					}
					case IN: {
						Object size = stack.pop();
						List<Object> list = new ArrayList<>();
						for (int i = 0; i < Integer.valueOf((String) size); i++) {
							String entry = (String) stack.pop();
							if (entry.startsWith("$")) {
								list.add(0, params.get(Integer.valueOf(entry.substring(1))));
							} else {
								list.add(0, Double.valueOf(entry));
							}
						}
						String attribute = (String) stack.pop();
						stack.push(entity.get(attribute).in(list).build());
						break;
					}
					case NOT_IN: {
						Object size = stack.pop();
						List<Object> list = new ArrayList<>();
						for (int i = 0; i < Integer.valueOf((String) size); i++) {
							String entry = (String) stack.pop();
							if (entry.startsWith("$")) {
								list.add(0, params.get(Integer.valueOf(entry.substring(1))));
							} else {
								list.add(0, Double.valueOf(entry));
							}
						}
						String attribute = (String) stack.pop();
						stack.push(entity.get(attribute).notIn(list).build());
						break;
					}
					case LIKE: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						stack.push(entity.get((String) obj1)
								.match((String) params.get(Integer.valueOf(((String) obj2).substring(1)))).build());
						break;
					}
					case NOT_LIKE: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						stack.push(entity.get((String) obj1)
								.notMatch((String) params.get(Integer.valueOf(((String) obj2).substring(1)))).build());
						break;
					}
					case EQUALS: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						if (obj2 instanceof String && ((String) obj2).startsWith("$")) {
							int paramId = Integer.valueOf(((String) obj2).substring(1));
							stack.push(entity.get((String) obj1).exactly(params.get(paramId)).build());
						} else {
							stack.push(entity.get((String) obj1).equal(Double.valueOf((String) obj2)).build());
						}
						break;
					}
					case NOT_EQUALS: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						if (obj2 instanceof String && ((String) obj2).startsWith("$")) {
							int paramId = Integer.valueOf(((String) obj2).substring(1));
							stack.push(Predicates.not(entity.get((String) obj1).exactly(params.get(paramId))).build());
						} else {
							stack.push(entity.get((String) obj1).notEqual(Double.valueOf((String) obj2)).build());
						}
						break;
					}
					case GREATER_THAN: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						stack.push(entity.get((String) obj1).greaterThan(Double.valueOf((String) obj2)).build());
						break;
					}
					case LESS_THAN: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						stack.push(entity.get((String) obj1).lessThan(Double.valueOf((String) obj2)).build());
						break;
					}
					case GREATER_OR_EQUALS: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						PredicateBuilder pb = entity.get((String) obj1).greaterOrEquals(Double.valueOf((String) obj2));
						Predicate predicate = pb.build();
						stack.push(predicate);
						break;
					}
					case LESS_OR_EQUALS: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						stack.push(entity.get((String) obj1).lessOrEquals(Double.valueOf((String) obj2)).build());
						break;
					}
					}
					logger.debug("Operator [" + token.toUpperCase() + "] --> predicate: " + stack.peek());
				}
			}
			return (Predicate) stack.pop();
		}
		return null;
	}

	private static boolean isOperator(String token) {
		if (token == null) {
			return false;
		}
		return token.equalsIgnoreCase(LIKE) //
				|| token.equalsIgnoreCase(NOT_LIKE) //
				|| token.equalsIgnoreCase(BETWEEN) //
				|| token.equalsIgnoreCase(NOT_BETWEEN) //
				|| token.equalsIgnoreCase(IN) //
				|| token.equalsIgnoreCase(NOT_IN) //
				|| LOGIC_OPERATORS.contains(token.toLowerCase()) //
				|| EQUALITY_OPERATORS.contains(token.toLowerCase());
	}

	private static int getOperatorPrecedence(String operator) {
		if (operator.equalsIgnoreCase(NOT)) {
			return 2;
		} else if (LOGIC_OPERATORS.contains(operator.toLowerCase())) {
			return 1;
		} else if (EQUALITY_OPERATORS.contains(operator.toLowerCase())) {
			return 2;
		} else if (operator.equalsIgnoreCase(LIKE) || operator.equalsIgnoreCase(NOT_LIKE)) {
			return 2;
		} else if (operator.equalsIgnoreCase(IN) || operator.equalsIgnoreCase(NOT_IN)) {
			return 2;
		} else if (operator.equalsIgnoreCase(BETWEEN) || operator.equalsIgnoreCase(NOT_BETWEEN)) {
			return 2;
		}
		throw new SqlPredicateInvalidOperatorException();
	}

	private static List<String> toPrefix(List<String> tokens) {
		if (tokens != null) {
			Stack<String> stack = new Stack<>();
			Stack<String> output = new Stack<>();
			for (String token : tokens) {
				logger.debug("**** " + token + " ****");
				if (token.equals("(")) {
					logger.debug("Found openParentheses, push to stack");
					stack.push(token);
				} else if (token.equals(")")) {
					logger.debug("Found closing parentheses, pop all from stack until found open");
					if (stack.size() > 0) {
						String stackHead = stack.pop();
						while (!stackHead.equals("(")) {
							logger.debug("Pop " + stackHead + " from stack --> add to output");
							output.push(stackHead);
							stackHead = stack.pop();
						}
					}
				} else if (!isOperator(token)) {
					logger.debug("Found operand " + token + " --> push to output");
					output.push(token);
				} else {
					logger.debug("Found operator " + token + ", checking stack head");
					String stackHead = stack.size() > 0 ? stack.peek() : null;
					if (isOperator(stackHead)) {
						logger.debug("Stack head (" + stackHead + ") is operator, checking precedence...");
						if (getOperatorPrecedence(stackHead) >= getOperatorPrecedence(token)) {
							stackHead = stack.pop();
							logger.debug("Stack head has higher (or equals) precedence, pop stack (" + stackHead
									+ ") and push to output, put new token (" + token + ") to stack");
							output.push(stackHead);
							stack.push(token);
						} else {
							logger.debug("Stack head has lower precedence, push token (" + token + ") to stack...");
							stack.push(token);
						}
					} else {
						logger.debug(
								"Stack head (" + stackHead + ") is not a operator, push token " + token + " to stack");
						stack.push(token);
					}
				}
				logger.debug("\tStack: " + stack);
				logger.debug("\tOutput: " + output);
			}
			while (stack.size() > 0) {
				output.push(stack.pop());
			}
			return output;
		}
		return null;
	}

	private static void normalize(List<String> tokens) {
		// remove "and" tokens if found "between" operator
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equalsIgnoreCase(BETWEEN) || tokens.get(i).equalsIgnoreCase(NOT_BETWEEN)) {
				if (tokens.get(i + 2).equalsIgnoreCase(AND)) {
					tokens.remove(i + 2);
				}
			}
		}

		// find "in" operator, insert size value for list to check
		List<Integer> inIndexes = new ArrayList<>();
		Map<Integer, Integer> inToSizeMap = new HashMap<>();
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equalsIgnoreCase(IN) || tokens.get(i).equalsIgnoreCase(NOT_IN)) {
				if (!tokens.get(i + 1).equals("(")) {
					throw new SqlPredicateSyntaxException();
				}
				inIndexes.add(i);
				for (int j = i + 2; j < tokens.size(); j++) {
					if (tokens.get(j).equals(")")) {
						inToSizeMap.put(i, j - i - 2);
						break;
					}
				}
			}
		}
		for (int i = inIndexes.size() - 1; i >= 0; i--) {
			int index = inIndexes.get(i);
			int listSize = inToSizeMap.get(index);
			tokens.add(index + listSize + 3, String.valueOf(listSize));
		}
	}

	private static List<String> split(String sql) {

		if (sql != null) {
			String[] splittedBySpace = sql.split(" ");
			List<String> results = new ArrayList<>();
			for (String token : splittedBySpace) {
				List<String> tokens = new ArrayList<>();
				int lastIndex = 0;
				char[] chars = token.toCharArray();

				for (int i = 0; i < chars.length; i++) {
					char c = chars[i];
					if (c == OPENING_PARENTHESES || c == CLOSING_PARENTHESES || c == COMMA) {
						if (lastIndex < i) {
							tokens.add(token.substring(lastIndex, i));
						}
						if (c != COMMA) {
							tokens.add(String.valueOf(c));
						}
						lastIndex = i + 1;
					}
				}

				if (lastIndex < chars.length - 1) {
					tokens.add(token.substring(lastIndex, chars.length));
				} else if (isOperator(token)) {
					tokens.add(token);
				}

				for (String str : tokens) {
					boolean hasOperator = false;
					for (String operator : EQUALITY_OPERATORS) {
						int index = str.indexOf(operator);
						if (index >= 0) {
							hasOperator = true;
							do {
								String preStr = str.substring(0, index).trim();
								if (preStr.length() > 0) {
									results.add(preStr);
								}
								results.add(operator);
								str = str.substring(index + operator.length()).trim();
								index = str.indexOf(operator);
							} while (index >= 0);
							if (str.length() > 0) {
								results.add(str);
							}
						}
					}
					if (!hasOperator) {
						results.add(str);
					}
				}
			}
			return results;
		}
		return null;
	}

	private static String removeUnnecessarySpaces(String input) {
		return input.replaceAll("\\s+", " ");
	}

	private static List<String> extractString(String sql) {
		char[] chars = sql.toCharArray();
		List<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == APOSTROPHE && (i > 0 && chars[i - 1] != ESCAPE)) {
				indexes.add(i);
			}
		}
		if (indexes.size() % 2 != 0) {
			throw new SqlPredicateSyntaxException();
		}

		StringBuilder sb = new StringBuilder();
		List<String> params = new ArrayList<>();
		if (indexes.size() > 0) {
			if (indexes.get(0) > 0) {
				String substring = sql.substring(0, indexes.get(0));
				sb.append(substring);
			}
			for (int i = 0; i < indexes.size(); i += 2) {
				int startIndex = indexes.get(i);
				int endIndex = indexes.get(i + 1);
				String subStr = sql.substring(startIndex + 1, endIndex);
				params.add(subStr.replaceAll("\\\\'", "'"));
				String param = "$" + (i / 2 + 1);
				sb.append(param);
				if (i < indexes.size() - 2) {
					sb.append(sql.substring(endIndex + 1, indexes.get(i + 2)));
				}
			}

			sb.append(sql.substring(indexes.get(indexes.size() - 1) + 1, sql.length()));
		} else {
			sb.append(sql);
		}

		List<String> result = new ArrayList<>();
		result.add(sb.toString());
		result.addAll(params);
		return result;
	}
}
