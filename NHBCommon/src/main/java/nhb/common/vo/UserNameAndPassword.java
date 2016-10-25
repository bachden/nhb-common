package nhb.common.vo;

import nhb.common.BaseLoggable;

public class UserNameAndPassword extends BaseLoggable {

	private String userName;
	private String password;

	public UserNameAndPassword(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public UserNameAndPassword() {
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
