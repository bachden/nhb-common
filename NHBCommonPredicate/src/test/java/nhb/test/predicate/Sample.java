package nhb.test.predicate;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.Predicates;
import com.nhb.common.utils.Initializer;

public class Sample {

	static {
		Initializer.bootstrap(Sample.class);
	}

	public static class UserVO {
		private int age;
		private String name;
		private boolean isFemale;

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isFemale() {
			return isFemale;
		}

		public void setFemale(boolean isFemale) {
			this.isFemale = isFemale;
		}
	}

	public static void main(String[] args) {
		// FilteredObject filteredObject = PredicateBuilder.newFilteredObject();
		// Predicate predicate = filteredObject.get("age").between(10,
		// 20).and(filteredObject.is("female")).build();

		Predicate predicate = Predicates.fromSQL("age in (25,30, '\\'40') and (not female or name = 'bachden')");

		UserVO userVO = new UserVO();
		userVO.setName("bachden");
		userVO.setAge(30);
		userVO.setFemale(false);

		System.out.println("Is valid user: " + predicate.apply(userVO));
	}

}
