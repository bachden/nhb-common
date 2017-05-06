package nhb.test.predicate;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.Predicates;
import com.nhb.common.utils.Initializer;

import lombok.Data;

public class Sample {

	static {
		Initializer.bootstrap(Sample.class);
	}

	@Data
	public static class Foo {
		private String name;
		private int value;
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
	}

	public static void main(String[] args) {
		// FilteredObject filteredObject = PredicateBuilder.newFilteredObject();
		// Predicate predicate = filteredObject.get("age").between(10,
		// 20).and(filteredObject.is("female")).build();

		String sql = "age%2+4*age-1*5+6 != 0 and (not female or name = bar.foo.name) and bar IS NOT NULL and (name in ('noname', -1, bar.foo.name) or bar.foo.name like '[Ms]ario.*') and (sqrt bar.foo.value >= 4)";
		Predicate predicate = Predicates.fromSQL(sql);

		predicate = Predicates.fromSQL(sql);

		UserVO userVO = new UserVO();
		userVO.setName("bachden");
		userVO.setAge(23);
		userVO.setFemale(false);

		Foo foo = new Foo();
		foo.setValue(27);
		foo.setName("bachden");

		Bar bar = new Bar();
		bar.setFoo(foo);

		userVO.setBar(bar);

		System.out.println("Is valid user: " + predicate.apply(userVO));
	}

}
