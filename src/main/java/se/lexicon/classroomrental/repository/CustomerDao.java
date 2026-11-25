package se.lexicon.classroomrental.repository;

import se.lexicon.classroomrental.config.DatabaseConfig;
import se.lexicon.classroomrental.exception.DatabaseOperationException;
import se.lexicon.classroomrental.model.CompanyCustomer;
import se.lexicon.classroomrental.model.Customer;
import se.lexicon.classroomrental.model.IndividualCustomer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data access object for company and individual customers.
 */
public class CustomerDao {

	private final DatabaseConfig dbConfig;

	public CustomerDao(DatabaseConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	public List<Customer> findAll() {
		String sql = "SELECT id, customer_type, name, email, phone, organization_number, contact_person FROM customers ORDER BY id";
		List<Customer> customers = new ArrayList<>();
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				customers.add(mapRow(rs));
			}
			return customers;
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error loading customers.", e);
		}
	}

	public Optional<Customer> findById(int id) {
		String sql = "SELECT id, customer_type, name, email, phone, organization_number, contact_person FROM customers WHERE id = ?";
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
			}
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error finding customer #" + id, e);
		}
	}

	public Customer save(Customer customer) {
		String sql = """
                INSERT INTO customers (customer_type, name, email, phone, organization_number, contact_person)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING id
                """;
		try (Connection conn = dbConfig.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, customer.getCustomerType());
			stmt.setString(2, customer.getName());
			stmt.setString(3, customer.getEmail());
			stmt.setString(4, customer.getPhone());

			if (customer instanceof CompanyCustomer company) {
				stmt.setString(5, company.getOrganizationNumber());
				stmt.setString(6, company.getContactPerson());
			} else {
				stmt.setNull(5, Types.VARCHAR);
				stmt.setNull(6, Types.VARCHAR);
			}

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int newId = rs.getInt("id");
					return findById(newId).orElseThrow();
				}
			}
			throw new DatabaseOperationException("Customer insert returned no ID.", null);
		} catch (SQLException e) {
			throw new DatabaseOperationException("Error saving customer.", e);
		}
	}

	private Customer mapRow(ResultSet rs) throws SQLException {
		int id = rs.getInt("id");
		String type = rs.getString("customer_type");
		String name = rs.getString("name");
		String email = rs.getString("email");
		String phone = rs.getString("phone");

		if ("COMPANY".equalsIgnoreCase(type)) {
			return new CompanyCustomer(
					id,
					name,
					email,
					phone,
					rs.getString("organization_number"),
					rs.getString("contact_person")
			);
		}
		return new IndividualCustomer(id, name, email, phone);
	}
}