package nhb.messaging.http;

public enum HttpMethod {
	GET, POST, PUT, OPTION, DELETE;

	public static HttpMethod fromName(String methodName) {
		if (methodName != null) {
			methodName = methodName.trim();
			for (HttpMethod method : values()) {
				if (method.name().equalsIgnoreCase(methodName)) {
					return method;
				}
			}
		}
		return null;
	}
}