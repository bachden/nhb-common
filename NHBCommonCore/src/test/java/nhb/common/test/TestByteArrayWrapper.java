package nhb.common.test;

import java.util.HashMap;
import java.util.Map;

import com.nhb.common.utils.MapUtils;
import com.nhb.common.vo.ByteArrayWrapper;

public class TestByteArrayWrapper {

	public static void main(String[] args) {
		Map<byte[], String> map1 = new HashMap<>();

		String key1 = new String("key1");
		String key2 = new String("key2");

		map1.put(key1.getBytes(), key1);
		map1.put(key2.getBytes(), key2);

		System.out.println(map1.containsKey(new String("key1").getBytes()));

		Map<ByteArrayWrapper, String> map2 = MapUtils.convertMapWithByteArrayKey(map1);

		System.out.println(map2.containsKey(new ByteArrayWrapper(new String("key1").getBytes())));
	}

}
