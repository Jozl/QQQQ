package db.beans;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.internal.LinkedTreeMap;

/**
 * 
 * @author qzl 对应mysql里数据库myqq的user表
 */
public class User {
	private String account;
	private String password;
	private String username;
	private String headimage;

	public User(String account, String password, String username, String headimage) {
		this.account = account;
		this.password = password;
		this.username = username;
		this.headimage = headimage;
	}

	// 只有账号密码 登录用的
	public User(String account, String password) {
		this(account, password, null, null);
	}

	public User(ResultSet resultSet) throws SQLException {
		this(resultSet.getString("account"), resultSet.getString("password"), resultSet.getString("username"),
				resultSet.getString("headimage"));
	}

	public User() {
		this("test", null, "测试者", "Default.jpg");
	}

	// public static User rebulidUser(LinkedTreeMap<String, String> ltm) {
	// try {
	// ltm.get("account");
	// return new User(ltm.get("account"), ltm.get("password"),
	// ltm.get("username"));
	// } catch (Exception e) {
	// return new User(ltm.get("account"), ltm.get("password"));
	// }
	// }

	public User withdrawPassword() {
		this.password = null;
		return this;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public User setPassword(String password) {
		this.password = password;
		return this;
	}

	@Override
	public String toString() {
		return "User [account=" + account + ", password=" + password + ", username=" + username + "]";
	}
}
