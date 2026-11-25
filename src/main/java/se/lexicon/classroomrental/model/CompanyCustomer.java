package se.lexicon.classroomrental.model;
public class CompanyCustomer extends Customer {

	private String organizationNumber;
	private String contactPerson;

	public CompanyCustomer(
			int id,
			String name,
			String email,
			String phone,
			String organizationNumber,
			String contactPerson
	) {
		super(id, name, email, phone);
		setOrganizationNumber(organizationNumber);
		setContactPerson(contactPerson);
	}

	public String getOrganizationNumber() {
		return organizationNumber;
	}

	public final void setOrganizationNumber(String organizationNumber) {
		if (organizationNumber == null || organizationNumber.isBlank()) {
			throw new IllegalArgumentException("Organization number cannot be blank.");
		}
		this.organizationNumber = organizationNumber.trim();
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public final void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson == null ? "" : contactPerson.trim();
	}

	@Override
	public String getCustomerType() {
		return "COMPANY";
	}

	@Override
	public String getSummary() {
		return String.format(
				"[#%d | COMPANY] %s (Org: %s, Contact: %s, Email: %s, Phone: %s)",
				getId(), getName(), organizationNumber,
				contactPerson.isBlank() ? "N/A" : contactPerson, getEmail(), getPhone()
		);
	}
}