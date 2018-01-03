package nhb.common.test;

import java.util.HashMap;
import java.util.Map;

import com.nhb.common.utils.ObjectUtils;

public class TestExtractData {

	public static void main(String[] args) {
		Map<String, Object> root = new HashMap<>();
		Map<String, Object> child1 = new HashMap<>();
		Map<String, Object> child2 = new HashMap<>();

		child2.put("val", "Tuan");
		child1.put("name", child2);
		root.put("properties", child1);

		System.out.println("value: " + ObjectUtils.getValueByPath(root, "properties.name.val"));
	}
}