package se.lexicon.classroomrental;

import se.lexicon.classroomrental.config.DatabaseConfig;
import se.lexicon.classroomrental.repository.*;
import se.lexicon.classroomrental.service.RentalService;
import se.lexicon.classroomrental.ui.ConsoleMenu;


public class Main {

	public static void main(String[] args) {
		DatabaseConfig dbConfig = new DatabaseConfig();
		ClassroomDao classroomDao = new ClassroomDao(dbConfig);
		CustomerDao customerDao = new CustomerDao(dbConfig);
		BookingUserDao bookingUserDao = new BookingUserDao(dbConfig);
		BookingDao bookingDao = new BookingDao(dbConfig);

		RentalService rentalService = new RentalService(
				classroomDao, customerDao, bookingUserDao, bookingDao
		);

		new ConsoleMenu(rentalService).start();
	}
}