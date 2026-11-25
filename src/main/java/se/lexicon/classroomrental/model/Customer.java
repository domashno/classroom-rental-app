package se.lexicon.classroomrental.model;

import java.util.Objects;

public abstract class Customer {

	private final int id;
	private String name;
	private String email;
	private String phone;

	protected Customer(int id, String name, String email, String phone) {
		this.id = id;
		setName(name);
		setEmail(email);
		setPhone(phone);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public final void setName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Customer name cannot be blank.");
		}
		this.name = name.trim();
	}

	public String getEmail() {
		return email;
	}

	public final void setEmail(String email) {
		if (email == null || !email.contains("@")) {
			throw new IllegalArgumentException("Valid customer email is required.");
		}
		this.email = email.trim();
	}

	public String getPhone() {
		return phone;
	}

	public final void setPhone(String phone) {
		if (phone == null || phone.isBlank()) {
			throw new IllegalArgumentException("Customer phone cannot be blank.");
		}
		this.phone = phone.trim();
	}

	public abstract String getCustomerType();

	public abstract String getSummary();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Customer customer)) return false;
		return id == customer.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}