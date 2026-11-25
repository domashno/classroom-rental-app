package se.lexicon.classroomrental.ui;

import se.lexicon.classroomrental.exception.DatabaseOperationException;
import se.lexicon.classroomrental.exception.DoubleBookingException;
import se.lexicon.classroomrental.exception.ResourceNotFoundException;
import se.lexicon.classroomrental.exception.ValidationException;
import se.lexicon.classroomrental.model.Booking;
import se.lexicon.classroomrental.model.Classroom;
import se.lexicon.classroomrental.model.Customer;
import se.lexicon.classroomrental.service.RentalService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

	private final RentalService rentalService;
	private final Scanner scanner;

	public ConsoleMenu(RentalService rentalService) {
		this.rentalService = rentalService;
		this.scanner = new Scanner(System.in);
	}

	public void start() {
		boolean running = true;
		while (running) {
			printMainMenu();
			String choice = scanner.nextLine().trim();
			try {
				switch (choice) {
					case "1" -> registerCustomer();
					case "2" -> listCustomers();
					case "3" -> manageClassroomsMenu();
					case "4" -> searchAvailableRooms();
					case "5" -> createBooking();
					case "6" -> viewBookingsByRoom();
					case "7" -> viewBookingsByCustomer();
					case "8" -> viewUpcomingBookings();
					case "9" -> cancelBooking();
					case "0" -> {
						running = false;
						System.out.println("Goodbye!");
					}
					default -> System.out.println("Invalid option. Please choose from the menu.");
				}
			} catch (ValidationException | DoubleBookingException | ResourceNotFoundException e) {
				System.out.println("[Error] " + e.getMessage());
			} catch (DatabaseOperationException e) {
				System.out.println("[Database Error] " + e.getMessage());
			} catch (DateTimeParseException e) {
				System.out.println("[Format Error] Please use YYYY-MM-DD for dates and HH:mm for times (e.g., 09:00).");
			} catch (NumberFormatException e) {
				System.out.println("[Input Error] Please enter a valid number.");
			}
		}
	}

	private void printMainMenu() {
		System.out.println("\n=== CLASSROOM RENTAL SYSTEM ===");
		System.out.println("1. Register Customer (Company / Individual)");
		System.out.println("2. List All Customers");
		System.out.println("3. Manage Classrooms (List / Add / Update)");
		System.out.println("4. Search Available Classrooms");
		System.out.println("5. Create New Booking");
		System.out.println("6. View Bookings by Room");
		System.out.println("7. View Bookings by Customer");
		System.out.println("8. View All Upcoming Bookings");
		System.out.println("9. Cancel Booking");
		System.out.println("0. Exit");
		System.out.print("Select option: ");
	}

	private void registerCustomer() {
		System.out.print("Customer type (1 = Company, 2 = Individual): ");
		String type = scanner.nextLine().trim();
		System.out.print("Name: ");
		String name = scanner.nextLine();
		System.out.print("Email: ");
		String email = scanner.nextLine();
		System.out.print("Phone: ");
		String phone = scanner.nextLine();

		Customer created;
		if ("1".equals(type)) {
			System.out.print("Organization Number: ");
			String orgNo = scanner.nextLine();
			System.out.print("Contact Person: ");
			String contact = scanner.nextLine();
			created = rentalService.registerCompanyCustomer(name, email, phone, orgNo, contact);
		} else if ("2".equals(type)) {
			created = rentalService.registerIndividualCustomer(name, email, phone);
		} else {
			throw new ValidationException("Invalid customer type selected.");
		}
		System.out.println("Customer registered: " + created.getSummary());
	}

	private void listCustomers() {
		List<Customer> customers = rentalService.getAllCustomers();
		if (customers.isEmpty()) {
			System.out.println("No customers registered yet.");
			return;
		}
		customers.forEach(c -> System.out.println(c.getSummary()));
	}

	private void manageClassroomsMenu() {
		System.out.println("\n--- Classroom Management ---");
		System.out.println("1. List All Classrooms");
		System.out.println("2. Add Classroom");
		System.out.println("3. Update Classroom");
		System.out.print("Choose: ");
		String sub = scanner.nextLine().trim();

		switch (sub) {
			case "1" -> rentalService.getAllClassrooms().forEach(r -> System.out.println(r.formatSummary()));
			case "2" -> {
				System.out.print("Room Name: ");
				String name = scanner.nextLine();
				int cap = readInt("Seating Capacity: ");
				System.out.print("Equipment (comma-separated, e.g., Projector,Whiteboard): ");
				List<String> eq = Arrays.asList(scanner.nextLine().split(","));
				boolean acc = readYesNo("Accessible for disabilities (y/n)? ");
				Classroom added = rentalService.addClassroom(name, cap, eq, acc);
				System.out.println("Added: " + added.formatSummary());
			}
			case "3" -> {
				rentalService.getAllClassrooms().forEach(r -> System.out.println(r.formatSummary()));
				int id = readInt("Classroom ID to update: ");
				System.out.print("New Name: ");
				String name = scanner.nextLine();
				int cap = readInt("New Capacity: ");
				System.out.print("New Equipment (comma-separated): ");
				List<String> eq = Arrays.asList(scanner.nextLine().split(","));
				boolean acc = readYesNo("Accessible (y/n)? ");
				rentalService.updateClassroom(id, name, cap, eq, acc);
				System.out.println("Classroom updated.");
			}
			default -> System.out.println("Returning to main menu.");
		}
	}

	private void searchAvailableRooms() {
		LocalDate date = LocalDate.parse(readNonBlank("Date (YYYY-MM-DD): "));
		LocalTime start = LocalTime.parse(readNonBlank("Start Time (HH:mm): "));
		LocalTime end = LocalTime.parse(readNonBlank("End Time (HH:mm): "));
		int minCap = readInt("Required Minimum Capacity: ");
		System.out.print("Required Equipment (leave blank for any): ");
		String equipment = scanner.nextLine().trim();
		boolean accOnly = readYesNo("Require accessibility (y/n)? ");

		List<Classroom> available = rentalService.searchAvailableClassrooms(
				date, start, end, minCap, equipment, accOnly
		);
		if (available.isEmpty()) {
			System.out.println("No matching available classrooms found.");
			return;
		}
		System.out.println("\nAvailable Classrooms:");
		available.forEach(r -> System.out.println(r.formatSummary()));
	}

	private void createBooking() {
		System.out.println("\nAvailable Booking Users:");
		rentalService.getAllBookingUsers().forEach(u -> System.out.println(u.formatSummary()));
		int userId = readInt("Your Booking User ID: ");

		System.out.println("\nCustomers:");
		listCustomers();
		int customerId = readInt("Customer ID: ");

		System.out.println("\nClassrooms:");
		rentalService.getAllClassrooms().forEach(r -> System.out.println(r.formatSummary()));
		int roomId = readInt("Classroom ID: ");

		LocalDate date = LocalDate.parse(readNonBlank("Booking Date (YYYY-MM-DD): "));
		LocalTime start = LocalTime.parse(readNonBlank("Start Time (HH:mm): "));
		LocalTime end = LocalTime.parse(readNonBlank("End Time (HH:mm): "));
		System.out.print("Optional Comments (e.g., nut allergy, setup notes): ");
		String comments = scanner.nextLine().trim();

		Booking created = rentalService.createBooking(roomId, customerId, userId, date, start, end, comments);
		System.out.println("Booking confirmed: " + created.formatSummary());
	}

	private void viewBookingsByRoom() {
		rentalService.getAllClassrooms().forEach(r -> System.out.println(r.formatSummary()));
		int roomId = readInt("Classroom ID: ");
		printBookings(rentalService.getBookingsByRoom(roomId));
	}

	private void viewBookingsByCustomer() {
		listCustomers();
		int customerId = readInt("Customer ID: ");
		printBookings(rentalService.getBookingsByCustomer(customerId));
	}

	private void viewUpcomingBookings() {
		printBookings(rentalService.getUpcomingBookings());
	}

	private void cancelBooking() {
		int bookingId = readInt("Booking ID to cancel: ");
		boolean removed = rentalService.cancelBooking(bookingId);
		System.out.println(removed ? "Booking cancelled." : "Booking not found.");
	}

	private void printBookings(List<Booking> bookings) {
		if (bookings.isEmpty()) {
			System.out.println("No bookings found.");
			return;
		}
		bookings.forEach(b -> System.out.println(b.formatSummary()));
	}

	private int readInt(String prompt) {
		System.out.print(prompt);
		return Integer.parseInt(scanner.nextLine().trim());
	}

	private String readNonBlank(String prompt) {
		System.out.print(prompt);
		String val = scanner.nextLine().trim();
		if (val.isBlank()) {
			throw new ValidationException("Input cannot be blank.");
		}
		return val;
	}

	private boolean readYesNo(String prompt) {
		System.out.print(prompt);
		String ans = scanner.nextLine().trim().toLowerCase();
		return ans.equals("y") || ans.equals("yes") || ans.equals("true");
	}
}