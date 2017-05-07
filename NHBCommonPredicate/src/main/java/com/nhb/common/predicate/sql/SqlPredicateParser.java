package com.nhb.common.predicate.sql;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.nhb.common.predicate.NumberValues;
import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.Predicates;
import com.nhb.common.predicate.numeric.Equals;
import com.nhb.common.predicate.numeric.GreaterOrEquals;
import com.nhb.common.predicate.numeric.GreaterThan;
import com.nhb.common.predicate.numeric.LessOrEquals;
import com.nhb.common.predicate.numeric.LessThan;
import com.nhb.common.predicate.numeric.NotEquals;
import com.nhb.common.predicate.object.getter.BooleanAttributeGetterValue;
import com.nhb.common.predicate.object.getter.CollectionAttributeGetterValue;
import com.nhb.common.predicate.object.getter.NumberAttributeGetterValue;
import com.nhb.common.predicate.object.getter.PointerAttributeGetterValue;
import com.nhb.common.predicate.object.getter.StringAttributeGetterValue;
import com.nhb.common.predicate.predefined.FalsePredicate;
import com.nhb.common.predicate.sql.exception.SqlPredicateInvalidOperatorException;
import com.nhb.common.predicate.sql.exception.SqlPredicateSyntaxException;
import com.nhb.common.predicate.text.Exactly;
import com.nhb.common.predicate.text.Match;
import com.nhb.common.predicate.text.NotExactly;
import com.nhb.common.predicate.text.NotMatch;
import com.nhb.common.predicate.value.BooleanValue;
import com.nhb.common.predicate.value.CollectionValue;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.StringValue;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.predicate.value.primitive.RawCollectionValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;
import com.nhb.common.predicate.value.primitive.RawStringValue;
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
	}

	private static final List<String> SYMBOLIZE_OPERATORS = new ArrayList<>();
	static {
		SYMBOLIZE_OPERATORS.addAll(EQUALITY_OPERATORS);
		SYMBOLIZE_OPERATORS.addAll(MATH_OPERATORS);

		Collections.sort(SYMBOLIZE_OPERATORS, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if (o1.length() == o2.length()) {
					return 0;
				}
				return o1.length() < o2.length() ? 1 : -1;
			}
		});
	}

	public static Predicate parse(String sql) {
		if (sql == null) {
			return null;
		}
		// logger.debug("Input string: " + sql);

		// TimeWatcher timeWatcher = new TimeWatcher();
		// timeWatcher.reset();

		List<String> extracted = extractString(sql);
		// logger.debug("Time to extract string: {}ms",
		// timeWatcher.endLapMillis());

		String sql1 = removeUnnecessarySpaces(extracted.get(0));
		// logger.debug("Time to remove unnecessary spaces: {}ms",
		// timeWatcher.endLapMillis());

		List<String> tokens = split(sql1);
		// logger.debug("Time to split: {}ms", timeWatcher.endLapMillis());

		normalize(tokens);
		logger.debug("Tokens after normalized: {}", tokens);

		List<String> prefixTokens = toPrefix(tokens);
		// logger.debug("Time to convert tokens to prefix: {}ms",
		// timeWatcher.endLapMillis());

		Predicate predicate = toPredicate(prefixTokens, extracted);
		// logger.debug("Time to convert prefix to predicate: {}ms",
		// timeWatcher.endLapMillis());

		return predicate;
	}

	private static Predicate toPredicate(List<String> prefixTokens, List<String> params) {
		if (prefixTokens != null) {
			logger.debug("------------- PREPARE PREDICATE -------------");
			Stack<Object> stack = new Stack<>();
			while (prefixTokens.size() > 0) {
				String token = prefixTokens.remove(0);
				if (!isOperator(token)) {
					stack.push(token);
				} else {
					// FilteredObject entity =
					// PredicateBuilder.newFilteredObject();
					switch (token) {
					case AND: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						Predicate predicate = genAndPredicate(obj2, obj1);
						stack.push(predicate);
						break;
					}
					case OR: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();
						Predicate predicate = genOrPredicate(obj2, obj1);
						stack.push(predicate);
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
						Object value = stack.pop();
						Predicate predicate = genBetweenPredicate(upper, lower, value);
						stack.push(predicate);
						break;
					}
					case NOT_BETWEEN: {
						Object upper = stack.pop();
						Object lower = stack.pop();
						Object value = stack.pop();
						Predicate predicate = generateNotBetweenPredicate(upper, lower, value);
						stack.push(predicate);
						break;
					}
					case IN: {
						Object size = stack.pop();
						Predicate predicate = null;
						CollectionValue collectionValue = null;
						if (size instanceof String) {
							String sizeStr = (String) size;
							if (sizeStr.equalsIgnoreCase("1*")) {
								collectionValue = new CollectionAttributeGetterValue((String) stack.pop());
							} else {
								List<Object> collection = new ArrayList<>();
								for (int i = 0; i < Integer.valueOf((String) size); i++) {
									String entry = (String) stack.pop();
									if (entry.startsWith("$")) {
										collection.add(0, params.get(Integer.valueOf(entry.substring(1))));
									} else if (StringUtils.isRepresentNumber(entry)) {
										collection.add(0, Double.valueOf(entry));
									} else {
										collection.add(0, new PointerAttributeGetterValue(entry));
									}
								}
								collectionValue = new RawCollectionValue(collection);
							}
						} else {
							throw new SqlPredicateSyntaxException("Syntax error near IN operator");
						}
						Object obj = stack.pop();
						if (obj instanceof Value) {
							predicate = Predicates.in((Value<?>) obj, collectionValue);
						} else if (obj instanceof Number) {
							predicate = Predicates.in((Number) obj, collectionValue);
						} else if (obj instanceof String) {
							String str = (String) obj;
							if (str.startsWith("$")) {
								str = params.get(Integer.valueOf(str.substring(1)));
								predicate = Predicates.in(new RawStringValue(str), collectionValue);
							} else {
								predicate = Predicates.in(new PointerAttributeGetterValue(str), collectionValue);
							}
						} else {
							throw new SqlPredicateSyntaxException("Syntax error near IN operator");
						}
						stack.push(predicate);
						break;
					}
					case NOT_IN: {
						Object size = stack.pop();
						Predicate predicate = null;
						CollectionValue collectionValue = null;
						if (size instanceof String) {
							String sizeStr = (String) size;
							if (sizeStr.equalsIgnoreCase("1*")) {
								collectionValue = new CollectionAttributeGetterValue((String) stack.pop());
							} else {
								List<Object> collection = new ArrayList<>();
								for (int i = 0; i < Integer.valueOf((String) size); i++) {
									String entry = (String) stack.pop();
									if (entry.startsWith("$")) {
										collection.add(0, params.get(Integer.valueOf(entry.substring(1))));
									} else if (StringUtils.isRepresentNumber(entry)) {
										collection.add(0, Double.valueOf(entry));
									} else {
										collection.add(0, new PointerAttributeGetterValue(entry));
									}
								}
								collectionValue = new RawCollectionValue(collection);
							}
						} else {
							throw new SqlPredicateSyntaxException("Syntax error near IN operator");
						}
						Object obj = stack.pop();
						if (obj instanceof Value) {
							predicate = Predicates.notIn((Value<?>) obj, collectionValue);
						} else if (obj instanceof Number) {
							predicate = Predicates.notIn((Number) obj, collectionValue);
						} else if (obj instanceof String) {
							String str = (String) obj;
							if (str.startsWith("$")) {
								str = params.get(Integer.valueOf(str.substring(1)));
								predicate = Predicates.notIn(new RawStringValue(str), collectionValue);
							} else {
								predicate = Predicates.notIn(new PointerAttributeGetterValue(str), collectionValue);
							}
						} else {
							throw new SqlPredicateSyntaxException("Syntax error near NOT IN operator");
						}
						stack.push(predicate);
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

						if (obj1 instanceof String) {
							String str1 = (String) obj1;
							if (str1.startsWith("$")) {
								str1 = params.get(Integer.valueOf(str1.substring(1)));
								obj1 = new RawStringValue(str1);
							} else {
								obj1 = new StringAttributeGetterValue(str1);
							}
						}
						if (obj2 instanceof String) {
							String str2 = (String) obj2;
							if (str2.startsWith("$")) {
								str2 = params.get(Integer.valueOf(str2.substring(1)));
								obj2 = new RawStringValue(str2);
							} else {
								obj2 = new StringAttributeGetterValue(str2);
							}
						}
						if (!(obj1 instanceof StringValue) || !(obj2 instanceof StringValue)) {
							throw new SqlPredicateSyntaxException("Syntax error near LIKE operator");
						}
						stack.push(new Match((StringValue) obj1, (StringValue) obj2));
						break;
					}
					case NOT_LIKE: {
						Object obj2 = stack.pop();
						Object obj1 = stack.pop();

						if (obj1 instanceof String) {
							String str1 = (String) obj1;
							if (str1.startsWith("$")) {
								str1 = params.get(Integer.valueOf(str1.substring(1)));
								obj1 = new RawStringValue(str1);
							} else {
								obj1 = new StringAttributeGetterValue(str1);
							}
						}
						if (obj2 instanceof String) {
							String str2 = (String) obj2;
							if (str2.startsWith("$")) {
								str2 = params.get(Integer.valueOf(str2.substring(1)));
								obj2 = new RawStringValue(str2);
							} else {
								obj2 = new StringAttributeGetterValue(str2);
							}
						}
						if (!(obj1 instanceof StringValue) || !(obj2 instanceof StringValue)) {
							throw new SqlPredicateSyntaxException("Syntax error near LIKE operator");
						}
						stack.push(new NotMatch((StringValue) obj1, (StringValue) obj2));
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

	private static List<Predicate> genLogicBooleanValues(Object obj1, Object obj2) {

		Object _obj1 = obj1;
		Object _obj2 = obj2;

		if (!(_obj1 instanceof BooleanValue)) {
			if (_obj1 instanceof String) {
				_obj1 = new BooleanAttributeGetterValue((String) _obj1);
			}
		}

		if (!(_obj2 instanceof BooleanValue)) {
			if (_obj2 instanceof String) {
				_obj2 = new BooleanAttributeGetterValue((String) _obj2);
			}
		}

		if (!(_obj1 instanceof Predicate)) {
			if (_obj1 instanceof BooleanValue) {
				_obj1 = Predicates.is((BooleanValue) _obj1);
			} else {
				throw new SqlPredicateSyntaxException("Syntax error near AND operator, value1");
			}
		}

		if (!(_obj2 instanceof Predicate)) {
			if (_obj2 instanceof BooleanValue) {
				_obj2 = Predicates.is((BooleanValue) _obj2);
			} else {
				throw new SqlPredicateSyntaxException("Syntax error near AND operator, value2");
			}
		}

		if (!(_obj1 instanceof Predicate) || !(_obj2 instanceof Predicate)) {
			throw new SqlPredicateSyntaxException("Syntax error near AND operator");
		}

		return Arrays.asList((Predicate) _obj1, (Predicate) _obj2);
	}

	private static Predicate genAndPredicate(Object obj2, Object obj1) {
		List<Predicate> values = genLogicBooleanValues(obj1, obj2);
		return Predicates.and(values.get(0), values.get(1));
	}

	private static Predicate genOrPredicate(Object obj2, Object obj1) {
		List<Predicate> values = genLogicBooleanValues(obj1, obj2);
		return Predicates.or(values.get(0), values.get(1));
	}

	private static List<NumberValue> genBetweenNumberValues(Object upper, Object lower, Object value) {
		if (upper instanceof String) {
			if (StringUtils.isRepresentNumber((String) upper)) {
				upper = new RawNumberValue(Double.valueOf((String) upper));
			} else {
				upper = new NumberAttributeGetterValue((String) upper);
			}
		}

		if (lower instanceof String) {
			if (StringUtils.isRepresentNumber((String) lower)) {
				lower = new RawNumberValue(Double.valueOf((String) lower));
			} else {
				lower = new NumberAttributeGetterValue((String) lower);
			}
		}

		if (value instanceof String) {
			if (StringUtils.isRepresentNumber((String) value)) {
				value = new RawNumberValue(Double.valueOf((String) value));
			} else {
				value = new NumberAttributeGetterValue((String) value);
			}
		}

		if (!(value instanceof NumberValue) || !(lower instanceof NumberValue) || !(upper instanceof NumberValue)) {
			throw new SqlPredicateSyntaxException("Syntax error near BETWEEN operator");
		}

		return Arrays.asList((NumberValue) value, (NumberValue) lower, (NumberValue) upper);
	}

	private static Predicate genBetweenPredicate(Object upper, Object lower, Object value) {
		List<NumberValue> numberValues = genBetweenNumberValues(upper, lower, value);
		Predicate predicate = Predicates.between(numberValues.get(0), numberValues.get(1), numberValues.get(2), true,
				true);
		return predicate;
	}

	private static Predicate generateNotBetweenPredicate(Object upper, Object lower, Object value) {
		List<NumberValue> numberValues = genBetweenNumberValues(upper, lower, value);
		Predicate predicate = Predicates.notBetween(numberValues.get(0), numberValues.get(1), numberValues.get(2), true,
				true);
		return predicate;
	}

	private static List<Value<?>> genEqualsValues(Object obj1, Object obj2, List<String> params) {
		if (obj1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj1)) {
				obj1 = new RawNumberValue(Double.valueOf((String) obj1));
			} else if (((String) obj1).startsWith("$")) {
				int paramId = Integer.valueOf(((String) obj1).substring(1));
				obj1 = new RawStringValue(params.get(paramId));
			} else {
				obj1 = new StringAttributeGetterValue((String) obj1);
			}
		} else if (obj1 instanceof Number) {
			obj1 = new RawNumberValue((Number) obj1);
		}

		if (obj2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj2)) {
				obj2 = new RawNumberValue(Double.valueOf((String) obj2));
			} else if (((String) obj2).startsWith("$")) {
				int paramId = Integer.valueOf(((String) obj2).substring(1));
				obj2 = new RawStringValue(params.get(paramId));
			} else {
				obj2 = new StringAttributeGetterValue((String) obj2);
			}
		} else if (obj2 instanceof Number) {
			obj2 = new RawNumberValue((Number) obj2);
		}

		if (!(obj1 instanceof StringValue) //
				&& !(obj1 instanceof NumberValue) //
				&& !(obj2 instanceof StringValue) //
				&& !(obj2 instanceof NumberValue)) {
			throw new SqlPredicateSyntaxException("Syntax error near (not) equals");
		}
		return Arrays.asList((Value<?>) obj1, (Value<?>) obj2);
	}

	private static Predicate genEqualsPredicate(Object obj1, Object obj2, List<String> params) {
		List<Value<?>> values = genEqualsValues(obj1, obj2, params);
		Predicate predicate = new FalsePredicate();
		if (values.get(0) instanceof StringValue) {
			if (values.get(1) instanceof StringValue) {
				return new Exactly((StringValue) values.get(0), (StringValue) values.get(1));
			}
		} else {
			if (values.get(1) instanceof NumberValue) {
				predicate = new Equals((NumberValue) values.get(0), (NumberValue) values.get(1));
			}
		}
		return predicate;
	}

	private static NumberValue[] genNumberValues(Object obj1, Object obj2) {
		if (obj1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) obj1)) {
				obj1 = new RawNumberValue(Double.valueOf((String) obj1));
			} else {
				obj1 = new NumberAttributeGetterValue((String) obj1);
			}
		}

		if (obj2 instanceof String) {
			obj2 = new RawNumberValue(Double.valueOf((String) obj2));
		} else {
			obj2 = new NumberAttributeGetterValue((String) obj1);
		}

		if (!(obj1 instanceof NumberValue) || !(obj2 instanceof NumberValue)) {
			throw new SqlPredicateSyntaxException("Syntax error near >= operator");
		}
		return new NumberValue[] { (NumberValue) obj1, (NumberValue) obj2 };
	}

	private static Predicate genNotEqualsPredicate(Object obj1, Object obj2, List<String> params) {
		List<Value<?>> values = genEqualsValues(obj1, obj2, params);
		Predicate predicate = new FalsePredicate();
		if (values.get(0) instanceof StringValue) {
			if (values.get(1) instanceof StringValue) {
				return new NotExactly((StringValue) values.get(0), (StringValue) values.get(1));
			}
		} else {
			if (values.get(1) instanceof NumberValue) {
				predicate = new NotEquals((NumberValue) values.get(0), (NumberValue) values.get(1));
			}
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
			logger.debug("------------- PREPARE PREFIX -------------");
			Stack<String> stack = new Stack<>();
			Stack<String> output = new Stack<>();
			for (String token : tokens) {
				logger.debug("**** [" + token + "] ****");
				if (token.equals("(")) {
					logger.debug("\tFound openParentheses, push to stack");
					stack.push(token);
				} else if (token.equals(")")) {
					logger.debug("\tFound closing parentheses, pop all from stack until found open");
					if (stack.size() > 0) {
						String stackHead = stack.pop();
						while (!stackHead.equals("(")) {
							logger.debug("\tPop " + stackHead + " from stack --> add to output");
							output.push(stackHead);
							stackHead = stack.pop();
						}
					}
				} else if (isOperator(token)) {
					logger.debug("\tFound operator (" + token + ") --> checking stack head");
					String stackHead = stack.size() > 0 ? stack.peek() : null;
					if (isOperator(stackHead)) {
						logger.debug("\tStack head (" + stackHead + ") is operator, checking precedence...");
						if (getOperatorPrecedence(stackHead) >= getOperatorPrecedence(token)) {
							stackHead = stack.pop();
							logger.debug("\tStack head has higher (or equals) precedence, pop stack (" + stackHead
									+ ") and push to output");

							output.push(stackHead);
							// nếu token hiện tại không phải là math opt và
							// stackHead lại là math opt --> bốc tất cả
							// stackHead còn là math opt
							if (!MATH_OPERATORS.contains(token) && MATH_OPERATORS.contains(stackHead)) {
								logger.debug(
										"\t({}) is non-math operator, checking stack head for remaining math operators",
										token);
								while (stack.size() > 0 && MATH_OPERATORS.contains(stack.peek())) {
									stackHead = stack.pop();
									logger.debug("\tPop {} from stack and push to output", stackHead);
									output.push(stackHead);
								}
							}
							logger.debug("\tPut new operator (" + token + ") to stack");
							stack.push(token);
						} else {
							logger.debug("\tStack head has lower precedence, push token (" + token + ") to stack...");
							stack.push(token);
						}
					} else {
						logger.debug("\tStack head (" + stackHead + ") is not a operator, push token " + token
								+ " to stack");
						stack.push(token);
					}
				} else {
					logger.debug("\tFound operand " + token + " --> push to output");
					output.push(token);
				}
				logger.debug("\tStack: " + stack);
				logger.debug("\tOutput: " + output);
			}
			while (stack.size() > 0) {
				output.push(stack.pop());
			}
			logger.debug("******** Prefix expression: \n{}", output);
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
					inIndexes.add(i);
					inToSizeMap.put(i, -1);
				} else {
					inIndexes.add(i);
					for (int j = i + 2; j < tokens.size(); j++) {
						if (tokens.get(j).equals(")")) {
							inToSizeMap.put(i, j - i - 2);
							break;
						}
					}
				}
			}
		}

		for (int i = inIndexes.size() - 1; i >= 0; i--) {
			int index = inIndexes.get(i);
			int listSize = inToSizeMap.get(index);
			int position = index + (listSize == -1 ? 2 : (listSize + 3));
			String listSizeValue = (listSize == -1) ? "1*" : String.valueOf(listSize);
			tokens.add(position, listSizeValue);
		}
	}

	private static List<String> split(String sql) {
		if (sql != null) {
			String[] splittedBySpace = sql.split(" ");
			List<String> results = new ArrayList<>();
			for (String token : splittedBySpace) {

				char[] chars = token.toCharArray();

				List<Integer> indexes = new ArrayList<>();
				for (int i = 0; i < chars.length; i++) {
					char c = chars[i];
					if (c == OPENING_PARENTHESES || c == CLOSING_PARENTHESES || c == COMMA) {
						indexes.add(i);
					}
				}

				List<String> localTokens = new ArrayList<>();
				if (indexes.size() > 0) {
					int lastIndex = 0;
					for (int index : indexes) {
						char c = chars[index];
						if (index == 0 && lastIndex == 0 && c != COMMA) {
							localTokens.add(String.valueOf(c));
							lastIndex = index + 1;
							if (lastIndex == chars.length - 1) {
								localTokens.add(String.valueOf(chars[index + 1]));
							}
						} else {
							localTokens.add(token.substring(lastIndex, index));
							if (c != COMMA) {
								localTokens.add(String.valueOf(c));
							}
							lastIndex = index + 1;
						}
					}
					if (lastIndex < chars.length - 1) {
						localTokens.add(token.substring(lastIndex));
					}
				} else {
					localTokens.add(token);
				}

				// logger.debug("Token: {} --> local tokens: {}", token,
				// localTokens);

				for (String localToken : localTokens) {
					indexes = new ArrayList<>();
					Map<Integer, String> indexToOperators = new HashMap<>();
					// logger.debug("Checking operator existence on {}",
					// localToken);
					if (!StringUtils.isRepresentNumber(localToken)) {
						char[] localTokenChars = localToken.toCharArray();
						for (int i = 0; i < localTokenChars.length; i++) {
							if (i > 0 && localTokenChars[i - 1] == '`') {
								continue;
							}
							for (String operator : SYMBOLIZE_OPERATORS) {
								int opLength = operator.length();
								if (opLength > localTokenChars.length - i) {
									continue;
								}
								char[] opChars = operator.toCharArray();
								boolean hasOperator = true;
								for (int j = 0; j < opLength; j++) {
									if (opChars[j] != localTokenChars[i + j]) {
										hasOperator = false;
										break;
									}
								}
								if (hasOperator && operator.equals(SQRT)) {
									int index = i + opLength + 1;
									Character nextChar = index < localTokenChars.length ? localTokenChars[index] : null;
									if (nextChar != null && nextChar.charValue() != ' '
											&& nextChar.charValue() != '(') {
										hasOperator = false;
									}
								}
								if (hasOperator) {
									indexes.add(i);
									indexToOperators.put(i, operator);
									// logger.debug("---> found operator {} at
									// index {}", operator, i);
									i += opLength - 1;
									break;
								}
							}
						}
					}
					// logger.debug(" indexes: {}", indexes);
					if (indexes.size() == 0) {
						results.add(localToken);
					} else {
						int lastIndex = 0;
						indexes.sort(new Comparator<Integer>() {

							@Override
							public int compare(Integer o1, Integer o2) {
								return o1 == o2 ? 0 : (o1 > o2 ? 1 : -1);
							}
						});
						for (int i = 0; i < indexes.size(); i++) {
							int index = indexes.get(i);
							int opLength = indexToOperators.get(index).length();
							if (lastIndex < index) {
								results.add(localToken.substring(lastIndex, index));
							}
							results.add(localToken.substring(index, index + opLength));
							lastIndex = index + opLength;
						}
						if (lastIndex < localToken.length()) {
							results.add(localToken.substring(lastIndex, localToken.length()));
						}
					}
					// logger.debug("---> results: {}", results);
				}
			}
			return results;
		}
		return null;
	}

	private static String removeUnnecessarySpaces(String input) {
		return input.replaceAll("\\s+", " ")//
				.replaceAll("(?i)not in", NOT_IN) //
				.replaceAll("(?i)not like", NOT_LIKE) //
				.replaceAll("(?i)not between", NOT_BETWEEN) //
				.replaceAll("(?i)is null", IS_NULL) //
				.replaceAll("(?i)is not null", IS_NOT_NULL);
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
