import java.util.UUID;

import com.nhb.common.utils.Converter;
import com.nhb.common.utils.StringUtils;

public class TestPrintable {
	public static void main(String[] args) {
		String uuidString = "776A248D-838B-46B6-819D-E5B9ADB1B06A";
		UUID uuid = UUID.fromString(uuidString);
		byte[] uuidBytes = Converter.uuidToBytes(uuid);
		boolean prinable = StringUtils.isPrinable(new String(uuidBytes));
		System.out.println(prinable);
	}
}
