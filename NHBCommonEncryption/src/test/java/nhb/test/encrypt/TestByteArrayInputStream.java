package nhb.test.encrypt;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TestByteArrayInputStream {

	public static void main(String[] args) throws IOException {

		byte[] bytes = new byte[] { "a".getBytes()[0], "b".getBytes()[0], "c".getBytes()[0] };

		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] a = new byte[1];
		byte[] b = new byte[1];
		byte[] c = new byte[1];
		is.read(a);
		is.read(b);
		is.read(c);
		System.out.println("a = " + new String(a));
		System.out.println("b = " + new String(b));
		System.out.println("c = " + new String(c));
	}

}
