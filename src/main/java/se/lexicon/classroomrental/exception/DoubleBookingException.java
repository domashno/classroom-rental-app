package se.lexicon.classroomrental.exception;

public class DoubleBookingException extends RuntimeException {
	public DoubleBookingException(String message) {
		super(message);
	}
}