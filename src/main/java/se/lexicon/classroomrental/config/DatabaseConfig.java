package se.lexicon.classroomrental.config;

import se.lexicon.classroomrental.exception.DatabaseOperationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connections to the relational database.
 */
public class DatabaseConfig {

	private final String url;
	private final String username;
	private final String password;

	public DatabaseConfig() {
		this.url = envOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/classroom_rental");
		this.username = envOrDefault("DB_USER", "postgres");
		this.password = envOrDefault("DB_PASSWORD", "postgres");
	}

	public Connection getConnection() {
		try {
			return DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			throw new DatabaseOperationException("Failed to connect to database at " + url, e);
		}
	}

	private String envOrDefault(String key, String fallback) {
		String value = System.getenv(key);
		return (value == null || value.isBlank()) ? fallback : value;
	}
}