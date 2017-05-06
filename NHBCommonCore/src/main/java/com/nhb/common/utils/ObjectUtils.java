package com.nhb.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.nhb.common.annotations.DefaultSetter;
import com.nhb.common.annotations.Transparent;
import com.nhb.common.exception.FieldNotFoundException;
import com.nhb.common.exception.NullValueOnPathException;
import com.nhb.common.utils.ArrayUtils.ForeachCallback;

//this is object utils class
public final class ObjectUtils {

	private static final String GETTER_PREFIX = "get";
	private static final String BOOLEAN_GETTER_PREFIX = "is";
	private static final String SETTER_PREFIX = "set";

	private static Map<Class<?>, Map<String, Getter>> classGetters = new ConcurrentHashMap<Class<?>, Map<String, Getter>>();
	private static Map<Class<?>, Map<String, Setter>> classSetters = new ConcurrentHashMap<Class<?>, Map<String, Setter>>();

	public synchronized static final void clearClassGettersCache() {
		classGetters = new ConcurrentHashMap<Class<?>, Map<String, Getter>>();
	}

	public synchronized static final void clearClassSettersCache() {
		classSetters = new ConcurrentHashMap<Class<?>, Map<String, Setter>>();
	}

	private static class Getter {
		boolean isMethod = false;
		Field field;
		Method method;

		public Getter(Field field) {
			this.field = field;
			this.isMethod = false;
		}

		public Getter(Method method) {
			this.method = method;
			this.isMethod = true;
		}

		public Object get(Object obj) {
			try {
				if (this.isMethod) {
					return this.method.invoke(obj);
				} else {
					return field.get(obj);
				}
			} catch (Exception ex) {
				throw new RuntimeException("Cannot get value from "
						+ (this.isMethod ? ("method " + this.method.getName()) : ("field " + this.field.getName())),
						ex);
			}
		}
	}

	public static class Setter {
		boolean isMethod = false;
		Field field;
		Method method;
		private Class<?> paramType;
		private Class<?> componentType;

		public Setter(Field field) {
			this.field = field;
			this.setParamType(field.getType());
			this.isMethod = false;
		}

		public Setter(Method method) {
			this.method = method;
			this.setParamType(this.method.getParameterTypes()[0]);
			if (getParamType().isArray()) {
				this.setComponentType(getParamType().getComponentType());
			} else if (method.isAnnotationPresent(DefaultSetter.class)) {
				DefaultSetter annotation = method.getAnnotation(DefaultSetter.class);
				this.setComponentType(annotation.value());
				// System.out.println("method is annotated by " +
				// DefaultSetter.class.getName() + ", component type is "
				// + this.componentType);
			} else if (Iterable.class.isAssignableFrom(getParamType())) {
				String fieldName = StringUtils.lowerCaseFirstLetter(method.getName().substring(SETTER_PREFIX.length()));
				Class<?> clazz = this.method.getDeclaringClass();
				// System.out.println("finding field name `" + fieldName +
				// "` for class " + clazz.getName());
				try {
					Field field = clazz.getDeclaredField(fieldName);
					ParameterizedType type = (ParameterizedType) field.getGenericType();
					setComponentType((Class<?>) type.getActualTypeArguments()[0]);
				} catch (Exception e) {
					// System.out.println("cannot to find field with name " +
					// fieldName + " for class " + clazz.getName());
					e.printStackTrace();
				}
			}
			// System.out.println("setter created with method " +
			// method.getName() + ", param type: " + paramType
			// + " component type: " + componentType);
			this.isMethod = true;
		}

