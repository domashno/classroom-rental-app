package se.lexicon.classroomrental.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Booking {

	private final int id;
	private final Classroom classroom;
	private final Customer customer;
	private final BookingUser bookingUser;
	private final LocalDate date;
	private final LocalTime startTime;
	private final LocalTime endTime;
	private String comments;

	public Booking(
			int id,
			Classroom classroom,
			Customer customer,
			BookingUser bookingUser,
			LocalDate date,
			LocalTime startTime,
			LocalTime endTime,
			String comments
	) {
		this.id = id;
		this.classroom = Objects.requireNonNull(classroom, "Classroom is required.");
		this.customer = Objects.requireNonNull(customer, "Customer is required.");
		this.bookingUser = Objects.requireNonNull(bookingUser, "Booking user is required.");
		this.date = Objects.requireNonNull(date, "Booking date is required.");
		this.startTime = Objects.requireNonNull(startTime, "Start time is required.");
		this.endTime = Objects.requireNonNull(endTime, "End time is required.");
		if (!endTime.isAfter(startTime)) {
			throw new IllegalArgumentException("End time must be after start time.");
		}
		this.comments = comments == null ? "" : comments.trim();
	}

	public int getId() {
		return id;
	}

	public Classroom getClassroom() {
		return classroom;
	}

	public Customer getCustomer() {
		return customer;
	}

	public BookingUser getBookingUser() {
		return bookingUser;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments == null ? "" : comments.trim();
	}

	public LocalDateTime getStartDateTime() {
		return LocalDateTime.of(date, startTime);
	}

	public boolean overlapsWith(LocalDate targetDate, LocalTime targetStart, LocalTime targetEnd) {
		if (!this.date.equals(targetDate)) {
			return false;
		}
		return this.startTime.isBefore(targetEnd) && this.endTime.isAfter(targetStart);
	}

	public String formatSummary() {
		String notePart = comments.isBlank() ? "" : " | Notes: " + comments;
		return String.format(
				"[Booking #%d] %s %s-%s | Room: %s | Customer: %s (%s) | By: %s%s",
				id, date, startTime, endTime, classroom.getName(),
				customer.getName(), customer.getCustomerType(), bookingUser.getName(), notePart
		);
	}
}