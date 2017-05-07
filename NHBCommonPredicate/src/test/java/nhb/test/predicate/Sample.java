package nhb.test.predicate;

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
	}

	public static void main(String[] args) {
		// FilteredObject filteredObject = PredicateBuilder.newFilteredObject();
		// Predicate predicate = filteredObject.get("age").between(10,
		// 20).and(filteredObject.is("female")).build();

		String sql = null;

		sql = "age%2+4*age-1*5+6 != 0 and (1+2^4) % 5 - 8 = 1 or age in bar.foo.collection1 and `sqrt` = 'ok' and (not female or name = bar.foo.name) and bar IS NOT NULL and (name in ('noname', -1, bar.foo.name) or bar.foo.name like '[Ms]ario.*') and (sqrt bar.foo.value >= 4)";
		// sql = "name in bar.foo.collection";

		Predicate predicate = Predicates.fromSQL(sql);

		predicate = Predicates.fromSQL(sql);

		System.out.println("Predicate: " + predicate.toString());

		UserVO userVO = new UserVO();
		userVO.setName("bachden");
		userVO.setAge(23);
		userVO.setFemale(false);
		userVO.setSqrt("ok");

		Foo foo = new Foo();
		foo.setValue(27);
		foo.setName("bachden");
		foo.setCollection(Arrays.asList("bachden", "quybu", "tombeo"));
		foo.setCollection1(Arrays.asList(23, 45, 80));

		Bar bar = new Bar();
		bar.setFoo(foo);

		userVO.setBar(bar);
		boolean validUser = predicate.apply(userVO);

		TimeWatcher timeWatcher = new TimeWatcher();
		timeWatcher.reset();
		validUser = predicate.apply(userVO);
		long time = timeWatcher.endLapMicro();
		System.out.println("Is valid user: " + validUser + " --> time: " + time + " microseconds");
	}

}
