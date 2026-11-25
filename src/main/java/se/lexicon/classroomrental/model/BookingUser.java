package se.lexicon.classroomrental.model;

import java.util.Objects;

public class BookingUser {

	private final int id;
	private String name;
	private String department;

	public BookingUser(int id, String name, String department) {
		this.id = id;
		setName(name);
		setDepartment(department);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public final void setName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Booking user name cannot be blank.");
		}
		this.name = name.trim();
	}

	public String getDepartment() {
		return department;
	}

	public final void setDepartment(String department) {
		this.department = department == null ? "" : department.trim();
	}

	public String formatSummary() {
		return String.format("[#%d] %s (%s)", id, name, department.isBlank() ? "General" : department);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BookingUser that)) return false;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}