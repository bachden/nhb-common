package com.nhb.common.predicate.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhb.common.predicate.FilteredObject;
import com.nhb.common.predicate.NumberValues;
import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.PredicateBuilder;
import com.nhb.common.predicate.Predicates;
import com.nhb.common.predicate.numeric.Equals;
import com.nhb.common.predicate.numeric.GreaterOrEquals;
import com.nhb.common.predicate.numeric.GreaterThan;
import com.nhb.common.predicate.numeric.LessOrEquals;
import com.nhb.common.predicate.numeric.LessThan;
import com.nhb.common.predicate.numeric.NotEquals;
import com.nhb.common.predicate.object.getter.NumberAttributeGetter;
import com.nhb.common.predicate.object.getter.StringAttributeGetter;
import com.nhb.common.predicate.predefined.FalsePredicate;
import com.nhb.common.predicate.text.Exactly;
import com.nhb.common.predicate.text.NotExactly;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.StringValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;
import com.nhb.common.predicate.value.primitive.RawStringValue;
import com.nhb.common.utils.Initializer;
import com.nhb.common.utils.StringUtils;

public class SqlPredicateParser {

	private static final Logger logger = LoggerFactory.getLogger(SqlPredicateParser.class);

	private static final char ESCAPE = '\\';
	private static final char APOSTROPHE = '\'';

	private static final char OPENING_PARENTHESES = '(';
	private static final char CLOSING_PARENTHESES = ')';
	private static final char COMMA = ',';

	private static final String ADD = "+";
	private static final String MULTIPLY = "*";
	private static final String SUBTRACT = "-";
	private static final String DIVIDE = "/";
	private static final String MOD = "%";
	private static final String POW = "^";
	private static final String SQRT = "sqrt";

	private static final Set<String> MATH_OPERATORS = new HashSet<>();
	static {
		MATH_OPERATORS.add(ADD);
		MATH_OPERATORS.add(SUBTRACT);
		MATH_OPERATORS.add(MULTIPLY);
		MATH_OPERATORS.add(DIVIDE);
		MATH_OPERATORS.add(MOD);
		MATH_OPERATORS.add(POW);
		MATH_OPERATORS.add(SQRT);
	}

	private static final String AND = "and";
	private static final String OR = "or";
	private static final String NOT = "not";

	private static final Set<String> LOGIC_OPERATORS = new HashSet<>();
	static {
		LOGIC_OPERATORS.add(AND);
		LOGIC_OPERATORS.add(OR);
		LOGIC_OPERATORS.add(NOT);
	}

