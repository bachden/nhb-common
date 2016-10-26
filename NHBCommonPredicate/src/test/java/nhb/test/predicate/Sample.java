package nhb.test.predicate;

import com.nhb.common.predicate.FilteredObject;
import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.PredicateBuilder;

public class Sample {

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
		FilteredObject filteredObject = PredicateBuilder.newFilteredObject();

		Predicate predicate = filteredObject.get("age").between(10, 20).and(filteredObject.is("female")).build();

		UserVO userVO = new UserVO();
		userVO.setAge(30);
		userVO.setFemale(true);

		System.out.println("Is valid user: " + predicate.apply(userVO));
	}

}
