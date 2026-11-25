package se.lexicon.classroomrental.model;
public class IndividualCustomer extends Customer {

	public IndividualCustomer(int id, String name, String email, String phone) {
		super(id, name, email, phone);
	}

	@Override
	public String getCustomerType() {
		return "INDIVIDUAL";
	}

	@Override
	public String getSummary() {
		return String.format(
				"[#%d | INDIVIDUAL] %s (Email: %s, Phone: %s)",
				getId(), getName(), getEmail(), getPhone()
		);
	}
}