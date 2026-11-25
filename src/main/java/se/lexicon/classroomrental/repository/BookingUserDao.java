package se.lexicon.classroomrental.repository;

import se.lexicon.classroomrental.config.DatabaseConfig;
import se.lexicon.classroomrental.exception.DatabaseOperationException;
import se.lexicon.classroomrental.model.BookingUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingUserDao {

	private final DatabaseConfig dbConfig;

	public BookingUserDao(DatabaseConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	public List<BookingUser> findAll() {
		String sql = "SELECT id, name, department FROM booking_users ORDER BY id";
		List<BookingUser> users = new ArrayList<>();
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				users.add(new BookingUser(rs.getInt("id"), rs.getString("name"), rs.getString("department")));
			}
			return users;
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error loading booking users.", e);
		}
	}

	public Optional<BookingUser> findById(int id) {
		String sql = "SELECT id, name, department FROM booking_users WHERE id = ?";
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next()
						? Optional.of(new BookingUser(rs.getInt("id"), rs.getString("name"), rs.getString("department")))
						: Optional.empty();
			}
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error finding booking user #" + id, e);
		}
	}
}