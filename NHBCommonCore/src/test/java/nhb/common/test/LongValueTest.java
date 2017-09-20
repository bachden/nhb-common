package nhb.common.test;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;

public class LongValueTest {

	public static void main(String[] args) {
		PuArray arr = new PuArrayList();
		arr.addFrom(Long.MAX_VALUE);

		byte[] bytes = arr.toBytes();
		System.out.println(PuArrayList.fromObject(bytes));
	}

}