		public Object set(Object obj, Object value) {
			try {
				if (this.isMethod) {
					return this.method.invoke(obj, value);
				} else {
					return field.get(obj);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		public Class<?> getComponentType() {
			return componentType;
		}

		public void setComponentType(Class<?> componentType) {
			this.componentType = componentType;
		}

		public Class<?> getParamType() {
			return paramType;
		}

		public void setParamType(Class<?> paramType) {
			this.paramType = paramType;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <T> T fromMap(Class<T> clazz, Map<String, ?> data) throws Exception {
		Map<String, Setter> classSetter = findAllClassSetters(clazz);
		T result = clazz.newInstance();
		for (Entry<String, ?> entry : data.entrySet()) {
			if (classSetter.containsKey(entry.getKey())) {
				Object value = entry.getValue();
				final Setter setter = classSetter.get(entry.getKey());
				if (value == null) {
					setter.set(result, null);
				} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(value.getClass())) {
					setter.set(result, PrimitiveTypeUtils.getValueFrom(setter.getParamType(), value));
				} else if (value instanceof Map) {
					setter.set(result, fromMap(setter.getParamType(), (Map) value));
				} else if (ArrayUtils.isArrayOrCollection(setter.getParamType())
						&& ArrayUtils.isArrayOrCollection(value.getClass())) {
					final List list = new ArrayList<>();
					ArrayUtils.foreach(value, new ForeachCallback<Object>() {

						@Override
						public void apply(Object element) {
							try {
								if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(element.getClass())) {
									list.add(PrimitiveTypeUtils.getValueFrom(setter.getComponentType(), element));
								} else if (element instanceof Map) {
									list.add(fromMap(setter.getComponentType(), (Map) element));
								}
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					});
					if (Collection.class.isAssignableFrom(setter.getParamType())) {
						setter.set(result, list);
					} else if (setter.getParamType().isArray()) {
						if (setter.getComponentType().isPrimitive()) {
							setter.set(result, ArrayUtils.toPrimitiveTypeArray(setter.getComponentType(), list));
						} else {
							setter.set(result, ArrayUtils.toArray(setter.getComponentType(), list));
						}
					}
				} else {
					throw new RuntimeException("unable to find suitable setter for data: " + value);
				}
			}
		}
		return result;
	}

	public static <T> Map<String, Setter> findAllClassSetters(Class<T> clazz) {
		Map<String, Setter> classSetter = null;
		if (classSetters.containsKey(clazz)) {
			classSetter = classSetters.get(clazz);
		} else {
			classSetter = new HashMap<>();
			Map<String, List<Method>> methodsByName = new HashMap<String, List<Method>>();
			Set<Method> methods = getAllInstancePublicMethods(clazz);
			for (Method method : methods) {
				if (method.getName().length() > SETTER_PREFIX.length() && method.getName().startsWith(SETTER_PREFIX)) {
					if (!methodsByName.containsKey(method.getName())) {
						methodsByName.put(method.getName(), new ArrayList<Method>());
					}
					methodsByName.get(method.getName()).add(method);
				}
			}
			Set<Field> fields = getAllInstancePublicFileds(clazz);
			for (Field field : fields) {
				if (!Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
					classSetter.put(field.getName(), new Setter(field));
				}
			}
			for (String methodName : methodsByName.keySet()) {
				List<Method> setters = methodsByName.get(methodName);
				Method setter = null;
				if (setters.size() > 0) {
					String fieldName = StringUtils.lowerCaseFirstLetter(methodName.substring(SETTER_PREFIX.length()));
					switch (setters.size()) {
					case 1:
						setter = setters.get(0);
						break;
					default:
						for (Method method : setters) {
							if (method.isAnnotationPresent(DefaultSetter.class) && method.getParameterCount() == 1) {
								setter = method;
								break;
							}
						}
						if (setter == null) {
							for (Method method : setters) {
								if (method.getParameterCount() == 1) {
									setter = method;
									break;
								}
							}
						}
						break;
					}
					if (setter != null) {
						classSetter.put(fieldName, new Setter(setter));
					}
				}
			}
			classSetters.put(clazz, classSetter);
		}

		return classSetter;
	}

	private static Set<Field> getAllInstancePublicFileds(final Class<?> clazz) {
		Set<Field> result = new HashSet<>();
		Set<String> checkFieldName = new HashSet<>();
		Class<?> _class = clazz;
		while (_class != null && _class != Object.class && _class != Class.class) {
			if (!_class.isAnnotationPresent(Transparent.class)) {
				Field[] fields = _class.getDeclaredFields();
				for (Field field : fields) {
					if (!checkFieldName.contains(field.getName())) {
						result.add(field);
						checkFieldName.add(field.getName());
					}
				}
			}
			_class = _class.getSuperclass();
		}
		return result;
	}

	private static Set<Method> getAllInstancePublicMethods(final Class<?> clazz) {
		Set<Method> result = new HashSet<>();
		Class<?> _class = clazz;
		while (_class != null && _class != Object.class && _class != Class.class) {
			Method[] methods = _class.getDeclaredMethods();
			for (Method method : methods) {
				if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())
						&& !method.isAnnotationPresent(Transparent.class)) {
					result.add(method);
				}
			}
			_class = _class.getSuperclass();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T getValueByPath(Object obj, String path) {
		if (obj != null && path != null) {
			String[] arr = path.split("\\.");
			Object currObj = obj;
			for (int i = 0; i < arr.length; i++) {
				String fieldName = arr[i];
				if (currObj == null) {
					throw new NullValueOnPathException("Cannot get field '" + fieldName + "' from '" + arr[i - 1]
							+ "' == null, primitive object: " + obj.toString() + ", path: " + path);
				}
				currObj = getFieldValue(currObj, fieldName);
			}
			return (T) currObj;
		}
		throw new IllegalArgumentException("Object and path must be not-null");
	}

	@SuppressWarnings("unchecked")
	public static final <T> T getFieldValue(Object obj, String fieldName) {
		if (obj != null && fieldName != null) {
			if (fieldName.startsWith("`")) {
				fieldName = fieldName.substring(1);
			}
			if (fieldName.endsWith("`")) {
				fieldName = fieldName.substring(0, fieldName.length() - 1);
			}
			if (obj instanceof Map) {
				return (T) ((Map<String, Object>) obj).get(fieldName);
			}

			Class<?> clazz = obj.getClass();
			Map<String, Getter> getters = classGetters.containsKey(obj.getClass()) ? classGetters.get(obj.getClass())
					: initClassGetters(clazz);
			if (getters.containsKey(fieldName)) {
				return (T) getters.get(fieldName).get(obj);
			} else {
				throw new FieldNotFoundException(
						"Field '" + fieldName + "' cannot be found in object type " + obj.getClass().getName());
			}
		}
		throw new IllegalArgumentException("Object and fieldName must be not-null");
	}

	private static final Map<String, Getter> initClassGetters(Class<?> clazz) {
		Map<String, Getter> classGetter = new HashMap<String, ObjectUtils.Getter>();
		Set<Field> fields = getAllInstancePublicFileds(clazz);

		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
				try {
					classGetter.put(field.getName(), new Getter(field));
				} catch (IllegalArgumentException e) {
					throw e;
				}
			}
		}
		Set<Method> methods = getAllInstancePublicMethods(clazz);
		for (Method method : methods) {
			String methodName = method.getName();
			if (method.getParameterCount() == 0) {
				if (methodName.startsWith(GETTER_PREFIX) && methodName.length() > GETTER_PREFIX.length()) {
					try {
						String fieldName = StringUtils
								.lowerCaseFirstLetter(methodName.substring(GETTER_PREFIX.length()));
						classGetter.put(fieldName, new Getter(method));
					} catch (IllegalArgumentException e) {
						throw e;
					}
				} else if (methodName.startsWith(BOOLEAN_GETTER_PREFIX)
						&& methodName.length() > BOOLEAN_GETTER_PREFIX.length()) {
					try {
						String fieldName = StringUtils
								.lowerCaseFirstLetter(methodName.substring(BOOLEAN_GETTER_PREFIX.length()));
						classGetter.put(fieldName, new Getter(method));
					} catch (IllegalArgumentException e) {
						throw e;
					}
				}

			}
		}
		classGetters.put(clazz, classGetter);
		return classGetter;
	}

	@SuppressWarnings("unchecked")
	public static final Map<String, Object> toMap(Object obj) {
		if (obj == null) {
			return null;
		}

		if (obj instanceof Map) {
			return (Map<String, Object>) obj;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Getter> getters = classGetters.containsKey(obj.getClass()) ? classGetters.get(obj.getClass())
				: initClassGetters(obj.getClass());
		for (Entry<String, Getter> entry : getters.entrySet()) {
			map.put(entry.getKey(), entry.getValue().get(obj));
		}
		return map;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final List toList(Object obj) {
		final List list = new ArrayList<>();
		if (ArrayUtils.isArrayOrCollection(obj.getClass())) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");
			ArrayUtils.foreach(obj, new ForeachCallback<Object>() {

				@Override
				public void apply(Object element) {
					if (element == null) {
						list.add(null);
					} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(element.getClass())) {
						list.add(element);
					} else if (ArrayUtils.isArrayOrCollection(element.getClass())) {
						list.add(toList(element));
					} else if (element instanceof Date) {
						list.add(df.format(element));
					} else if (element.getClass().isEnum()) {
						list.add(element.toString());
					} else if (element instanceof Throwable) {
						list.add(ExceptionUtils.getFullStackTrace((Throwable) element));
					} else {
						list.add(toMapRecursive(element));
					}
				}
			});
		}
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Map<String, Object> toMapRecursive(Object obj) {
		if (obj == null) {
			return null;
		}
		if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(obj.getClass())) {
			throw new RuntimeException("cannot convert primitive type : " + obj.getClass() + " to Map");
		}
		if (ArrayUtils.isArrayOrCollection(obj.getClass())) {
			throw new RuntimeException("cannot convert array|collection : " + obj.getClass() + " to Map");
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");

		Map<?, Object> rawMap = obj instanceof Map ? (Map) obj : toMap(obj);
		Map<String, Object> map = new HashMap<String, Object>();
		for (Entry<?, Object> child : rawMap.entrySet()) {
			String field = String.valueOf(child.getKey());
			Object value = child.getValue();
			if (value == null) {
				map.put(field, null);
			} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(value.getClass())) {
				map.put(field, value);
			} else if (ArrayUtils.isArrayOrCollection(value.getClass())) {
				map.put(field, toList(value));
			} else if (value instanceof Map<?, ?>) {
				Map<String, Object> childMap = new HashMap<String, Object>();
				for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
					if (entry.getValue() == null) {
						childMap.put(String.valueOf(entry.getKey()), null);
					} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(((Object) entry.getValue()).getClass())) {
						childMap.put(String.valueOf(entry.getKey()), (Object) entry.getValue());
					} else if (ArrayUtils.isArrayOrCollection(entry.getValue().getClass())) {
						childMap.put(String.valueOf(entry.getKey()), toList(entry.getValue()));
					} else if (entry.getValue() instanceof Date) {
						map.put(field, df.format(entry.getValue()));
					} else {
						childMap.put(String.valueOf(entry.getKey()), toMapRecursive(entry.getValue()));
					}
				}
				map.put(field, childMap);
			} else if (value instanceof Date) {
				map.put(field, df.format(value));
			} else if (value.getClass().isEnum()) {
				map.put(field, value.toString());
			} else if (value instanceof Throwable) {
				map.put(field, ExceptionUtils.getFullStackTrace((Throwable) value));
			} else {
				map.put(field, toMapRecursive(value));
			}
		}
		return map;
	}
}