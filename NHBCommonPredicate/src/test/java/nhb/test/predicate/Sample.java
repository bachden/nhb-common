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

		Predicate predicate = Predicates.fromSQL(
				"age in (25,30, '\\'40') and (not female or name = 'bachden') and bar.foo.name = 'Mario' and bar.foo.value in (5, 8, 10)");

		UserVO userVO = new UserVO();
		userVO.setName("bachden");
		userVO.setAge(30);
		userVO.setFemale(false);

		Foo foo = new Foo();
		foo.setValue(10);
		foo.setName("Mario");

		Bar bar = new Bar();
		bar.setFoo(foo);

		userVO.setBar(bar);

		System.out.println("Is valid user: " + predicate.apply(userVO));
	}

}
