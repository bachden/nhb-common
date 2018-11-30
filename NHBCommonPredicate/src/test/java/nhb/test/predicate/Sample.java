package nhb.test.predicate;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.Predicates;
import com.nhb.common.utils.Initializer;
import com.nhb.common.utils.TimeWatcher;

import lombok.Data;

public class Sample {

	static {
		Initializer.bootstrap(Sample.class);
	}

	@Data
	public static class Foo {
		private String name;
		private int value;
		private Collection<String> collection;
		private Collection<Integer> collection1;
	}

	@Data
	public static class Bar {
		private Foo foo;
	}

	@Data
	public static class UserVO {
		private Bar bar;
		private int age;
		private String name;
		private boolean isFemale;
		private String sqrt;

		private long time;
	}

	public static void main(String[] args) {
		// FilteredObject filteredObject = PredicateBuilder.newFilteredObject();
		// Predicate predicate = filteredObject.get("age").between(10,
		// 20).and(filteredObject.is("female")).build();

		String sql = null;

		sql = "age%2+4*age-1*5+6 != 0 " //
				+ "and (1+2^4) % 5 - 8 = 1 " //
				+ "or age in bar.foo.collection1 " //
				+ "and `sqrt` = 'ok' " //
				+ "and (not female or name = bar.foo.name) " //
				+ "and bar IS NOT NULL " //
				+ "and (name in ('noname', -1, bar.foo.name) " //
				+ "or bar.foo.name like '[Ms]ario.*') " //
				//+ "and (sqrt bar.foo.value >= 4)"//
				;

		// sql = "time >= 10 And age = 7";

		Predicate predicate = Predicates.fromSQL(sql);

		predicate = Predicates.fromSQL(sql);

		System.out.println("Predicate: " + predicate.toString());

		UserVO userVO = new UserVO();
		userVO.setName("bachden");
		userVO.setAge(7);
		userVO.setFemale(false);
		userVO.setSqrt("ok");
		userVO.setTime(20);

		Foo foo = new Foo();
		foo.setValue(27);
		foo.setName("bachden");
		foo.setCollection(Arrays.asList("bachden", "ok bây bê", "nothing to do"));
		foo.setCollection1(Arrays.asList(23, 45, 80));

		Bar bar = new Bar();
		bar.setFoo(foo);

		userVO.setBar(bar);
		// warm up
		predicate.apply(userVO);

		TimeWatcher timeWatcher = new TimeWatcher();
		timeWatcher.reset();
		int loop = (int) 1e6;
		for (int i=0; i<loop; i++) {
			predicate.apply(userVO);
		}

		double time = timeWatcher.endLapNano();
		DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
		System.out.println("Ops: " + decimalFormat.format(loop / (Double.valueOf(time) / 1e9)) + ", avg time: " + decimalFormat.format(Double.valueOf(time) / 1e3 / loop) + " microseconds");
	}

}
