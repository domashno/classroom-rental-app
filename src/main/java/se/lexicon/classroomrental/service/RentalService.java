package se.lexicon.classroomrental.service;

import se.lexicon.classroomrental.exception.DoubleBookingException;
import se.lexicon.classroomrental.exception.ResourceNotFoundException;
import se.lexicon.classroomrental.exception.ValidationException;
import se.lexicon.classroomrental.model.*;
import se.lexicon.classroomrental.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public class RentalService {

	private final ClassroomDao classroomDao;
	private final CustomerDao customerDao;
	private final BookingUserDao bookingUserDao;
	private final BookingDao bookingDao;

	public RentalService(
			ClassroomDao classroomDao,
			CustomerDao customerDao,
			BookingUserDao bookingUserDao,
			BookingDao bookingDao
	) {
		this.classroomDao = classroomDao;
		this.customerDao = customerDao;
		this.bookingUserDao = bookingUserDao;
		this.bookingDao = bookingDao;
	}

	public Customer registerCompanyCustomer(
			String name, String email, String phone, String orgNumber, String contactPerson
	) {
		try {
			CompanyCustomer customer = new CompanyCustomer(0, name, email, phone, orgNumber, contactPerson);
			return customerDao.save(customer);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	public Customer registerIndividualCustomer(String name, String email, String phone) {
		try {
			IndividualCustomer customer = new IndividualCustomer(0, name, email, phone);
			return customerDao.save(customer);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	public List<Customer> getAllCustomers() {
		return customerDao.findAll().stream()
				.sorted(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	public List<Classroom> getAllClassrooms() {
		return classroomDao.findAll().stream()
				.sorted(Comparator.comparingInt(Classroom::getId))
				.toList();
	}

	public Classroom addClassroom(String name, int capacity, List<String> equipment, boolean accessible) {
		try {
			Classroom room = new Classroom(0, name, capacity, equipment, accessible);
			return classroomDao.save(room);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	public void updateClassroom(int roomId, String name, int capacity, List<String> equipment, boolean accessible) {
		Classroom existing = classroomDao.findById(roomId)
				.orElseThrow(() -> new ResourceNotFoundException("Classroom #" + roomId + " not found."));
		try {
			existing.setName(name);
			existing.setCapacity(capacity);
			existing.setEquipment(equipment);
			existing.setAccessible(accessible);
			classroomDao.update(existing);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	public List<BookingUser> getAllBookingUsers() {
		return bookingUserDao.findAll();
	}

	public List<Classroom> searchAvailableClassrooms(
			LocalDate date,
			LocalTime startTime,
			LocalTime endTime,
			int minCapacity,
			String requiredEquipment,
			boolean accessibilityRequired
	) {
		validateTimeRange(date, startTime, endTime);
		if (minCapacity <= 0) {
			throw new ValidationException("Minimum capacity must be greater than 0.");
		}

		List<Booking> allBookings = bookingDao.findAll();

		return classroomDao.findAll().stream()
				.filter(room -> room.getCapacity() >= minCapacity)
				.filter(room -> !accessibilityRequired || room.isAccessible())
				.filter(room -> room.hasEquipment(requiredEquipment))
				.filter(room -> allBookings.stream()
						.filter(b -> b.getClassroom().getId() == room.getId())
						.noneMatch(b -> b.overlapsWith(date, startTime, endTime)))
				.sorted(Comparator.comparingInt(Classroom::getCapacity)
						.thenComparing(Classroom::getName))
				.toList();
	}

	public Booking createBooking(
			int classroomId,
			int customerId,
			int bookingUserId,
			LocalDate date,
			LocalTime startTime,
			LocalTime endTime,
			String comments
	) {
		validateTimeRange(date, startTime, endTime);

		Classroom room = classroomDao.findById(classroomId)
				.orElseThrow(() -> new ResourceNotFoundException("Classroom #" + classroomId + " not found."));
		Customer customer = customerDao.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer #" + customerId + " not found."));
		BookingUser user = bookingUserDao.findById(bookingUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Booking user #" + bookingUserId + " not found."));

		boolean hasConflict = bookingDao.findAll().stream()
				.filter(b -> b.getClassroom().getId() == room.getId())
				.anyMatch(b -> b.overlapsWith(date, startTime, endTime));

		if (hasConflict) {
			throw new DoubleBookingException(String.format(
					"Classroom '%s' is already booked on %s between %s and %s.",
					room.getName(), date, startTime, endTime
			));
		}

		try {
			Booking newBooking = new Booking(0, room, customer, user, date, startTime, endTime, comments);
			return bookingDao.save(newBooking);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	public List<Booking> getBookingsByRoom(int classroomId) {
		return bookingDao.findAll().stream()
				.filter(b -> b.getClassroom().getId() == classroomId)
				.sorted(Comparator.comparing(Booking::getStartDateTime))
				.toList();
	}

	public List<Booking> getBookingsByCustomer(int customerId) {
		return bookingDao.findAll().stream()
				.filter(b -> b.getCustomer().getId() == customerId)
				.sorted(Comparator.comparing(Booking::getStartDateTime))
				.toList();
	}

	public List<Booking> getUpcomingBookings() {
		LocalDateTime now = LocalDateTime.now();
		return bookingDao.findAll().stream()
				.filter(b -> !LocalDateTime.of(b.getDate(), b.getEndTime()).isBefore(now))
				.sorted(Comparator.comparing(Booking::getStartDateTime))
				.toList();
	}

	public boolean cancelBooking(int bookingId) {
		return bookingDao.deleteById(bookingId);
	}

	private void validateTimeRange(LocalDate date, LocalTime startTime, LocalTime endTime) {
		if (date == null || startTime == null || endTime == null) {
			throw new ValidationException("Date, start time, and end time are required.");
		}
		if (!endTime.isAfter(startTime)) {
			throw new ValidationException("End time must be after start time.");
		}
	}
}