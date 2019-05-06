package com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.beans.User;

public class Dao {
	Connection connection;

	public Dao() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/myQQ?characterEncoding=utf8", "root",
					"312400");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isUserInTable(User user) {
		String sql = "select * from user where account = ? and password = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
			preparedStatement.setString(1, user.getAccount());
			preparedStatement.setString(2, user.getPassword());
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public User getUser(String account) {
		String sql = "select * from user where account = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
			preparedStatement.setString(1, account);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.last();
			if (resultSet != null)
				return new User(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<User> getHisFriends(String account) {
		String sql = "select * from user where account != ?";
		List<User> results = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
			preparedStatement.setString(1, account);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
				results.add(new User(resultSet));

			return results;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	public int addNewUser(User user) {
		String sql = "insert into user values(?, ?, ?, ?, ?)";
		int account = getNewAccout();
		if (account == -1) {
			return account;
		}
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
			preparedStatement.setInt(1, account);
			preparedStatement.setString(2, user.getPassword());
			preparedStatement.setString(3, user.getUsername());
			preparedStatement.setString(4, null);
			preparedStatement.setString(5, "Default.jpg");
			if (preparedStatement.executeUpdate() != 1) {
				return -2;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return account;
	}

	public int getNewAccout() {
		String sql = "select max(account) from user";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.last();

			return 1 + resultSet.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
