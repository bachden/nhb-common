import java.io.IOException;

import com.nhb.common.data.PuValue;
import com.nhb.common.data.msgpkg.PuElementTemplate;

import net.minidev.json.parser.ParseException;

public class TestPuObject {

	public static void main(String[] args) throws ParseException, IOException {

		PuValue value = new PuValue("Bach den");
		byte[] bytes = value.toBytes();
		System.out.println(new String(bytes).length());
		System.out.println(PuElementTemplate.getInstance().read(bytes));
	}
}
