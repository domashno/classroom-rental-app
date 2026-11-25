package se.lexicon.classroomrental.repository;

import se.lexicon.classroomrental.config.DatabaseConfig;
import se.lexicon.classroomrental.exception.DatabaseOperationException;
import se.lexicon.classroomrental.model.Classroom;

import java.sql.*;
import java.util.*;

public class ClassroomDao {

	private final DatabaseConfig dbConfig;

	public ClassroomDao(DatabaseConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	public List<Classroom> findAll() {
		String sql = "SELECT id, name, capacity, equipment, accessible FROM classrooms ORDER BY id";
		List<Classroom> classrooms = new ArrayList<>();
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				classrooms.add(mapRow(rs));
			}
			return classrooms;
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error retrieving classrooms.", e);
		}
	}

	public Optional<Classroom> findById(int id) {
		String sql = "SELECT id, name, capacity, equipment, accessible FROM classrooms WHERE id = ?";
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
			}
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error finding classroom with id " + id, e);
		}
	}

	public Classroom save(Classroom classroom) {
		String sql = "INSERT INTO classrooms (name, capacity, equipment, accessible) VALUES (?, ?, ?, ?) RETURNING id";
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, classroom.getName());
			stmt.setInt(2, classroom.getCapacity());
			stmt.setString(3, String.join(",", classroom.getEquipment()));
			stmt.setBoolean(4, classroom.isAccessible());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new Classroom(
							rs.getInt("id"),
							classroom.getName(),
							classroom.getCapacity(),
							classroom.getEquipment(),
							classroom.isAccessible()
					);
				}
			}
			throw new DatabaseOperationException("Classroom insert returned no ID.", null);
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error saving classroom.", e);
		}
	}

	public void update(Classroom classroom) {
		String sql = "UPDATE classrooms SET name = ?, capacity = ?, equipment = ?, accessible = ? WHERE id = ?";
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, classroom.getName());
			stmt.setInt(2, classroom.getCapacity());
			stmt.setString(3, String.join(",", classroom.getEquipment()));
			stmt.setBoolean(4, classroom.isAccessible());
			stmt.setInt(5, classroom.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error updating classroom #" + classroom.getId(), e);
		}
	}

	public boolean deleteById(int id) {
		String sql = "DELETE FROM classrooms WHERE id = ?";
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error deleting classroom #" + id, e);
		}
	}

	private Classroom mapRow(ResultSet rs) throws SQLException {
		String rawEq = rs.getString("equipment");
		List<String> eqList = (rawEq == null || rawEq.isBlank())
				? List.of()
				: Arrays.stream(rawEq.split(","))
						.map(String::trim)
						.filter(s -> !s.isBlank())
						.toList();

		return new Classroom(
				rs.getInt("id"),
				rs.getString("name"),
				rs.getInt("capacity"),
				eqList,
				rs.getBoolean("accessible")
		);
	}
}