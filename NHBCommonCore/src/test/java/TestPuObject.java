import java.io.IOException;

import com.nhb.common.data.PuObject;

import net.minidev.json.parser.ParseException;

public class TestPuObject {

	public static void main(String[] args) throws ParseException, IOException {

		PuObject value = new PuObject();
		value.set("name", "bachden");
		value.set("bytes", new byte[] { 1, 2, 3 });
		value.set("age", 28);

		byte[] bytes = value.toBytes();

		System.out.println("--> " + PuObject.fromObject(bytes));
	}
}
