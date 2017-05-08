import java.io.IOException;

import com.nhb.common.data.PuObject;

import net.minidev.json.parser.ParseException;

public class TestPuObject {

	public static void main(String[] args) throws ParseException, IOException {

		System.out.println("Test string");
		PuObject value = new PuObject();
		value.set("name", "bachden");
		// value.set("age", 28);
		byte[] bytes = value.toBytes();
		System.out.println("--> " + PuObject.fromObject(bytes));

		System.out.println("Test byte array");
		value = new PuObject();
		value.set("bytes",
				new byte[] { 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2,
						3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2,
						3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4 });
		bytes = value.toBytes();
		System.out.println("--> " + PuObject.fromObject(bytes));
	}
}
