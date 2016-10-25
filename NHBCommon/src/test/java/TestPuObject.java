import java.io.IOException;

import net.minidev.json.parser.ParseException;
import nhb.common.data.PuValue;
import nhb.common.data.msgpkg.PuElementTemplate;

public class TestPuObject {

	public static void main(String[] args) throws ParseException, IOException {

		PuValue value = new PuValue("Bach den");
		byte[] bytes = value.toBytes();
		System.out.println(new String(bytes).length());
		System.out.println(PuElementTemplate.getInstance().read(bytes));
	}
}