	private static final String IS_NULL = "is_null";
	private static final String IS_NOT_NULL = "is_not_null";

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
				.replaceAll("(?i)not in", NOT_IN) //
				.replaceAll("(?i)not like", NOT_LIKE) //
				.replaceAll("(?i)not between", NOT_BETWEEN) //
				.replaceAll("(?i)is null", IS_NULL) //
				.replaceAll("(?i)is not null", IS_NOT_NULL);

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
							} else if (obj2 instanceof String) {
								stack.push(Predicates.and((Predicate) obj1, entity.is((String) obj2).build()));
							} else {
								throw new SqlPredicateSyntaxException("Syntax error near AND operator");
							}
						} else if (obj1 instanceof String) {
							if (obj2 instanceof Predicate) {
								stack.push(Predicates.and(entity.is((String) obj1).build(), (Predicate) obj2));
							} else {
								stack.push(Predicates.and(entity.is((String) obj1).build(),
										entity.is((String) obj2).build()));
							}
						} else {
							throw new SqlPredicateSyntaxException("Syntax error near AND operator");
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
						if (attribute instanceof String) {
							if (StringUtils.isRepresentNumber((String) attribute)) {
								stack.push(Predicates.between(new RawNumberValue(Double.valueOf((String) attribute)),
										Double.valueOf((String) lower), Double.valueOf((String) upper), true, true));
							} else {
								stack.push(Predicates.between((String) attribute, Double.valueOf((String) lower),
										Double.valueOf((String) upper), true, true));
							}
						} else {
							throw new SqlPredicateSyntaxException("Syntax error near " + BETWEEN);
						}
						break;
					}
					case NOT_BETWEEN: {
						Object upper = stack.pop();
						Object lower = stack.pop();
						Object value = stack.pop();
						if (value instanceof String) {
							if (StringUtils.isRepresentNumber((String) value)) {
								stack.push(Predicates.notBetween(new RawNumberValue(Double.valueOf((String) value)),
										Double.valueOf((String) lower), Double.valueOf((String) upper), true, true));
							} else {
								stack.push(Predicates.notBetween((String) value, Double.valueOf((String) lower),
										Double.valueOf((String) upper), true, true));
							}
						} else {
							throw new SqlPredicateSyntaxException("Syntax error near " + BETWEEN);
						}
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
						Object obj = stack.pop();
						if (obj instanceof NumberValue) {
							stack.push(Predicates.in((NumberValue) obj, list));
						} else if (obj instanceof Number) {
							stack.push(Predicates.in((Number) obj, list));
						} else if (obj instanceof String) {
							stack.push(entity.get((String) obj).in(list).build());
						}
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
						Object obj = stack.pop();
						if (obj instanceof NumberValue) {
							stack.push(Predicates.notIn((NumberValue) obj, list));
						} else if (obj instanceof Number) {
							stack.push(Predicates.in((Number) obj, list));
						} else if (obj instanceof String) {
							stack.push(entity.get((String) obj).in(list).build());
						}
						break;
					}
					case IS_NULL: {
						Object entry = stack.pop();
						if (entry instanceof String) {
							stack.push(Predicates.isNull((String) entry));
						} else {
							throw new SqlPredicateSyntaxException("Syntax error for 'is null' operator");
						}
						break;
					}
					case IS_NOT_NULL: {
						Object entry = stack.pop();
						if (entry instanceof String) {
							stack.push(Predicates.notNull((String) entry));
						} else {
							throw new SqlPredicateSyntaxException("Syntax error for 'is not null' operator");
						}
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
						stack.push(genEqualsPredicate(obj1, obj2, params));
						break;
					}
					case NOT_EQUALS: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						stack.push(genNotEqualsPredicate(obj1, obj2, params));
						break;
					}
					case GREATER_THAN: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						NumberValue[] values = genNumberValues(obj1, obj2);
						stack.push(new GreaterThan(values[0], values[1]));
						break;
					}
					case LESS_THAN: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						NumberValue[] values = genNumberValues(obj1, obj2);
						stack.push(new LessThan(values[0], values[1]));
						break;
					}
					case GREATER_OR_EQUALS: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						NumberValue[] values = genNumberValues(obj1, obj2);
						stack.push(new GreaterOrEquals(values[0], values[1]));
						break;
					}
					case LESS_OR_EQUALS: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						NumberValue[] values = genNumberValues(obj1, obj2);
						stack.push(new LessOrEquals(values[0], values[1]));
						break;
					}
					case ADD: {
						Object value2 = stack.pop();
						Object value1 = stack.pop();
						stack.push(NumberValues.add(value1, value2));
						break;
					}
					case SUBTRACT: {
						Object value2 = stack.pop();
						Object value1 = stack.pop();
						stack.push(NumberValues.subtract(value1, value2));
						break;
					}
					case MULTIPLY: {
						Object value2 = stack.pop();
						Object value1 = stack.pop();
						stack.push(NumberValues.multiply(value1, value2));
						break;
					}
					case DIVIDE: {
						Object value2 = stack.pop();
						Object value1 = stack.pop();
						stack.push(NumberValues.divide(value1, value2));
						break;
					}
					case MOD: {
						Object value2 = stack.pop();
						Object value1 = stack.pop();
						stack.push(NumberValues.mod(value1, value2));
						break;
					}
					case POW: {
						Object value2 = stack.pop();
						Object value1 = stack.pop();
						stack.push(NumberValues.pow(value1, value2));
						break;
					}
					case SQRT: {
						Object value = stack.pop();
						stack.push(NumberValues.sqrt(value));
						break;
					}
					}
					logger.debug("Operator [" + token.toUpperCase() + "] --> " + stack.peek());
				}
			}
			return (Predicate) stack.pop();
		}
		return null;

	}

	private static Predicate genEqualsPredicate(Object obj1, Object obj2, List<String> params) {

		if (obj1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj1)) {
				obj1 = new RawNumberValue(Double.valueOf((String) obj1));
			} else if (((String) obj1).startsWith("$")) {
				int paramId = Integer.valueOf(((String) obj1).substring(1));
				obj1 = new RawStringValue(params.get(paramId));
			} else {
				obj1 = new StringAttributeGetter((String) obj1);
			}
		} else {
			throw new SqlPredicateSyntaxException("Syntax error near = operator");
		}

		Predicate predicate = new FalsePredicate();
		if (obj2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj2)) {
				obj2 = new RawNumberValue(Double.valueOf((String) obj2));
				if (obj1 instanceof NumberValue) {
					predicate = new Equals((NumberValue) obj1, (NumberValue) obj2);
				}
			} else if (((String) obj2).startsWith("$")) {
				int paramId = Integer.valueOf(((String) obj2).substring(1));
				obj2 = new RawStringValue(params.get(paramId));
				if (obj1 instanceof StringValue) {
					predicate = new Exactly((StringValue) obj1, (StringValue) obj2);
				}
			} else {
				obj2 = new StringAttributeGetter((String) obj2);
				if (obj1 instanceof StringValue) {
					predicate = new Exactly((StringValue) obj1, (StringValue) obj2);
				}
			}
		} else {
			throw new SqlPredicateSyntaxException("Syntax error near = operator");
		}
		return predicate;
	}

	private static NumberValue[] genNumberValues(Object obj1, Object obj2) {
		if (obj1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj1)) {
				obj1 = new RawNumberValue(Double.valueOf((String) obj1));
			} else {
				obj1 = new NumberAttributeGetter((String) obj1);
			}
		}

		if (obj2 instanceof String) {
			obj2 = new RawNumberValue(Double.valueOf((String) obj2));
		} else {
			obj2 = new NumberAttributeGetter((String) obj1);
		}

		if (!(obj1 instanceof NumberValue) || !(obj2 instanceof NumberValue)) {
			throw new SqlPredicateSyntaxException("Syntax error near >= operator");
		}
		return new NumberValue[] { (NumberValue) obj1, (NumberValue) obj2 };
	}

	private static Predicate genNotEqualsPredicate(Object obj1, Object obj2, List<String> params) {

		if (obj1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj1)) {
				obj1 = new RawNumberValue(Double.valueOf((String) obj1));
			} else if (((String) obj1).startsWith("$")) {
				int paramId = Integer.valueOf(((String) obj1).substring(1));
				obj1 = new RawStringValue(params.get(paramId));
			} else {
				obj1 = new StringAttributeGetter((String) obj1);
			}
		} else if (!(obj1 instanceof NumberValue) && !(obj1 instanceof StringValue)) {
			throw new SqlPredicateSyntaxException("Syntax error near != operator");
		}

		Predicate predicate = new FalsePredicate();
		if (obj2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj2)) {
				obj2 = new RawNumberValue(Double.valueOf((String) obj2));
				if (obj1 instanceof NumberValue) {
					predicate = new NotEquals((NumberValue) obj1, (NumberValue) obj2);
				}
			} else if (((String) obj2).startsWith("$")) {
				int paramId = Integer.valueOf(((String) obj2).substring(1));
				obj2 = new RawStringValue(params.get(paramId));
				if (obj1 instanceof StringValue) {
					predicate = new NotExactly((StringValue) obj1, (StringValue) obj2);
				}
			} else {
				obj2 = new StringAttributeGetter((String) obj2);
				if (obj1 instanceof StringValue) {
					predicate = new NotExactly((StringValue) obj1, (StringValue) obj2);
				}
			}
		} else {
			throw new SqlPredicateSyntaxException("Syntax error near != operator");
		}
		return predicate;
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
				|| token.equalsIgnoreCase(IS_NULL) //
				|| token.equalsIgnoreCase(IS_NOT_NULL) //
				|| MATH_OPERATORS.contains(token.toLowerCase())//
				|| LOGIC_OPERATORS.contains(token.toLowerCase()) //
				|| EQUALITY_OPERATORS.contains(token.toLowerCase());
	}

	private static int getOperatorPrecedence(String operator) {
		if (MATH_OPERATORS.contains(operator)) {
			if (operator.equalsIgnoreCase(POW) || operator.equalsIgnoreCase(SQRT)) {
				return 5;
			} else if (operator.equalsIgnoreCase(MULTIPLY) || operator.equalsIgnoreCase(DIVIDE)
					|| operator.equalsIgnoreCase(MOD)) {
				return 4;
			}
			return 3;
		} else if (operator.equalsIgnoreCase(NOT)) {
			return 2;
		} else if (LOGIC_OPERATORS.contains(operator.toLowerCase())) {
			return 1;
		} else if (EQUALITY_OPERATORS.contains(operator.toLowerCase())) {
			return 2;
		} else if (operator.equalsIgnoreCase(LIKE) || operator.equalsIgnoreCase(NOT_LIKE)) {
			return 2;
		} else if (operator.equalsIgnoreCase(IN) || operator.equalsIgnoreCase(NOT_IN)) {
			return 2;
		} else if (operator.equalsIgnoreCase(IS_NULL) || operator.equalsIgnoreCase(IS_NOT_NULL)) {
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
				System.out.println("checking token " + token);
				List<String> localTokens = new ArrayList<>();
				int lastIndex = 0;
				char[] chars = token.toCharArray();

				if (token.equalsIgnoreCase("4")) {
					System.out.println("just for test");
				}

				for (int i = 0; i < chars.length; i++) {
					char c = chars[i];
					if (c == OPENING_PARENTHESES || c == CLOSING_PARENTHESES || c == COMMA) {
						if (lastIndex < i) {
							localTokens.add(token.substring(lastIndex, i));
						}
						if (c != COMMA) {
							localTokens.add(String.valueOf(c));
						}
						lastIndex = i + 1;
					}
				}

				if (lastIndex < chars.length - 1) {
					localTokens.add(token.substring(lastIndex, chars.length));
				} else if (isOperator(token)) {
					localTokens.add(token);
				} else if (localTokens.size() == 0) {
					results.add(token);
				}

				for (String str : localTokens) {
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
