package se.lexicon.classroomrental.repository;

import se.lexicon.classroomrental.config.DatabaseConfig;
import se.lexicon.classroomrental.exception.DatabaseOperationException;
import se.lexicon.classroomrental.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookingDao {

	private final DatabaseConfig dbConfig;

	public BookingDao(DatabaseConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	public List<Booking> findAll() {
		String sql = """
                SELECT b.id, b.booking_date, b.start_time, b.end_time, b.comments,
                       r.id AS room_id, r.name AS room_name, r.capacity, r.equipment, r.accessible,
                       c.id AS cust_id, c.customer_type, c.name AS cust_name, c.email, c.phone,
                       c.organization_number, c.contact_person,
                       u.id AS user_id, u.name AS user_name, u.department
                FROM bookings b
                JOIN classrooms r ON b.classroom_id = r.id
                JOIN customers c ON b.customer_id = c.id
                JOIN booking_users u ON b.booking_user_id = u.id
                ORDER BY b.booking_date, b.start_time
                """;
		List<Booking> bookings = new ArrayList<>();
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				bookings.add(mapRow(rs));
			}
			return bookings;
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error retrieving bookings.", e);
		}
	}

	public Booking save(Booking booking) {
		String sql = """
                INSERT INTO bookings (classroom_id, customer_id, booking_user_id, booking_date, start_time, end_time, comments)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
                """;
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, booking.getClassroom().getId());
			stmt.setInt(2, booking.getCustomer().getId());
			stmt.setInt(3, booking.getBookingUser().getId());
			stmt.setDate(4, Date.valueOf(booking.getDate()));
			stmt.setTime(5, Time.valueOf(booking.getStartTime()));
			stmt.setTime(6, Time.valueOf(booking.getEndTime()));
			stmt.setString(7, booking.getComments());

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new Booking(
							rs.getInt("id"),
							booking.getClassroom(),
							booking.getCustomer(),
							booking.getBookingUser(),
							booking.getDate(),
							booking.getStartTime(),
							booking.getEndTime(),
							booking.getComments()
					);
				}
			}
			throw new DatabaseOperationException("Booking insert returned no ID.", null);
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error saving booking.", e);
		}
	}

	public boolean deleteById(int id) {
		String sql = "DELETE FROM bookings WHERE id = ?";
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error cancelling booking #" + id, e);
		}
	}

	private Booking mapRow(ResultSet rs) throws SQLException {
		String rawEq = rs.getString("equipment");
		List<String> eqList = (rawEq == null || rawEq.isBlank())
				? List.of()
				: Arrays.stream(rawEq.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();

		Classroom room = new Classroom(
				rs.getInt("room_id"),
				rs.getString("room_name"),
				rs.getInt("capacity"),
				eqList,
				rs.getBoolean("accessible")
		);

		Customer customer;
		String custType = rs.getString("customer_type");
		if ("COMPANY".equalsIgnoreCase(custType)) {
			customer = new CompanyCustomer(
					rs.getInt("cust_id"),
					rs.getString("cust_name"),
					rs.getString("email"),
					rs.getString("phone"),
					rs.getString("organization_number"),
					rs.getString("contact_person")
			);
		} else {
			customer = new IndividualCustomer(
					rs.getInt("cust_id"),
					rs.getString("cust_name"),
					rs.getString("email"),
					rs.getString("phone")
			);
		}

		BookingUser user = new BookingUser(
				rs.getInt("user_id"),
				rs.getString("user_name"),
				rs.getString("department")
		);

		LocalDate date = rs.getDate("booking_date").toLocalDate();
		LocalTime start = rs.getTime("start_time").toLocalTime();
		LocalTime end = rs.getTime("end_time").toLocalTime();
		String comments = rs.getString("comments");

		return new Booking(rs.getInt("id"), room, customer, user, date, start, end, comments);
	}
}