import static org.msgpack.template.Templates.tMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.StringTemplate;
import org.msgpack.template.Template;

import nhb.common.data.msgpkg.GenericTypeTemplate;

public class TestMessagePack {

	public static void main(String[] args) throws IOException {

		Template<Map<String, Object>> mapTemplate = tMap(StringTemplate.getInstance(),
				GenericTypeTemplate.getInstance());

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("long_value", Long.MAX_VALUE);

		System.out.println("Working with original map: " + map);

		MessagePack mp = new MessagePack();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Packer pk = mp.createPacker(os);
		mapTemplate.write(pk, map);
		byte[] bytes = os.toByteArray();

		System.out.println(new String(bytes));

		ByteArrayInputStream is = new ByteArrayInputStream(bytes, 0, bytes.length);
		Map<String, Object> map1 = mp.createUnpacker(is).read(mapTemplate);
		System.out.println(map1.get("long_value").getClass());
	}

}
